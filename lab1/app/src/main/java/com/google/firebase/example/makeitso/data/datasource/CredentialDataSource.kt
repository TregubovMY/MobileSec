package com.google.firebase.example.makeitso.data.datasource

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CreatePasswordRequest
import androidx.credentials.CreatePublicKeyCredentialRequest
import androidx.credentials.CreatePublicKeyCredentialResponse
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetPasswordOption
import androidx.credentials.GetPublicKeyCredentialOption
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.firebase.example.makeitso.R
import javax.inject.Inject

class CredentialDataSource @Inject constructor() {
    suspend fun savePassword(context: Context, email: String, password: String) {
        CredentialManager.create(context)
            .createCredential(context, CreatePasswordRequest(email, password))
    }

    suspend fun createPasskey(context: Context, requestJson: String): String {
        val response = CredentialManager.create(context)
            .createCredential(context, CreatePublicKeyCredentialRequest(requestJson))
        return (response as CreatePublicKeyCredentialResponse).registrationResponseJson
    }

    suspend fun getCredential(context: Context, passkeyRequestJson: String?): Credential {
        val googleOption = GetGoogleIdOption.Builder()
            .setServerClientId(context.getString(R.string.default_web_client_id))
            .setFilterByAuthorizedAccounts(false)
            .build()
        val options = listOfNotNull(
            GetPasswordOption(),
            googleOption,
            passkeyRequestJson?.let { GetPublicKeyCredentialOption(it) }
        )
        val request = GetCredentialRequest(options)
        return CredentialManager.create(context).getCredential(context, request).credential
    }

    suspend fun clearState(context: Context) {
        CredentialManager.create(context).clearCredentialState(ClearCredentialStateRequest())
    }
}
