package forpdateam.ru.forpda.model.data.remote.api.common

import forpdateam.ru.forpda.entity.remote.others.pagination.Pagination
import forpdateam.ru.forpda.model.data.remote.ParserPatterns
import forpdateam.ru.forpda.model.data.remote.parser.BaseParser
import forpdateam.ru.forpda.model.data.storage.PatternProvider
import java.util.regex.Pattern
import javax.inject.Inject
import kotlin.math.ceil

class PaginationParser @Inject constructor(
    private val patternProvider: PatternProvider
) : BaseParser() {

    private val scope = ParserPatterns.Pagination

    fun parseNews(page: String): Pagination {
        return patternProvider
            .getRegexParser(scope.scope, scope.news)
            .mapOnce(page) {
                Pagination(
                    perPage = 30,
                    all = ceil(it.require(1).toInt() / 30.0).toInt(),
                    current = it.require(2).toInt(),
                    isForum = false
                )
            }
            ?: createNewsDefault()
    }

    fun parseForum(page: String): Pagination {
        return patternProvider
            .getRegexParser(scope.scope, scope.forum)
            .mapOnce(page) {
                Pagination(
                    all = it.require(1).toInt() + 1,
                    perPage = it.require(2).toInt(),
                    current = it.require(3).toInt(),
                    isForum = true
                )
            }
            ?: createForumDefault()
    }

    private fun createForumDefault(): Pagination {
        return Pagination(20, 1, 1, true)
    }

    private fun createNewsDefault(): Pagination {
        return Pagination(30, 1, 1, false)
    }
}