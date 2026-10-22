package com.google.firebase.example.makeitso.data.repository

import android.content.Context
import androidx.credentials.CustomCredential
import androidx.credentials.PasswordCredential
import androidx.credentials.PublicKeyCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.example.makeitso.data.datasource.AuthRemoteDataSource
import com.google.firebase.example.makeitso.data.datasource.CredentialDataSource
import com.google.firebase.example.makeitso.data.datasource.PasskeyRemoteDataSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val authRemoteDataSource: AuthRemoteDataSource,
    private val credentialDataSource: CredentialDataSource,
    private val passkeyRemoteDataSource: PasskeyRemoteDataSource
) {
    val currentUser: FirebaseUser? get() = authRemoteDataSource.currentUser
    val currentUserIdFlow: Flow<String?> = authRemoteDataSource.currentUserIdFlow

    private var passkeyRequest: String? = null

    suspend fun signIn(email: String, password: String) {
        authRemoteDataSource.signIn(email, password)
    }

    suspend fun signUp(context: Context, email: String, password: String) {
        authRemoteDataSource.signUp(email, password)
        runCatching { credentialDataSource.savePassword(context, email, password) }
    }

    suspend fun preparePasskey() {
        passkeyRequest = runCatching { passkeyRemoteDataSource.loginOptions() }.getOrNull()
    }

    suspend fun signInWithSavedCredential(context: Context) {
        when (val credential = credentialDataSource.getCredential(context, passkeyRequest)) {
            is PublicKeyCredential -> {
                val token = passkeyRemoteDataSource.loginVerify(credential.authenticationResponseJson)
                authRemoteDataSource.signInWithCustomToken(token)
            }
            is PasswordCredential -> authRemoteDataSource.signIn(credential.id, credential.password)
            is CustomCredential -> {
                val google = GoogleIdTokenCredential.createFrom(credential.data)
                authRemoteDataSource.signInWithGoogle(google.idToken)
            }
            else -> error("Unsupported credential")
        }
    }

    suspend fun createPasskey(context: Context) {
        val requestJson = passkeyRemoteDataSource.registerOptions()
        val response = credentialDataSource.createPasskey(context, requestJson)
        passkeyRemoteDataSource.registerVerify(response)
    }

    suspend fun signInProvider(): String? {
        return authRemoteDataSource.signInProvider()
    }

    suspend fun signOut(context: Context) {
        authRemoteDataSource.signOut()
        credentialDataSource.clearState(context)
    }

    suspend fun deleteAccount(context: Context) {
        authRemoteDataSource.deleteAccount()
        credentialDataSource.clearState(context)
    }
}
