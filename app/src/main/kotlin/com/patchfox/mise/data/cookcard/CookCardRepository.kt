package com.patchfox.mise.data.cookcard

import com.patchfox.mise.domain.model.CookCard
import kotlinx.coroutines.flow.Flow

sealed interface CookCardLoadState {
    data object Loading : CookCardLoadState
    data class Success(val card: CookCard) : CookCardLoadState
    data class Error(val message: String, val cached: CookCard?) : CookCardLoadState
}

interface CookCardRepository {
    fun observe(): Flow<CookCardLoadState>
    suspend fun refresh()

    /** Every cook card the signed-in user has, newest-first. Parsing failures are dropped. */
    suspend fun getHistory(): List<CookCard>

    /** A single cook card by its domain id, or null if absent / unparseable. */
    suspend fun getById(id: String): CookCard?
}
