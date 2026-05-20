package com.patchfox.mise.ui.screen.summary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patchfox.mise.data.cookcard.CookCardLoadState
import com.patchfox.mise.data.state.DaysDivisorRepository
import com.patchfox.mise.data.state.SummaryInputRepository
import com.patchfox.mise.domain.model.CookCard
import com.patchfox.mise.domain.model.RecipeId
import com.patchfox.mise.domain.usecase.GetCurrentCookCardUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SummaryUiState(
    val card: CookCard? = null,
    val inputs: Map<SummaryInputRepository.Key, String> = emptyMap(),
    val days: Int = DaysDivisorRepository.DEFAULT_DAYS,
    val loading: Boolean = true,
)

@HiltViewModel
class SummaryViewModel @Inject constructor(
    getCard: GetCurrentCookCardUseCase,
    private val inputRepo: SummaryInputRepository,
    private val divisorRepo: DaysDivisorRepository,
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<SummaryUiState> = getCard()
        .flatMapLatest { load ->
            val card = (load as? CookCardLoadState.Success)?.card
                ?: (load as? CookCardLoadState.Error)?.cached
            if (card == null) {
                flowOf(SummaryUiState(loading = load is CookCardLoadState.Loading))
            } else {
                combine(
                    inputRepo.observe(card.id),
                    divisorRepo.observe(card.id),
                ) { inputs, days ->
                    SummaryUiState(card = card, inputs = inputs, days = days, loading = false)
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SummaryUiState())

    fun setWeight(recipeId: RecipeId, label: String, raw: String) {
        val card = state.value.card ?: return
        val sanitized = sanitizeNumeric(raw)
        viewModelScope.launch {
            inputRepo.set(card.id, SummaryInputRepository.Key(recipeId, label), sanitized)
        }
    }

    fun setDays(days: Int) {
        val card = state.value.card ?: return
        viewModelScope.launch { divisorRepo.set(card.id, days) }
    }

    private fun sanitizeNumeric(s: String): String {
        if (s.isEmpty()) return s
        val cleaned = s.filterIndexed { idx, c -> c.isDigit() || (c == '.' && s.indexOf('.') == idx) }
        return cleaned
    }
}
