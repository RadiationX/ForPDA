package forpdateam.ru.forpda.model.data.remote.api.favorites

import forpdateam.ru.forpda.entity.remote.favorites.FavData
import forpdateam.ru.forpda.entity.remote.favorites.Favorite
import forpdateam.ru.forpda.entity.remote.favorites.FavoritesData
import forpdateam.ru.forpda.entity.remote.others.pagination.Pagination
import forpdateam.ru.forpda.entity.remote.others.user.User
import forpdateam.ru.forpda.extensions.map
import forpdateam.ru.forpda.model.data.remote.ParserPatterns
import forpdateam.ru.forpda.model.data.remote.parser.BaseParser
import forpdateam.ru.forpda.model.data.storage.IPatternProvider
import java.util.regex.Matcher

class FavoritesParserNew(
    private val patternProvider: IPatternProvider
) : BaseParser() {

    private val scope = ParserPatterns.Favorites

    fun parseFavorites(response: String): FavoritesData {
        val list = patternProvider
            .getPattern(scope.scope, scope.main)
            .matcher(response)
            .map { matcher ->
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
            .getPattern(scope.scope, scope.check_action)
            .matcher(result)
            .find()
    }

    private fun parseFavorite(matcher: Matcher): Favorite {
        val isTopic = matcher.group(19) == null
        return if (isTopic) {
            parseFavoriteTopic(matcher)
        } else {
            parseFavoriteForum(matcher)
        }
    }

    private fun parseFavoriteTopic(matcher: Matcher): Favorite.Topic {
        val flagsGroup: String? = matcher.group(5)
        return Favorite.Topic(
            favId = matcher.group(1).toInt(),
            topicId = matcher.group(6).toInt(),
            trackType = matcher.group(2),
            isPin = matcher.group(3) == "1",
            isNew = flagsGroup?.contains("+") == true,
            isPoll = flagsGroup?.contains("^") == true,
            isClosed = flagsGroup?.contains("Х") == true,
            title = matcher.group(8)!!.fromHtml()!!,
            stParam = matcher.group(9)?.toInt(),
            desc = matcher.group(10).fromHtml(),
            forumId = matcher.group(12).toInt(),
            forumTitle = matcher.group(13)!!.fromHtml()!!,
            author = User.required(
                matcher.group(14).toInt(),
                matcher.group(15).fromHtml()
            ),
            lastUser = User.required(
                matcher.group(16).toInt(),
                matcher.group(17).fromHtml()
            ),
            date = matcher.group(18),
            curator = matcher.group(22)?.let {
                User.required(
                    it.toInt(),
                    matcher.group(23).fromHtml()
                )
            }
        )
    }

    private fun parseFavoriteForum(matcher: Matcher): Favorite.Forum {
        return Favorite.Forum(
            favId = matcher.group(1).toInt(),
            forumId = matcher.group(6).toInt(),
            trackType = matcher.group(2),
            isPin = matcher.group(3) == "1",
            isNew = matcher.group(5)?.contains("+") == true,
            title = matcher.group(8)!!.fromHtml()!!,
            date = matcher.group(19),
            lastUser = User.optional(
                matcher.group(20).toInt(),
                matcher.group(21).fromHtml()
            )
        )
    }
}
