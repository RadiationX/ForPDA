package forpdateam.ru.forpda.model.data.remote.api.patterns

import forpdateam.ru.forpda.entity.remote.checker.PatternsDataJson
import forpdateam.ru.forpda.model.data.remote.IWebClient
import kotlinx.serialization.json.Json

/**
 * Created by radiationx on 28.01.18.
 */
class PatternsApi(
    private val client: IWebClient,
    private val json: Json
) {

    suspend fun loadPatterns(): PatternsDataJson = client
        .get("https://bitbucket.org/RadiationX/apps-updates/raw/master/forpda/patterns.json")
        .let { json.decodeFromString(it.body) }

}