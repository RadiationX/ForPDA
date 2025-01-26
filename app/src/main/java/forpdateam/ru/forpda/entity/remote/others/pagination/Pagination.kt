package forpdateam.ru.forpda.entity.remote.others.pagination

import java.util.regex.Pattern
import kotlin.math.ceil

/**
 * Created by radiationx on 03.03.17.
 */
data class Pagination(
    val perPage: Int,
    val all: Int,
    val current: Int,
    private val isForum: Boolean
) {


    fun getPage(page: Int): Int {
        if (!isForum) return page
        return page * perPage
    }

    fun firstPage(): Int {
        return if (isForum) 0 else 1
    }

    fun prevPage(): Int {
        return getPage(current - (if (isForum) 2 else 1))
    }

    fun currentPage(): Int {
        return getPage(current - 1)
    }

    fun nextPage(): Int {
        return getPage(current + (if (isForum) 0 else 1))
    }

    fun lastPage(): Int {
        return getPage(all - (if (isForum) 1 else 0))
    }

    fun hasPrev(): Boolean {
        return current > if (isForum) 1 else 2
    }

    fun hasNext(): Boolean {
        return current < all
    }

    fun isSinglePage(): Boolean {
        return all <= 1
    }

    companion object {
        private val forumPaginationPattern: Pattern =
            Pattern.compile("parseInt\\((\\d*)\\)[\\s\\S]*?parseInt\\(st\\*(\\d*)\\)[\\s\\S]*?pagination\">[\\s\\S]*?<span[^>]*?>([^<]*?)<\\/span>")
        private val newsPaginationPattern: Pattern =
            Pattern.compile("class=\"s-count[\\s\\S]*?<strong>(\\d+)<\\/strong>[\\s\\S]*?<ul class=\"page-nav[^>]*?>[\\s\\S]*?<li class=\"active\"><a[^>]*?>(\\d+)")

        fun createForumDefault(): Pagination {
            return Pagination(20, 1, 1, true)
        }

        fun createNewsDefault(): Pagination {
            return Pagination(30, 1, 1, false)
        }

        fun parseNews(page: String): Pagination {
            val matcher = newsPaginationPattern.matcher(page)
            return if (matcher.find()) {
                Pagination(
                    perPage = 30,
                    all = ceil(matcher.group(1).toInt() / 30.0).toInt(),
                    current = matcher.group(2).toInt(),
                    isForum = false
                )
            } else {
                createNewsDefault()
            }
        }

        fun parseForum(page: String): Pagination {
            val matcher = forumPaginationPattern.matcher(page)
            return if (matcher.find()) {
                Pagination(
                    all = matcher.group(1).toInt() + 1,
                    perPage = matcher.group(2).toInt(),
                    current = matcher.group(3).toInt(),
                    isForum = true
                )
            } else {
                createForumDefault()
            }
        }
    }
}
