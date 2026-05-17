package forpdateam.ru.forpda.model.data.remote.api.search

import forpdateam.ru.forpda.entity.remote.search.SearchResult
import forpdateam.ru.forpda.entity.remote.search.SearchSettings
import forpdateam.ru.forpda.model.data.remote.WebClient
import javax.inject.Inject

/**
 * Created by radiationx on 01.02.17.
 */

class SearchApi @Inject constructor(
    private val webClient: WebClient,
    private val searchParser: SearchParser
) {

    suspend fun getSearch(settings: SearchSettings): SearchResult {
        val response = webClient.get(settings.toUrl())
        return searchParser.parse(response.body, settings)
    }
}
