package forpdateam.ru.forpda.entity.remote.search

import java.util.ArrayList

import forpdateam.ru.forpda.entity.remote.others.pagination.Pagination

/**
 * Created by radiationx on 01.02.17.
 */

class SearchResult {
    val items = mutableListOf<SearchItem>()
    var settings: SearchSettings? = null
    var pagination = Pagination()
    var html: String? = null

    fun addItem(item: SearchItem) {
        items.add(item)
    }
}
