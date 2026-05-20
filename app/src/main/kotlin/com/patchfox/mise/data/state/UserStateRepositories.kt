package com.patchfox.mise.data.state

import com.patchfox.mise.data.local.DaysDivisorDao
import com.patchfox.mise.data.local.DaysDivisorEntity
import com.patchfox.mise.data.local.MiseCheckDao
import com.patchfox.mise.data.local.MiseCheckEntity
import com.patchfox.mise.data.local.PrepCheckDao
import com.patchfox.mise.data.local.PrepCheckEntity
import com.patchfox.mise.data.local.StartedTimerDao
import com.patchfox.mise.data.local.StartedTimerEntity
import com.patchfox.mise.data.local.StepCompletionDao
import com.patchfox.mise.data.local.StepCompletionEntity
import com.patchfox.mise.data.local.SummaryInputDao
import com.patchfox.mise.data.local.SummaryInputEntity
import com.patchfox.mise.domain.model.BowlId
import com.patchfox.mise.domain.model.CookCardId
import com.patchfox.mise.domain.model.PrepTaskId
import com.patchfox.mise.domain.model.RecipeId
import com.patchfox.mise.domain.model.StepId
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class MiseCheckRepository @Inject constructor(private val dao: MiseCheckDao) {
    fun observe(cookCardId: CookCardId): Flow<Set<BowlId>> =
        dao.observe(cookCardId.value).map { it.map(::BowlId).toSet() }

    suspend fun toggle(cookCardId: CookCardId, bowlId: BowlId, checked: Boolean) {
        if (checked) {
            dao.insert(MiseCheckEntity(cookCardId.value, bowlId.value, System.currentTimeMillis()))
        } else {
            dao.delete(cookCardId.value, bowlId.value)
        }
    }
}

@Singleton
class PrepCheckRepository @Inject constructor(private val dao: PrepCheckDao) {
    fun observe(cookCardId: CookCardId): Flow<Set<PrepTaskId>> =
        dao.observe(cookCardId.value).map { it.map(::PrepTaskId).toSet() }

    suspend fun toggle(cookCardId: CookCardId, id: PrepTaskId, checked: Boolean) {
        if (checked) {
            dao.insert(PrepCheckEntity(cookCardId.value, id.value, System.currentTimeMillis()))
        } else {
            dao.delete(cookCardId.value, id.value)
        }
    }
}

@Singleton
class StepCompletionRepository @Inject constructor(private val dao: StepCompletionDao) {
    fun observe(cookCardId: CookCardId): Flow<Set<StepId>> =
        dao.observe(cookCardId.value).map { it.map(::StepId).toSet() }

    suspend fun toggle(cookCardId: CookCardId, id: StepId, completed: Boolean) {
        if (completed) {
            dao.insert(StepCompletionEntity(cookCardId.value, id.value, System.currentTimeMillis()))
        } else {
            dao.delete(cookCardId.value, id.value)
        }
    }
}

@Singleton
class StartedTimerRepository @Inject constructor(private val dao: StartedTimerDao) {
    fun observe(cookCardId: CookCardId): Flow<Set<StepId>> =
        dao.observe(cookCardId.value).map { it.map(::StepId).toSet() }

    suspend fun markStarted(cookCardId: CookCardId, id: StepId) {
        dao.insert(StartedTimerEntity(cookCardId.value, id.value, System.currentTimeMillis()))
    }

    suspend fun clear(cookCardId: CookCardId, id: StepId) {
        dao.delete(cookCardId.value, id.value)
    }
}

@Singleton
class SummaryInputRepository @Inject constructor(private val dao: SummaryInputDao) {
    data class Key(val recipeId: RecipeId, val componentLabel: String)

    fun observe(cookCardId: CookCardId): Flow<Map<Key, String>> =
        dao.observe(cookCardId.value).map { rows ->
            rows.associate { Key(RecipeId(it.recipeId), it.componentLabel) to it.value }
        }

    suspend fun set(cookCardId: CookCardId, key: Key, value: String) {
        if (value.isBlank()) {
            dao.delete(cookCardId.value, key.recipeId.value, key.componentLabel)
        } else {
            dao.upsert(
                SummaryInputEntity(cookCardId.value, key.recipeId.value, key.componentLabel, value),
            )
        }
    }
}

@Singleton
class DaysDivisorRepository @Inject constructor(private val dao: DaysDivisorDao) {
    fun observe(cookCardId: CookCardId): Flow<Int> =
        dao.observe(cookCardId.value).map { it ?: DEFAULT_DAYS }

    suspend fun set(cookCardId: CookCardId, days: Int) {
        dao.upsert(DaysDivisorEntity(cookCardId.value, days.coerceIn(1, 14)))
    }

    companion object { const val DEFAULT_DAYS = 7 }
}
