package forpdateam.ru.forpda.model.data.remote.api.auth

import forpdateam.ru.forpda.entity.remote.auth.AuthForm
import forpdateam.ru.forpda.model.data.remote.ParserPatterns
import forpdateam.ru.forpda.model.data.remote.parser.BaseParser
import forpdateam.ru.forpda.model.data.storage.IPatternProvider

class AuthParser(
        private val patternProvider: IPatternProvider
) : BaseParser() {

    private val scope = ParserPatterns.Auth

    fun parseForm(response: String): AuthForm = patternProvider
            .getPattern(scope.scope, scope.captcha)
            .matcher(response)
            .mapOnce {
                AuthForm().apply {
                    captchaTime = it.group(1)
                    captchaSig = it.group(2)
                    captchaImageUrl = it.group(3)
                }
            } ?: throw Exception("Form Not Found")
}
