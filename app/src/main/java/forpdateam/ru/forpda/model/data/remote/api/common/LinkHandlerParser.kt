package forpdateam.ru.forpda.model.data.remote.api.common

import forpdateam.ru.forpda.model.data.remote.ParserPatterns
import forpdateam.ru.forpda.model.data.remote.parser.BaseParser
import forpdateam.ru.forpda.model.data.storage.PatternProvider
import java.net.URLDecoder
import javax.inject.Inject

class LinkHandlerParser @Inject constructor(
    private val patternProvider: PatternProvider
) : BaseParser() {

    private val scope = ParserPatterns.LinkHandler

    fun forumMedia(url: String): ForumMedia? {
        return patternProvider
            .getRegexParser(scope.scope, scope.forum_media)
            .mapOnce(url) { match ->
                val fileName = match.require(1).let {
                    try {
                        URLDecoder.decode(it, "CP1251")
                    } catch (_: Exception) {
                        it
                    }
                }
                ForumMedia(
                    fileName = fileName,
                    extension = match.require(2)
                )
            }
    }

    fun isSupportImage(url: String): Boolean {
        return patternProvider
            .getRegexParser(scope.scope, scope.support_images)
            .mapOnce(url) { true }
            ?: false
    }


    data class ForumMedia(
        val fileName: String,
        val extension: String
    )
}