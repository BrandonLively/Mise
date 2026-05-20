package com.patchfox.mise.data.cookcard

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

private const val TAG = "CookCardSync"

/**
 * Cook-card sync via Firestore.
 *
 * The `cookCards` collection holds many cook-card documents, each stored as native
 * Firestore fields (not a JSON blob). This source listens to the whole collection and
 * republishes whichever document is newest — ranked by a `createdAt` / `updatedAt`
 * timestamp field. The winning document's native fields are re-serialized into a JSON
 * string so the existing [CookCardParser] can decode it unchanged.
 */
@Singleton
class FirestoreCookCardSource @Inject constructor(
    private val firestore: FirebaseFirestore,
) : CookCardSource {

    private var registration: ListenerRegistration? = null

    override fun startListening(onUpdate: (payload: String?, error: String?) -> Unit) {
        registration?.remove()
        Log.d(TAG, "Attaching snapshot listener to '$COLLECTION' collection")
        registration = firestore.collection(COLLECTION)
            .addSnapshotListener { snap, e ->
                if (e != null) {
                    Log.e(TAG, "Firestore listen error on '$COLLECTION'", e)
                    onUpdate(null, e.message ?: "Firestore error")
                    return@addSnapshotListener
                }
                val docs = snap?.documents.orEmpty()
                Log.d(
                    TAG,
                    "Snapshot from '$COLLECTION': ${docs.size} document(s), " +
                        "fromCache=${snap?.metadata?.isFromCache}",
                )
                val latest = docs.maxByOrNull { it.latestTimestampMillis() }
                if (latest == null) {
                    Log.w(TAG, "'$COLLECTION' is empty — keeping existing cache")
                    return@addSnapshotListener
                }
                val payload = latest.toJsonPayload()
                if (payload != null) {
                    Log.d(
                        TAG,
                        "Latest doc '${latest.id}' rank=${latest.latestTimestampMillis()}, " +
                            "payload ${payload.length} chars",
                    )
                    onUpdate(payload, null)
                } else {
                    Log.e(TAG, "Could not serialize doc '${latest.id}' to JSON")
                    onUpdate(null, "Could not read the latest cook card document")
                }
            }
    }

    override fun stopListening() {
        registration?.remove()
        registration = null
    }

    override suspend fun refreshOnce() {
        // Forces a server round-trip; the snapshot listener delivers the result.
        firestore.collection(COLLECTION).get().await()
    }

    /** Newest-touched time, used to rank documents. Checks the common field names. */
    private fun DocumentSnapshot.latestTimestampMillis(): Long {
        for (field in TIMESTAMP_FIELDS) {
            val millis = get(field).toEpochMillisOrNull()
            if (millis != null) return millis
        }
        return 0L
    }

    /** Re-serialize the document's native fields into a JSON string for the parser. */
    private fun DocumentSnapshot.toJsonPayload(): String? {
        val data = data ?: return null
        return runCatching { data.toJsonElement().toString() }.getOrNull()
    }

    companion object {
        private const val COLLECTION = "cookCards"
        private val TIMESTAMP_FIELDS =
            listOf("uploadedAt", "updatedAt", "createdAt", "updated_at", "created_at")
    }
}

private fun Any?.toEpochMillisOrNull(): Long? = when (this) {
    is Timestamp -> toDate().time
    is Date -> time
    // Heuristic: values below ~year 5138 in seconds are treated as seconds.
    is Long -> if (this < 100_000_000_000L) this * 1000 else this
    is Int -> toLong() * 1000
    is Double -> toLong()
    is String -> runCatching { java.time.Instant.parse(this).toEpochMilli() }.getOrNull()
    else -> null
}

/** Recursively converts a Firestore value tree into a kotlinx-serialization [JsonElement]. */
private fun Any?.toJsonElement(): JsonElement = when (this) {
    null -> JsonNull
    is String -> JsonPrimitive(this)
    is Boolean -> JsonPrimitive(this)
    is Int -> JsonPrimitive(this)
    is Long -> JsonPrimitive(this)
    is Double -> JsonPrimitive(this)
    is Float -> JsonPrimitive(this)
    is Number -> JsonPrimitive(this)
    is Timestamp -> JsonPrimitive(toDate().time)
    is Date -> JsonPrimitive(time)
    is Map<*, *> -> JsonObject(
        entries.mapNotNull { (k, v) ->
            val key = k as? String ?: return@mapNotNull null
            key to v.toJsonElement()
        }.toMap(),
    )
    is Iterable<*> -> JsonArray(map { it.toJsonElement() })
    else -> JsonPrimitive(toString())
}
