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

    fun basicMatches(url: String): Boolean {
        return patternProvider
            .getPattern(scope.scope, scope.basic)
            .matcher(url)
            .matches()
    }

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

    fun forumLoFi(url: String): ForumLoFi? {
        return patternProvider
            .getRegexParser(scope.scope, scope.forum_lofi)
            .mapOnce(url) {
                ForumLoFi(
                    type = it.require(1),
                    id = it.require(2),
                    st = it.get(3)
                )
            }
    }

    fun site(url: String): Site? {
        return patternProvider
            .getRegexParser(scope.scope, scope.site)
            .mapOnce(url) {
                Site(
                    articleId = it.require(2).toInt(),
                    commentId = it.get(3)?.toInt()
                )
            }

    }

    data class ForumMedia(
        val fileName: String,
        val extension: String
    )

    data class ForumLoFi(
        val type: String,
        val id: String,
        val st: String?
    )

    data class Site(
        val articleId: Int,
        val commentId: Int?
    )
}