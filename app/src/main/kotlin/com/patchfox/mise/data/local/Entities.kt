package com.patchfox.mise.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cook_card")
data class CookCardEntity(
    @PrimaryKey val uid: String,
    val cookCardId: String,
    val jsonPayload: String,
    val fetchedAtEpochMs: Long,
)

@Entity(tableName = "mise_check", primaryKeys = ["cookCardId", "bowlId"])
data class MiseCheckEntity(
    val cookCardId: String,
    val bowlId: String,
    val checkedAtEpochMs: Long,
)

@Entity(tableName = "prep_check", primaryKeys = ["cookCardId", "prepTaskId"])
data class PrepCheckEntity(
    val cookCardId: String,
    val prepTaskId: String,
    val checkedAtEpochMs: Long,
)

@Entity(tableName = "step_completion", primaryKeys = ["cookCardId", "stepId"])
data class StepCompletionEntity(
    val cookCardId: String,
    val stepId: String,
    val completedAtEpochMs: Long,
)

@Entity(tableName = "started_timer", primaryKeys = ["cookCardId", "stepId"])
data class StartedTimerEntity(
    val cookCardId: String,
    val stepId: String,
    val startedAtEpochMs: Long,
)

@Entity(tableName = "summary_input", primaryKeys = ["cookCardId", "recipeId", "componentLabel"])
data class SummaryInputEntity(
    val cookCardId: String,
    val recipeId: String,
    val componentLabel: String,
    val value: String,
)

@Entity(tableName = "days_divisor")
data class DaysDivisorEntity(
    @PrimaryKey val cookCardId: String,
    val days: Int,
)

@Entity(tableName = "lane_visibility", primaryKeys = ["cookCardId", "stage", "laneId"])
data class LaneVisibilityEntity(
    val cookCardId: String,
    val stage: String,
    val laneId: String,
    val updatedAtEpochMs: Long,
)

@Entity(tableName = "cook_step_index", primaryKeys = ["cookCardId", "laneKey"])
data class CookStepIndexEntity(
    val cookCardId: String,
    val laneKey: String,
    val stepIndex: Int,
)

@Entity(tableName = "completed_recipe", primaryKeys = ["cookCardId", "recipeId"])
data class CompletedRecipeEntity(
    val cookCardId: String,
    val recipeId: String,
    val completedAtEpochMs: Long,
)

@Entity(tableName = "pending_alarm")
data class PendingAlarmEntity(
    @PrimaryKey val timerId: String,
    val cookCardId: String,
    val stepId: String,
    val title: String,
    val durationSeconds: Int,
    val expiresAtRealtimeMs: Long,
    val recipeColor: String?,
)
