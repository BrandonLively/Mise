package com.patchfox.mise.ui.screen.login

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.patchfox.mise.data.auth.AuthRepository
import com.patchfox.mise.data.auth.SignInResult
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val signingIn: Boolean = false,
    val errorMessage: String? = null,
    val signedIn: Boolean = false,
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val auth: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState(signedIn = auth.currentUser.value != null))
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    fun signIn(activity: Activity) {
        if (_state.value.signingIn) return
        _state.value = _state.value.copy(signingIn = true, errorMessage = null)
        viewModelScope.launch {
            when (val result = auth.signInWithGoogle(activity)) {
                is SignInResult.Success ->
                    _state.value = LoginUiState(signedIn = true)
                is SignInResult.Cancelled ->
                    _state.value = _state.value.copy(signingIn = false)
                is SignInResult.Failure ->
                    _state.value = _state.value.copy(signingIn = false, errorMessage = result.message)
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(errorMessage = null)
    }
}
