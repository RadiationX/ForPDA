package forpdateam.ru.forpda.model.data.remote.api.favorites

import forpdateam.ru.forpda.entity.remote.favorites.FavData
import forpdateam.ru.forpda.entity.remote.favorites.FavItem
import forpdateam.ru.forpda.entity.remote.others.pagination.Pagination
import forpdateam.ru.forpda.model.data.remote.ParserPatterns
import forpdateam.ru.forpda.model.data.remote.parser.BaseParser
import forpdateam.ru.forpda.model.data.storage.IPatternProvider
import java.util.Locale
import java.util.regex.Matcher

class FavoritesParser(
    private val patternProvider: IPatternProvider
) : BaseParser() {

    private val scope = ParserPatterns.Favorites

    fun parseFavorites(response: String): FavData {
        val list = patternProvider
            .getPattern(scope.scope, scope.main)
            .matcher(response)
            .map { matcher ->
                parseFavItem(matcher)
            }
        return FavData(
            items = list,
            pagination = Pagination.parseForum(response),
            sorting = Sorting.parse(response)
        )
    }

    fun checkIsComplete(result: String): Boolean {
        return patternProvider
            .getPattern(scope.scope, scope.check_action)
            .matcher(result)
            .find()
    }

    private fun parseFavItem(matcher: Matcher): FavItem = FavItemBuilder().apply {
        isForum = matcher.group(19) != null

        favId = matcher.group(1).toInt()
        trackType = matcher.group(2)
        isPin = matcher.group(3) == "1"

        matcher.group(4)?.also {
            infoColor = it
        }

        matcher.group(5)?.also {
            isNew = it.contains("+")
            isPoll = it.contains("^")
            isClosed = it.contains("Х")
        }

        matcher.group(6).toInt().also {
            if (isForum) {
                forumId = it
            } else {
                topicId = it
            }
        }

        isNew = matcher.group(7) != null
        topicTitle = matcher.group(8).fromHtml()

        if (isForum) {
            date = matcher.group(19)
            lastUserId = matcher.group(20).toInt()
            lastUserNick = matcher.group(21).fromHtml()
        } else {
            matcher.group(9)?.also {
                stParam = it.toInt()
                pages = stParam / 20 + 1
            }
            matcher.group(10)?.also {
                desc = it.fromHtml()
            }

            forumId = matcher.group(12).toInt()
            forumTitle = matcher.group(13).fromHtml()
            authorId = matcher.group(14).toInt()
            authorUserNick = matcher.group(15).fromHtml()
            lastUserId = matcher.group(16).toInt()
            lastUserNick = matcher.group(17).fromHtml()
            date = matcher.group(18)

            matcher.group(22)?.also {
                curatorId = it.toInt()
                curatorNick = matcher.group(23).fromHtml()
            }

            subType = matcher.group(24).trim().lowercase(Locale.getDefault())
        }
    }.build()

    private class FavItemBuilder {
        var favId: Int = 0
        var topicId: Int = 0
        var forumId: Int = 0
        var authorId: Int = 0
        var lastUserId: Int = 0
        var stParam: Int = 0
        var pages: Int = 0
        var curatorId: Int = 0
        var trackType: String? = null
        var infoColor: String? = null
        var topicTitle: String? = null
        var forumTitle: String? = null
        var authorUserNick: String? = null
        var lastUserNick: String? = null
        var date: String? = null
        var desc: String? = null
        var curatorNick: String? = null
        var subType: String? = null
        var isPin = false
        var isForum = false
        var isNew: Boolean = false
        var isPoll: Boolean = false
        var isClosed: Boolean = false

        fun build(): FavItem {
            return FavItem(
                favId = favId,
                topicId = topicId,
                forumId = forumId,
                authorId = authorId,
                lastUserId = lastUserId,
                stParam = stParam,
                pages = pages,
                curatorId = curatorId,
                trackType = trackType,
                infoColor = infoColor,
                topicTitle = topicTitle,
                forumTitle = forumTitle,
                authorUserNick = authorUserNick,
                lastUserNick = lastUserNick,
                date = date,
                desc = desc,
                curatorNick = curatorNick,
                subType = subType,
                isPin = isPin,
                isForum = isForum,
                isNew = isNew,
                isPoll = isPoll,
                isClosed = isClosed
            )
        }
    }
}
