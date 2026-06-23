package com.patchfox.mise.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        CookCardEntity::class,
        MiseCheckEntity::class,
        PrepCheckEntity::class,
        StepCompletionEntity::class,
        StartedTimerEntity::class,
        SummaryInputEntity::class,
        DaysDivisorEntity::class,
        LaneVisibilityEntity::class,
        CookStepIndexEntity::class,
        CompletedRecipeEntity::class,
        PendingAlarmEntity::class,
    ],
    version = 2,
    exportSchema = false,
)
abstract class MiseDatabase : RoomDatabase() {
    abstract fun cookCardDao(): CookCardDao
    abstract fun miseCheckDao(): MiseCheckDao
    abstract fun prepCheckDao(): PrepCheckDao
    abstract fun stepCompletionDao(): StepCompletionDao
    abstract fun startedTimerDao(): StartedTimerDao
    abstract fun summaryInputDao(): SummaryInputDao
    abstract fun daysDivisorDao(): DaysDivisorDao
    abstract fun laneVisibilityDao(): LaneVisibilityDao
    abstract fun cookStepIndexDao(): CookStepIndexDao
    abstract fun completedRecipeDao(): CompletedRecipeDao
    abstract fun pendingAlarmDao(): PendingAlarmDao
}
