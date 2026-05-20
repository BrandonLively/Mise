package com.patchfox.mise.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CookCardDao {
    @Query("SELECT * FROM cook_card WHERE uid = :uid LIMIT 1")
    fun observe(uid: String): Flow<CookCardEntity?>

    @Query("SELECT * FROM cook_card WHERE uid = :uid LIMIT 1")
    suspend fun get(uid: String): CookCardEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(card: CookCardEntity)

    @Query("DELETE FROM cook_card WHERE uid = :uid")
    suspend fun deleteForUid(uid: String)
}

@Dao
interface MiseCheckDao {
    @Query("SELECT bowlId FROM mise_check WHERE cookCardId = :cookCardId")
    fun observe(cookCardId: String): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: MiseCheckEntity)

    @Query("DELETE FROM mise_check WHERE cookCardId = :cookCardId AND bowlId = :bowlId")
    suspend fun delete(cookCardId: String, bowlId: String)
}

@Dao
interface PrepCheckDao {
    @Query("SELECT prepTaskId FROM prep_check WHERE cookCardId = :cookCardId")
    fun observe(cookCardId: String): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: PrepCheckEntity)

    @Query("DELETE FROM prep_check WHERE cookCardId = :cookCardId AND prepTaskId = :prepTaskId")
    suspend fun delete(cookCardId: String, prepTaskId: String)
}

@Dao
interface StepCompletionDao {
    @Query("SELECT stepId FROM step_completion WHERE cookCardId = :cookCardId")
    fun observe(cookCardId: String): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: StepCompletionEntity)

    @Query("DELETE FROM step_completion WHERE cookCardId = :cookCardId AND stepId = :stepId")
    suspend fun delete(cookCardId: String, stepId: String)
}

@Dao
interface StartedTimerDao {
    @Query("SELECT stepId FROM started_timer WHERE cookCardId = :cookCardId")
    fun observe(cookCardId: String): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: StartedTimerEntity)

    @Query("DELETE FROM started_timer WHERE cookCardId = :cookCardId AND stepId = :stepId")
    suspend fun delete(cookCardId: String, stepId: String)
}

@Dao
interface SummaryInputDao {
    @Query("SELECT * FROM summary_input WHERE cookCardId = :cookCardId")
    fun observe(cookCardId: String): Flow<List<SummaryInputEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: SummaryInputEntity)

    @Query("DELETE FROM summary_input WHERE cookCardId = :cookCardId AND recipeId = :recipeId AND componentLabel = :label")
    suspend fun delete(cookCardId: String, recipeId: String, label: String)
}

@Dao
interface DaysDivisorDao {
    @Query("SELECT days FROM days_divisor WHERE cookCardId = :cookCardId LIMIT 1")
    fun observe(cookCardId: String): Flow<Int?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: DaysDivisorEntity)
}

@Dao
interface PendingAlarmDao {
    @Query("SELECT * FROM pending_alarm")
    suspend fun getAll(): List<PendingAlarmEntity>

    @Query("SELECT * FROM pending_alarm WHERE timerId = :id LIMIT 1")
    suspend fun get(id: String): PendingAlarmEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: PendingAlarmEntity)

    @Query("DELETE FROM pending_alarm WHERE timerId = :id")
    suspend fun delete(id: String)
}
