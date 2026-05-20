package com.patchfox.mise.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patchfox.mise.data.cookcard.CookCardLoadState
import com.patchfox.mise.data.state.PrepCheckRepository
import com.patchfox.mise.domain.model.CookCard
import com.patchfox.mise.domain.model.PrepTaskId
import com.patchfox.mise.domain.usecase.GetCurrentCookCardUseCase
import com.patchfox.mise.domain.usecase.ParsePrepTasksUseCase
import com.patchfox.mise.domain.usecase.PrepTask
import com.patchfox.mise.domain.usecase.RefreshCookCardUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val loading: Boolean = true,
    val card: CookCard? = null,
    val prepTasks: List<PrepTask> = emptyList(),
    val checkedPrep: Set<PrepTaskId> = emptySet(),
    val error: String? = null,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    getCard: GetCurrentCookCardUseCase,
    private val refresh: RefreshCookCardUseCase,
    private val parsePrep: ParsePrepTasksUseCase,
    private val prepCheck: PrepCheckRepository,
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<HomeUiState> = getCard()
        .flatMapLatest { load ->
            val card = (load as? CookCardLoadState.Success)?.card
                ?: (load as? CookCardLoadState.Error)?.cached
            if (card == null) {
                flowOf(
                    HomeUiState(
                        loading = load is CookCardLoadState.Loading,
                        error = (load as? CookCardLoadState.Error)?.message,
                    ),
                )
            } else {
                val tasks = parsePrep(card)
                prepCheck.observe(card.id).map { checked ->
                    HomeUiState(
                        loading = false,
                        card = card,
                        prepTasks = tasks,
                        checkedPrep = checked,
                        error = (load as? CookCardLoadState.Error)?.message,
                    )
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    fun togglePrep(taskId: PrepTaskId, checked: Boolean) {
        val cardId = state.value.card?.id ?: return
        viewModelScope.launch { prepCheck.toggle(cardId, taskId, checked) }
    }

    fun refreshNow() {
        viewModelScope.launch { refresh() }
    }
}
