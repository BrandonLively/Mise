package com.patchfox.mise.data.cookcard

import android.content.Context
import android.util.Log
import com.patchfox.mise.data.auth.AuthRepository
import com.patchfox.mise.data.local.CookCardDao
import com.patchfox.mise.data.local.CookCardEntity
import com.patchfox.mise.domain.mapper.toDomain
import com.patchfox.mise.domain.model.CookCard
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Singleton
class CookCardRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val auth: AuthRepository,
    private val cookCardDao: CookCardDao,
    private val firestoreSource: CookCardSource,
) : CookCardRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val syncError = MutableStateFlow<String?>(null)

    private companion object {
        const val TAG = "CookCardSync"
    }

    init {
        // When the signed-in user changes, attach a Firestore listener for that uid.
        scope.launch {
            auth.currentUser.collect { user ->
                if (user != null) {
                    Log.d(TAG, "Signed-in user ${user.uid} — starting cook-card sync")
                    firestoreSource.startListening { payload, error ->
                        if (payload != null) {
                            scope.launch {
                                // Validate the FULL parse + domain mapping before caching,
                                // so a payload the DTO accepts but the mapper rejects can't
                                // poison the cache and leave the UI stuck on Loading.
                                val result = runCatching { CookCardParser.parse(payload).toDomain() }
                                val card = result.getOrNull()
                                if (card != null) {
                                    Log.d(TAG, "Mapped cook card '${card.id.value}' — writing to cache")
                                    cookCardDao.upsert(
                                        CookCardEntity(
                                            uid = user.uid,
                                            cookCardId = card.id.value,
                                            jsonPayload = payload,
                                            fetchedAtEpochMs = System.currentTimeMillis(),
                                        ),
                                    )
                                    syncError.value = null
                                } else {
                                    Log.e(TAG, "Failed to parse/map cook card payload", result.exceptionOrNull())
                                    syncError.value =
                                        "Could not read latest cook card: ${result.exceptionOrNull()?.message}"
                                }
                            }
                        } else if (error != null) {
                            Log.e(TAG, "Cook-card sync error: $error")
                            syncError.value = error
                        }
                    }
                    seedFromBundledAssetIfEmpty(user.uid)
                } else {
                    Log.d(TAG, "No signed-in user — stopping cook-card sync")
                    firestoreSource.stopListening()
                }
            }
        }
    }

    override fun observe(): Flow<CookCardLoadState> {
        return auth.currentUser.flatMapLatest { user ->
            if (user == null) {
                flow { emit(CookCardLoadState.Loading) }
            } else {
                cookCardDao.observe(user.uid).combine(syncError) { entity, err ->
                    val card = entity?.toCookCardOrNull()
                    when {
                        card != null && err != null -> CookCardLoadState.Error(err, card)
                        card != null -> CookCardLoadState.Success(card)
                        err != null -> CookCardLoadState.Error(err, null)
                        // A cached row exists but won't map — surface an error
                        // rather than hanging on the loading spinner forever.
                        entity != null -> CookCardLoadState.Error(
                            "Cook card data couldn't be read.", null,
                        )
                        else -> CookCardLoadState.Loading
                    }
                }
            }
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun refresh() {
        if (auth.currentUser.value == null) return
        firestoreSource.refreshOnce()
    }

    override suspend fun getHistory(): List<CookCard> = withContext(Dispatchers.IO) {
        if (auth.currentUser.value == null) return@withContext emptyList()
        firestoreSource.fetchAll().mapNotNull { raw ->
            runCatching { CookCardParser.parse(raw.payload).toDomain() }
                .onFailure { Log.e(TAG, "Skipping unparseable history doc '${raw.id}'", it) }
                .getOrNull()
        }
    }

    override suspend fun getById(id: String): CookCard? =
        getHistory().firstOrNull { it.id.value == id }

    private fun CookCardEntity.toCookCardOrNull(): CookCard? =
        runCatching { CookCardParser.parse(jsonPayload).toDomain() }
            .onFailure { Log.e(TAG, "Failed to map cached cook card to domain", it) }
            .getOrNull()

    /**
     * On a fresh debug install, seed the cache from the bundled sample JSON so the UI has
     * something to render before Firestore data exists. Release builds skip this.
     */
    private suspend fun seedFromBundledAssetIfEmpty(uid: String) {
        if (cookCardDao.get(uid) != null) return
        val payload = runCatching {
            withContext(Dispatchers.IO) {
                context.assets.open("sample-cook-card.json").bufferedReader().use { it.readText() }
            }
        }.getOrNull() ?: return

        val parsed = runCatching { CookCardParser.parse(payload) }.getOrNull() ?: return
        cookCardDao.upsert(
            CookCardEntity(
                uid = uid,
                cookCardId = parsed.meta.id,
                jsonPayload = payload,
                fetchedAtEpochMs = System.currentTimeMillis(),
            ),
        )
    }
}
