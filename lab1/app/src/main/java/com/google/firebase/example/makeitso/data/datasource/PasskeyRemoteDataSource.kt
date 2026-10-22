package com.google.firebase.example.makeitso.data.datasource

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.example.makeitso.R
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class PasskeyRemoteDataSource @Inject constructor(
    @ApplicationContext private val context: Context,
    private val auth: FirebaseAuth
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .callTimeout(15, TimeUnit.SECONDS)
        .build()

    suspend fun registerOptions(): String = post("register-options", authorized = true).getString("options")

    suspend fun registerVerify(response: String) {
        post("register-verify", response, authorized = true)
    }

    suspend fun loginOptions(): String = post("login-options").getString("options")

    suspend fun loginVerify(response: String): String =
        post("login-verify", response).getString("token")

    private suspend fun post(
        path: String,
        response: String? = null,
        authorized: Boolean = false
    ): JSONObject {
        val idToken = if (authorized) auth.currentUser!!.getIdToken(false).await().token else null
        val url = "${context.getString(R.string.passkey_server_url)}/api/$path"

        return withContext(Dispatchers.IO) {
            retryOnIoError { request(url, idToken, response) }
        }
    }

    private fun request(url: String, idToken: String?, response: String?): JSONObject {
        val body = JSONObject().apply { response?.let { put("response", it) } }
            .toString()
            .toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url(url)
            .post(body)
            .apply { idToken?.let { header("Authorization", "Bearer $it") } }
            .build()

        client.newCall(request).execute().use { result ->
            val json = JSONObject(result.body.string())
            if (!result.isSuccessful) error(json.optString("error", "Server error"))
            return json
        }
    }

    private fun <T> retryOnIoError(block: () -> T): T {
        for (attempt in 1 until ATTEMPTS) {
            try {
                return block()
            } catch (e: IOException) {
                Thread.sleep(RETRY_DELAY_MS)
            }
        }
        return block()
    }

    companion object {
        private const val ATTEMPTS = 3
        private const val RETRY_DELAY_MS = 300L
    }
}
