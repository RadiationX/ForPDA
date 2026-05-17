package forpdateam.ru.forpda.model.data.remote.api.favorites

import forpdateam.ru.forpda.entity.remote.favorites.Favorite
import forpdateam.ru.forpda.entity.remote.favorites.FavoritesData
import forpdateam.ru.forpda.entity.remote.others.user.User
import forpdateam.ru.forpda.model.data.remote.ParserPatterns
import forpdateam.ru.forpda.model.data.remote.api.common.PaginationParser
import forpdateam.ru.forpda.model.data.remote.api.favorites.Sorting.Key
import forpdateam.ru.forpda.model.data.remote.api.favorites.Sorting.Order
import forpdateam.ru.forpda.model.data.remote.parser.BaseParser
import forpdateam.ru.forpda.model.data.storage.PatternProvider
import ru.radiationx.regexparser.core.RegexMatch
import javax.inject.Inject

class FavoritesParser @Inject constructor(
    private val patternProvider: PatternProvider,
    private val paginationParser: PaginationParser
) : BaseParser() {

    private val scope = ParserPatterns.Favorites

    fun parseFavorites(response: String): FavoritesData {
        val list = patternProvider
            .getRegexParser(scope.scope, scope.main)
            .map(response) { matcher ->
                parseFavorite(matcher)
            }
        return FavoritesData(
            items = list,
            pagination = paginationParser.parseForum(response),
            sorting = parseSorting(response)
        )
    }

    fun checkIsComplete(result: String): Boolean {
        return patternProvider
            .getRegexParser(scope.scope, scope.check_action)
            .mapOnce(result) { true }
            ?: false
    }

    private fun parseSorting(response: String): Sorting {
        val sorting = Sorting()
        patternProvider
            .getRegexParser(scope.scope, scope.sorting)
            .findOnce(response) {
                when (it.require(1)) {
                    Key.LAST_POST -> sorting.key = Key.LAST_POST
                    Key.TITLE -> sorting.key = Key.TITLE
                }
                when (it.require(2)) {
                    Order.DESC -> sorting.order = Order.DESC
                    Order.ASC -> sorting.order = Order.ASC
                }
            }
        return sorting
    }

    private fun parseFavorite(match: RegexMatch): Favorite {
        val isTopic = match.get(19) == null
        return if (isTopic) {
            parseFavoriteTopic(match)
        } else {
            parseFavoriteForum(match)
        }
    }

    private fun parseFavoriteTopic(match: RegexMatch): Favorite.Topic {
        val flagsGroup = match.get(5)
        return Favorite.Topic(
            favId = match.require(1).toInt(),
            topicId = match.require(6).toInt(),
            trackType = match.require(2),
            isPin = match.require(3) == "1",
            isNew = flagsGroup?.contains("+") == true,
            isPoll = flagsGroup?.contains("^") == true,
            isClosed = flagsGroup?.contains("Х") == true,
            title = match.require(8).fromHtml(),
            stParam = match.get(9)?.toInt(),
            desc = match.get(10)?.fromHtml(),
            forumId = match.require(12).toInt(),
            forumTitle = match.require(13).fromHtml(),
            author = User.required(
                match.require(14).toInt(),
                match.require(15).fromHtml()
            ),
            lastUser = User.required(
                match.require(16).toInt(),
                match.require(17).fromHtml()
            ),
            date = match.require(18),
            curator = match.get(22)?.let {
                User.required(
                    it.toInt(),
                    match.require(23).fromHtml()
                )
            }
        )
    }

    private fun parseFavoriteForum(match: RegexMatch): Favorite.Forum {
        return Favorite.Forum(
            favId = match.require(1).toInt(),
            forumId = match.require(6).toInt(),
            trackType = match.require(2),
            isPin = match.require(3) == "1",
            isNew = match.get(5)?.contains("+") == true,
            title = match.require(8).fromHtml(),
            date = match.require(19),
            lastUser = User.optional(
                match.require(20).toInt(),
                match.require(21).fromHtml()
            )
        )
    }
}
