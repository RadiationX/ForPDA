package forpdateam.ru.forpda.model.data.remote.api.auth

import forpdateam.ru.forpda.App
import forpdateam.ru.forpda.entity.remote.auth.AuthForm
import forpdateam.ru.forpda.model.data.remote.IWebClient
import forpdateam.ru.forpda.model.data.remote.api.ApiUtils
import forpdateam.ru.forpda.model.data.remote.api.NetworkRequest
import java.net.URLEncoder
import java.util.regex.Pattern

/**
 * Created by radiationx on 25.03.17.
 */

class AuthApi(
        private val webClient: IWebClient,
        private val authParser: AuthParser
) {

    fun getForm(): AuthForm {
        val response = webClient.get(AUTH_BASE_URL)

        if (response.body.isNullOrEmpty())
            throw Exception("Page empty!")

        if (checkLogin(response.body))
            throw Exception("You already logged")

        return authParser.parseForm(response.body)
    }

    fun login(form: AuthForm): AuthForm {
        val builder = NetworkRequest.Builder()
                .url(AUTH_BASE_URL)
                .formHeader("captcha-time", form.captchaTime)
                .formHeader("captcha-sig", form.captchaSig)
                .formHeader("captcha", form.captcha)
                .formHeader("return", IWebClient.MINIMAL_PAGE)
                .formHeader("login", URLEncoder.encode(form.nick, "windows-1251"), true)
                .formHeader("password", URLEncoder.encode(form.password, "windows-1251"), true)
                .formHeader("remember", "1")
                .formHeader("hidden", if (form.isHidden) "1" else "0")

        val response = webClient.request(builder.build())
        val matcher = errorPattern.matcher(response.body)
        if (matcher.find()) {
            throw Exception(ApiUtils.fromHtml(matcher.group(1)).replace("\\.".toRegex(), ".\n").trim())
        }
        if (!checkLogin(response.body)) {
            throw Exception("Ошибка при проверке авторизации")
        }
        return form
    }

    fun logout(): Boolean {
        val response = webClient.get("https://4pda.ru/forum/index.php?act=logout&CODE=03&k=" + webClient.authKey)

        val matcher = Pattern.compile("wr va-m text").matcher(response.body)
        if (matcher.find())
            throw Exception("You already logout")

        webClient.clearCookies()
        App.get().preferences.edit().remove("cookie_member_id").remove("cookie_pass_hash").apply()

        return !checkLogin(webClient.get(IWebClient.MINIMAL_PAGE).body)
    }

    private fun checkLogin(response: String): Boolean {
        val matcher = Pattern.compile("<i class=\"icon-profile\">[\\s\\S]*?<ul class=\"dropdown-menu\">[\\s\\S]*?showuser=(\\d+)\"[\\s\\S]*?action=logout[^\"]*?k=([a-z0-9]{32})").matcher(response)
        if (matcher.find()) {
            App.get().preferences.edit().putString("auth_key", matcher.group(2)).apply()
            return true
        }
        return false
    }

    companion object {
        val AUTH_BASE_URL = "https://4pda.ru/forum/index.php?act=auth"
        private val errorPattern = Pattern.compile("errors-list\">([\\s\\S]*?)</ul>")
    }

}
