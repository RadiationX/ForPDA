package forpdateam.ru.forpda.model.data.remote.api.patterns

import forpdateam.ru.forpda.entity.remote.checker.PatternsDataJson
import forpdateam.ru.forpda.model.data.remote.WebClient
import kotlinx.serialization.json.Json
import javax.inject.Inject

/**
 * Created by radiationx on 28.01.18.
 */
class PatternsApi @Inject constructor(
    private val client: WebClient,
    private val json: Json
) {

    suspend fun loadPatterns(): PatternsDataJson = client
        .get("https://bitbucket.org/RadiationX/apps-updates/raw/master/forpda/patterns.json")
        .let { json.decodeFromString(it.body) }

}