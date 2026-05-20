package com.patchfox.mise.domain.usecase

import com.patchfox.mise.domain.model.CookCard
import com.patchfox.mise.domain.model.CookStep
import com.patchfox.mise.domain.model.phase2
import javax.inject.Inject

/**
 * Given the wall-clock minutes since cook start, return the index of the Phase 2 step
 * that's currently active. Returns 0 if before the first step, last index if past the end,
 * otherwise the latest step whose clockMinutes ≤ elapsed.
 */
class ResolveCurrentStepUseCase @Inject constructor() {
    operator fun invoke(card: CookCard, elapsedMinutes: Int): Int {
        val steps = card.phase2()?.steps.orEmpty()
        if (steps.isEmpty()) return 0
        if (elapsedMinutes < steps.first().clockMinutes) return 0
        val last = steps.indexOfLast { it.clockMinutes <= elapsedMinutes }
        return if (last < 0) 0 else last
    }

    fun stepAt(card: CookCard, elapsedMinutes: Int): CookStep? =
        card.phase2()?.steps?.getOrNull(invoke(card, elapsedMinutes))
}
