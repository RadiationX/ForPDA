package forpdateam.ru.forpda.model.data.remote.api.search

import forpdateam.ru.forpda.entity.remote.search.SearchResult
import forpdateam.ru.forpda.entity.remote.search.SearchSettings
import forpdateam.ru.forpda.model.data.remote.IWebClient

/**
 * Created by radiationx on 01.02.17.
 */

class SearchApi(
        private val webClient: IWebClient,
        private val searchParser: SearchParser
) {

    fun getSearch(settings: SearchSettings): SearchResult {
        val response = webClient.get(settings.toUrl())
        return searchParser.parse(response.body, settings)
    }
}
