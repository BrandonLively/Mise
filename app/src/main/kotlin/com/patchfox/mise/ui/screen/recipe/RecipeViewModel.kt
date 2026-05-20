package com.patchfox.mise.ui.screen.recipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patchfox.mise.data.cookcard.CookCardLoadState
import com.patchfox.mise.domain.model.CookCard
import com.patchfox.mise.domain.usecase.GetCurrentCookCardUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class RecipeUiState(
    val card: CookCard? = null,
    val loading: Boolean = true,
)

@HiltViewModel
class RecipeViewModel @Inject constructor(
    getCard: GetCurrentCookCardUseCase,
) : ViewModel() {
    val state: StateFlow<RecipeUiState> = getCard()
        .map { load ->
            val card = (load as? CookCardLoadState.Success)?.card
                ?: (load as? CookCardLoadState.Error)?.cached
            RecipeUiState(card = card, loading = card == null && load is CookCardLoadState.Loading)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RecipeUiState())
}
