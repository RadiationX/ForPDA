package forpdateam.ru.forpda.model.data.remote.api.auth

import android.util.Log
import forpdateam.ru.forpda.common.ApiRequest
import forpdateam.ru.forpda.entity.remote.auth.AuthCaptcha
import forpdateam.ru.forpda.entity.remote.auth.AuthForm
import forpdateam.ru.forpda.extensions.coRunCatching
import forpdateam.ru.forpda.model.AuthHolder
import forpdateam.ru.forpda.model.data.remote.WebClient
import javax.inject.Inject

/**
 * Created by radiationx on 25.03.17.
 */

class AuthApi @Inject constructor(
    private val webClient: WebClient,
    private val authParser: AuthParser,
    private val authHolder: AuthHolder
) {

    suspend fun getCaptcha(): AuthCaptcha {
        val response = webClient.request(ApiRequest.Forum.Auth.GetCaptcha)

        if (response.body.isEmpty())
            throw Exception("Page empty!")

        if (checkLogin(response.body))
            throw Exception("You already logged")

        return authParser.parseCaptcha(response.body)
    }

    suspend fun login(captcha: AuthCaptcha, form: AuthForm) {
        val response = webClient.request(ApiRequest.Forum.Auth.Login(captcha, form))
        val errors = authParser.parseErrors(response.body)
        if (errors != null) {
            throw Exception(errors)
        }
        if (!checkLogin(response.body)) {
            throw Exception("Ошибка при проверке авторизации")
        }
    }

    suspend fun logout() {
        coRunCatching {
            val response = webClient.request(ApiRequest.Forum.Auth.Logout(authHolder.getAuthKey()))
            if (authParser.parseAlreadyLoggedOut(response.body)) {
                throw Exception("You already logout")
            }
        }.onFailure {
            Log.e(TAG, "logout", it)
        }
        authHolder.clearData()
    }

    private fun checkLogin(response: String): Boolean {
        val authKey = authParser.parseAuthKey(response)
        if (authKey != null) {
            authHolder.setAuthKey(authKey)
            return true
        }
        return false
    }

    companion object {
        private const val TAG = "AuthApi"
    }

}
