package forpdateam.ru.forpda.entity.remote.others.pagination

import java.util.regex.Pattern
import kotlin.math.ceil

/**
 * Created by radiationx on 03.03.17.
 */
class Pagination {
    var perPage: Int = 20
    var all: Int = 1
    var current: Int = 1
    var st: Int = 0
    var isForum: Boolean = true

    fun getPage(page: Int): Int {
        if (!isForum) return page
        return page * perPage
    }

    companion object {
        private val forumPaginationPattern: Pattern =
            Pattern.compile("parseInt\\((\\d*)\\)[\\s\\S]*?parseInt\\(st\\*(\\d*)\\)[\\s\\S]*?pagination\">[\\s\\S]*?<span[^>]*?>([^<]*?)<\\/span>")
        private val newsPaginationPattern: Pattern =
            Pattern.compile("class=\"s-count[\\s\\S]*?<strong>(\\d+)<\\/strong>[\\s\\S]*?<ul class=\"page-nav[^>]*?>[\\s\\S]*?<li class=\"active\"><a[^>]*?>(\\d+)")

        fun parseNews(page: String): Pagination {
            return parseNews(Pagination(), page)
        }

        fun parseNews(pagination: Pagination, page: String): Pagination {
            pagination.isForum = false
            val matcher = newsPaginationPattern.matcher(page)
            if (matcher.find()) {
                pagination.perPage = 30
                pagination.all = ceil(matcher.group(1).toInt() / 30.0) as Int
                pagination.current = matcher.group(2).toInt()
            }
            return pagination
        }

        fun parseForum(page: String): Pagination {
            return parseForum(Pagination(), page)
        }

        fun parseForum(pagination: Pagination, page: String): Pagination {
            val matcher = forumPaginationPattern.matcher(page)
            if (matcher.find()) {
                pagination.all = matcher.group(1).toInt() + 1
                pagination.perPage = matcher.group(2).toInt()
                pagination.current = matcher.group(3).toInt()
            }
            return pagination
        }
    }
}
