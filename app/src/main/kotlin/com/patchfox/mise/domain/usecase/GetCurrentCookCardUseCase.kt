package com.patchfox.mise.domain.usecase

import com.patchfox.mise.data.cookcard.CookCardLoadState
import com.patchfox.mise.data.cookcard.CookCardRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class GetCurrentCookCardUseCase @Inject constructor(
    private val repo: CookCardRepository,
) {
    operator fun invoke(): Flow<CookCardLoadState> = repo.observe()
}

class RefreshCookCardUseCase @Inject constructor(
    private val repo: CookCardRepository,
) {
    suspend operator fun invoke() = repo.refresh()
}
