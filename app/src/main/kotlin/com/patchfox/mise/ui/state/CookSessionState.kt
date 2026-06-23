package com.patchfox.mise.ui.state

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * App-wide signal for "is the Cook screen visible, and which stage is active."
 *
 * Used by [com.patchfox.mise.ui.MiseAppViewModel] to decide whether a done timer
 * should pop the app-level alert dialog. On the [CookStage.COOK] stage the command
 * bar already shows running/expired timers inline, so the dialog is suppressed
 * there; for every other location (the Mise stage, or other tabs) it surfaces.
 *
 * CookScreen updates [setCookScreenVisible] via `DisposableEffect`; CookViewModel
 * keeps [setStage] in sync with its selected stage.
 */
@Singleton
class CookSessionState @Inject constructor() {

    private val _isCookScreenVisible = MutableStateFlow(false)
    val isCookScreenVisible: StateFlow<Boolean> = _isCookScreenVisible.asStateFlow()

    private val _stage = MutableStateFlow<CookStage?>(null)
    val stage: StateFlow<CookStage?> = _stage.asStateFlow()

    fun setCookScreenVisible(visible: Boolean) {
        _isCookScreenVisible.value = visible
    }

    fun setStage(stage: CookStage) {
        _stage.value = stage
    }
}
