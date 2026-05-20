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
}
