package forpdateam.ru.forpda.entity.remote.others.pagination

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
}
