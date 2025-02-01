package forpdateam.ru.forpda.entity.remote.search

import forpdateam.ru.forpda.entity.DeferredData
import forpdateam.ru.forpda.entity.remote.others.pagination.Pagination

/**
 * Created by radiationx on 01.02.17.
 */

data class SearchResult(
    val items: List<SearchItem>,
    val settings: SearchSettings,
    val pagination: Pagination,
    val html: DeferredData<String>?
) 
