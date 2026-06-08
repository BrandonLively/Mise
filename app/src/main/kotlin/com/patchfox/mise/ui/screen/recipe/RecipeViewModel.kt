package com.patchfox.mise.ui.screen.recipe

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patchfox.mise.data.cookcard.CookCardLoadState
import com.patchfox.mise.domain.model.CookCard
import com.patchfox.mise.domain.usecase.GetCookCardByIdUseCase
import com.patchfox.mise.domain.usecase.GetCookCardHistoryUseCase
import com.patchfox.mise.domain.usecase.GetCurrentCookCardUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class RecipeUiState(
    /** The active card: the current week's card, or — for a detail/instructions route
     *  carrying a cookCardId — the past card it names. */
    val card: CookCard? = null,
    /** Every cook card (newest-first), for the "Previous Recipes" section. */
    val history: List<CookCard> = emptyList(),
    val loading: Boolean = true,
)

@HiltViewModel
class RecipeViewModel @Inject constructor(
    getCurrent: GetCurrentCookCardUseCase,
    getById: GetCookCardByIdUseCase,
    getHistory: GetCookCardHistoryUseCase,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    /** Set when navigated to a specific past card (detail / instructions); null for the list. */
    private val cookCardId: String? = savedStateHandle["cookCardId"]

    private val currentCard = getCurrent().map { load ->
        (load as? CookCardLoadState.Success)?.card ?: (load as? CookCardLoadState.Error)?.cached
    }

    private val history = MutableStateFlow<List<CookCard>>(emptyList())
    private val pastCard = MutableStateFlow<CookCard?>(null)

    init {
        if (cookCardId == null) {
            // List route: load history for the "Previous Recipes" section.
            viewModelScope.launch {
                runCatching { getHistory() }.getOrNull()?.let { history.value = it }
            }
        } else {
            // Detail / instructions route: resolve just the one past card.
            viewModelScope.launch {
                pastCard.value = runCatching { getById(cookCardId) }.getOrNull()
            }
        }
    }

    val state: StateFlow<RecipeUiState> =
        combine(currentCard, history, pastCard) { current, hist, past ->
            val active = if (cookCardId != null) past else current
            RecipeUiState(card = active, history = hist, loading = active == null)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RecipeUiState())
}
