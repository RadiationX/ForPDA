package forpdateam.ru.forpda.model.data.remote.api.common

import android.util.Log
import forpdateam.ru.forpda.entity.common.MessageCounters
import forpdateam.ru.forpda.model.data.remote.ParserPatterns
import forpdateam.ru.forpda.model.data.remote.parser.BaseParser
import forpdateam.ru.forpda.model.data.storage.PatternProvider
import javax.inject.Inject

class GlobalParser @Inject constructor(
    private val patternProvider: PatternProvider
) : BaseParser() {

    private val scope = ParserPatterns.Global

    fun parseMetaTags(response: String): List<MetaTag> {
        return patternProvider
            .getRegexParser(scope.scope, scope.meta_tags)
            .map(response) {
                MetaTag(
                    target = it.require(1),
                    type = it.require(2),
                    content = it.require(3)
                )
            }
    }

    fun parseCounters(response: String): MessageCounters? {
        return runCatching {
            patternProvider
                .getRegexParser(scope.scope, scope.counters)
                .mapOnce(response) {
                    MessageCounters(
                        mentions = it.get(1)?.toInt() ?: 0,
                        favorites = it.get(2)?.toInt() ?: 0,
                        qms = it.get(3)?.toInt() ?: 0
                    )
                }
        }.onFailure {
            Log.d("WATAFUCK", response, it)
        }.getOrNull()

    }

    fun parseForumError(response: String): String? {
        return patternProvider
            .getRegexParser(scope.scope, scope.forum_error)
            .mapOnce(response) { it.require(1).fromHtml() }
    }

    data class MetaTag(
        val target: String,
        val type: String,
        val content: String
    )
}