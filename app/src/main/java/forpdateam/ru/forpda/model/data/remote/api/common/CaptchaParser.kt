package forpdateam.ru.forpda.model.data.remote.api.common

import forpdateam.ru.forpda.model.data.remote.ParserPatterns
import forpdateam.ru.forpda.model.data.remote.parser.BaseParser
import forpdateam.ru.forpda.model.data.storage.PatternProvider
import javax.inject.Inject

class CaptchaParser @Inject constructor(
    private val patternProvider: PatternProvider
) : BaseParser() {

    private val scope = ParserPatterns.Captcha

    fun checkRedirect(url: String): Boolean {
        return patternProvider
            .getRegexParser(scope.scope, scope.redirect)
            .mapOnce(url) { true }
            ?: false
    }
}