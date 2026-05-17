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

    suspend fun parseCaptcha(response: String): AuthCaptcha = patternProvider
        .getRegexParser(scope.scope, scope.captcha)
        .mapOnce(response) {
            AuthCaptcha(
                captchaTime = it.require(1),
                captchaSig = it.require(2),
                captchaImageUrl = it.require(3)
            )
        } ?: throw Exception("Form Not Found")
}
