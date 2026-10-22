package com.google.firebase.example.makeitso.ui.profile

import android.content.Context
import com.google.firebase.example.makeitso.MainViewModel
import com.google.firebase.example.makeitso.data.model.ErrorMessage
import com.google.firebase.example.makeitso.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class ProfileInfo(
    val method: String = "",
    val email: String = "",
    val name: String = "",
    val uid: String = "",
    val emailVerified: Boolean = false
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : MainViewModel() {
    private val _profile = MutableStateFlow(ProfileInfo())
    val profile: StateFlow<ProfileInfo>
        get() = _profile.asStateFlow()

    fun loadProfile() {
        launchCatching {
            val user = authRepository.currentUser ?: return@launchCatching
            _profile.value = ProfileInfo(
                method = methodTitle(authRepository.signInProvider()),
                email = user.email.orEmpty(),
                name = user.displayName.orEmpty(),
                uid = user.uid,
                emailVerified = user.isEmailVerified
            )
        }
    }

    fun signOut(context: Context, showErrorSnackbar: (ErrorMessage) -> Unit) {
        launchCatching(showErrorSnackbar) {
            authRepository.signOut(context)
        }
    }

    fun createPasskey(context: Context, showErrorSnackbar: (ErrorMessage) -> Unit) {
        launchCatching(showErrorSnackbar) {
            authRepository.createPasskey(context)
        }
    }

    fun deleteAccount(context: Context, showErrorSnackbar: (ErrorMessage) -> Unit) {
        launchCatching(showErrorSnackbar) {
            authRepository.deleteAccount(context)
        }
    }

    private fun methodTitle(provider: String?): String {
        return when (provider) {
            "password" -> "Email / password"
            "google.com" -> "Google"
            "custom" -> "Passkey"
            else -> provider.orEmpty()
        }
    }
}
