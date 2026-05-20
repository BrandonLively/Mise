package com.patchfox.mise.ui.state

import com.patchfox.mise.ui.component.PhaseTab
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * App-wide signal for "is the Cook screen visible and which phase tab is active."
 *
 * Used by [com.patchfox.mise.ui.MiseAppViewModel] to decide whether a done timer
 * should pop the app-level alert dialog. When the user is on Phase 2 they already
 * see the active-timers strip, so no dialog is needed — for every other location
 * (other tabs, or Phase 0 / Phase 1 inside Cook) we surface the alert.
 *
 * CookScreen updates [setCookScreenVisible] via `DisposableEffect`; CookViewModel
 * keeps [setPhase] in sync with its `selectedPhase` flow.
 */
@Singleton
class CookSessionState @Inject constructor() {

    private val _isCookScreenVisible = MutableStateFlow(false)
    val isCookScreenVisible: StateFlow<Boolean> = _isCookScreenVisible.asStateFlow()

    private val _phase = MutableStateFlow<PhaseTab?>(null)
    val phase: StateFlow<PhaseTab?> = _phase.asStateFlow()

    fun setCookScreenVisible(visible: Boolean) {
        _isCookScreenVisible.value = visible
    }

    fun setPhase(phase: PhaseTab) {
        _phase.value = phase
    }
}
