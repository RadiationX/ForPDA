package forpdateam.ru.forpda.model.data.remote.api.favorites

import forpdateam.ru.forpda.entity.remote.favorites.Favorite
import forpdateam.ru.forpda.entity.remote.favorites.FavoritesData
import forpdateam.ru.forpda.entity.remote.others.pagination.Pagination
import forpdateam.ru.forpda.entity.remote.others.user.User
import forpdateam.ru.forpda.model.data.remote.ParserPatterns
import forpdateam.ru.forpda.model.data.remote.parser.BaseParser
import forpdateam.ru.forpda.model.data.storage.IPatternProvider
import forpdateam.ru.forpda.model.data.storage.parser.ParserPattern

class FavoritesParser(
    private val patternProvider: IPatternProvider
) : BaseParser() {

    private val scope = ParserPatterns.Favorites

    fun parseFavorites(response: String): FavoritesData {
        val list = patternProvider
            .getParserPattern(scope.scope, scope.main)
            .map(response) { matcher ->
                parseFavorite(matcher)
            }
        return FavoritesData(
            items = list,
            pagination = Pagination.parseForum(response),
            sorting = Sorting.parse(response)
        )
    }

    fun checkIsComplete(result: String): Boolean {
        return patternProvider
            .getParserPattern(scope.scope, scope.check_action)
            .mapOnce(result) { true }
            ?: false
    }

    private fun parseFavorite(matcher: ParserPattern.Matcher): Favorite {
        val isTopic = matcher.get(19) == null
        return if (isTopic) {
            parseFavoriteTopic(matcher)
        } else {
            parseFavoriteForum(matcher)
        }
    }

    private fun parseFavoriteTopic(matcher: ParserPattern.Matcher): Favorite.Topic {
        val flagsGroup = matcher.get(5)
        return Favorite.Topic(
            favId = matcher.require(1).toInt(),
            topicId = matcher.require(6).toInt(),
            trackType = matcher.require(2),
            isPin = matcher.require(3) == "1",
            isNew = flagsGroup?.contains("+") == true,
            isPoll = flagsGroup?.contains("^") == true,
            isClosed = flagsGroup?.contains("Х") == true,
            title = matcher.require(8).fromHtml(),
            stParam = matcher.get(9)?.toInt(),
            desc = matcher.get(10)?.fromHtml(),
            forumId = matcher.require(12).toInt(),
            forumTitle = matcher.require(13).fromHtml(),
            author = User.required(
                matcher.require(14).toInt(),
                matcher.require(15).fromHtml()
            ),
            lastUser = User.required(
                matcher.require(16).toInt(),
                matcher.require(17).fromHtml()
            ),
            date = matcher.require(18),
            curator = matcher.get(22)?.let {
                User.required(
                    it.toInt(),
                    matcher.require(23).fromHtml()
                )
            }
        )
    }

    private fun parseFavoriteForum(matcher: ParserPattern.Matcher): Favorite.Forum {
        return Favorite.Forum(
            favId = matcher.require(1).toInt(),
            forumId = matcher.require(6).toInt(),
            trackType = matcher.require(2),
            isPin = matcher.require(3) == "1",
            isNew = matcher.get(5)?.contains("+") == true,
            title = matcher.require(8).fromHtml(),
            date = matcher.require(19),
            lastUser = User.optional(
                matcher.require(20).toInt(),
                matcher.require(21).fromHtml()
            )
        )
    }
}
