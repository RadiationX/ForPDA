package forpdateam.ru.forpda.model.data.remote.api.checker

import forpdateam.ru.forpda.entity.app.checker.UpdateData
import forpdateam.ru.forpda.entity.remote.checker.UpdateDataJson
import kotlinx.serialization.json.Json

/**
 * Created by radiationx on 27.01.18.
 */
class CheckerParser(
    private val json: Json
) {

    fun parse(httpResponse: String): UpdateData {
        return json
            .decodeFromString<UpdateDataJson>(httpResponse)
            .toDomain()
    }

    private fun UpdateDataJson.toDomain(): UpdateData {
        return UpdateData(
            code = code,
            build = build,
            name = name,
            date = date,
            links = links.map { it.toDomain() },
            important = important,
            added = added,
            fixed = fixed,
            changed = changed,
            patternsVersion = patternsVersion
        )
    }

    private fun UpdateDataJson.UpdateLink.toDomain(): UpdateData.UpdateLink {
        return UpdateData.UpdateLink(
            name = name,
            url = url,
            type = type
        )
    }
}