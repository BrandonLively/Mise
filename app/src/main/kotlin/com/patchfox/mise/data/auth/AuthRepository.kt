package com.patchfox.mise.data.auth

import android.app.Activity
import kotlinx.coroutines.flow.StateFlow

data class MiseUser(
    val uid: String,
    val email: String?,
    val displayName: String?,
)

sealed interface SignInResult {
    data class Success(val user: MiseUser) : SignInResult
    data object Cancelled : SignInResult
    data class Failure(val message: String) : SignInResult
}

interface AuthRepository {
    val currentUser: StateFlow<MiseUser?>

    /** Launch the Google sign-in flow. Must be called from an Activity context. */
    suspend fun signInWithGoogle(activity: Activity): SignInResult

    suspend fun signOut()
}
