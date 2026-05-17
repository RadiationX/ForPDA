package forpdateam.ru.forpda.model.data.remote.api.auth

import android.util.Log
import forpdateam.ru.forpda.entity.remote.auth.AuthCaptcha
import forpdateam.ru.forpda.entity.remote.auth.AuthForm
import forpdateam.ru.forpda.extensions.coRunCatching
import forpdateam.ru.forpda.model.AuthHolder
import forpdateam.ru.forpda.model.data.remote.WebClient
import forpdateam.ru.forpda.model.data.remote.api.NetworkRequest
import java.net.URLEncoder
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
        val response = webClient.get(AUTH_BASE_URL)

        if (response.body.isEmpty())
            throw Exception("Page empty!")

        if (checkLogin(response.body))
            throw Exception("You already logged")

        return authParser.parseCaptcha(response.body)
    }

    suspend fun login(captcha: AuthCaptcha, form: AuthForm) {
        val builder = NetworkRequest.Builder()
            .url(AUTH_BASE_URL)
            .formHeader("captcha-time", requireNotNull(captcha.captchaTime))
            .formHeader("captcha-sig", requireNotNull(captcha.captchaSig))
            .formHeader("captcha", requireNotNull(form.captcha))
            .formHeader("return", MINIMAL_PAGE_URL)
            .formHeader("login", URLEncoder.encode(form.nick, "windows-1251"), true)
            .formHeader("password", URLEncoder.encode(form.password, "windows-1251"), true)
            .formHeader("remember", "1")
            .formHeader("hidden", if (form.isHidden) "1" else "0")

        val response = webClient.request(builder.build())
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
            val response = webClient.get("https://4pda.to/forum/index.php?act=logout&CODE=03&k=" + authHolder.getAuthKey().orEmpty())
            if (authParser.parseAlreadyLoggedOut(response.body)) {
                throw Exception("You already logout")
            }
            checkLogin(webClient.get(MINIMAL_PAGE_URL).body)
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
        private const val AUTH_BASE_URL = "https://4pda.to/forum/index.php?act=auth"
        private const val MINIMAL_PAGE_URL = "https://4pda.to/forum/index.php?showforum=200#afterauth"
    }

}
