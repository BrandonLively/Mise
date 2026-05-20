package com.patchfox.mise.data.auth

import android.app.Activity
import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.patchfox.mise.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

@Singleton
class AuthRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : AuthRepository {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val _currentUser = MutableStateFlow(firebaseAuth.currentUser?.toMiseUser())
    override val currentUser: StateFlow<MiseUser?> = _currentUser.asStateFlow()

    init {
        firebaseAuth.addAuthStateListener { auth ->
            _currentUser.value = auth.currentUser?.toMiseUser()
        }
    }

    override suspend fun signInWithGoogle(activity: Activity): SignInResult {
        val webClientId = context.getString(R.string.default_web_client_id)
        if (webClientId.isBlank()) {
            return SignInResult.Failure(
                "Sign-in not configured. Add the project's web client id to google-services.json " +
                    "(Firebase console → Authentication → Google provider) and rebuild.",
            )
        }

        val googleIdOption = GetGoogleIdOption.Builder()
            .setServerClientId(webClientId)
            .setFilterByAuthorizedAccounts(false)
            .setAutoSelectEnabled(false)
            .build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val cm = CredentialManager.create(activity)
        return try {
            val response = cm.getCredential(activity, request)
            val cred = response.credential
            if (cred is CustomCredential &&
                cred.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val idCred = GoogleIdTokenCredential.createFrom(cred.data)
                val authCred = GoogleAuthProvider.getCredential(idCred.idToken, null)
                val result = firebaseAuth.signInWithCredential(authCred).await()
                val user = result.user?.toMiseUser()
                if (user != null) SignInResult.Success(user)
                else SignInResult.Failure("Firebase returned no user")
            } else {
                SignInResult.Failure("Unexpected credential type from Credential Manager")
            }
        } catch (_: GetCredentialCancellationException) {
            SignInResult.Cancelled
        } catch (e: GetCredentialException) {
            SignInResult.Failure(e.message ?: "Credential Manager error")
        } catch (e: GoogleIdTokenParsingException) {
            SignInResult.Failure("Couldn't parse Google ID token: ${e.message}")
        } catch (e: Exception) {
            SignInResult.Failure(e.message ?: "Sign-in failed")
        }
    }

    override suspend fun signOut() {
        firebaseAuth.signOut()
    }

    private fun FirebaseUser.toMiseUser() = MiseUser(uid = uid, email = email, displayName = displayName)
}
