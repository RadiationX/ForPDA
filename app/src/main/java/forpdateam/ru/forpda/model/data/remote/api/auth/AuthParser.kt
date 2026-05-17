package forpdateam.ru.forpda.model.data.remote.api.auth

import forpdateam.ru.forpda.entity.remote.auth.AuthCaptcha
import forpdateam.ru.forpda.model.data.remote.ParserPatterns
import forpdateam.ru.forpda.model.data.remote.parser.BaseParser
import forpdateam.ru.forpda.model.data.storage.PatternProvider
import javax.inject.Inject

class AuthParser @Inject constructor(
    private val patternProvider: PatternProvider
) : BaseParser() {

    private val scope = ParserPatterns.Auth

    fun parseCaptcha(response: String): AuthCaptcha {
        return patternProvider
            .getRegexParser(scope.scope, scope.captcha)
            .mapOnce(response) {
                AuthCaptcha(
                    captchaTime = it.require(1),
                    captchaSig = it.require(2),
                    captchaImageUrl = it.require(3)
                )
            }
            ?: throw Exception("Form Not Found")
    }

    fun parseErrors(response: String): String? {
        return patternProvider
            .getRegexParser(scope.scope, scope.errors_list)
            .mapOnce(response) {
                it.require(1)
                    .fromHtml()
                    .replace("\\.".toRegex(), ".\n")
                    .trim()
            }
    }

    fun parseAlreadyLoggedOut(response: String): Boolean {
        return patternProvider
            .getRegexParser(scope.scope, scope.already_logged_out)
            .mapOnce(response) { true }
            ?: false
    }

    fun parseAuthKey(response: String): String? {
        return patternProvider
            .getRegexParser(scope.scope, scope.check_login)
            .mapOnce(response) { it.require(2) }
    }
}
