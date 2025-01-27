package forpdateam.ru.forpda.model.data.remote.api.checker

import forpdateam.ru.forpda.entity.remote.checker.UpdateData
import forpdateam.ru.forpda.extensions.mapObjects
import forpdateam.ru.forpda.extensions.mapToString
import org.json.JSONObject

/**
 * Created by radiationx on 27.01.18.
 */
class CheckerParser {

    fun parse(httpResponse: String): UpdateData {
        val responseJson = JSONObject(httpResponse)
        val jsonUpdate = responseJson.getJSONObject("update")

        val code = jsonUpdate.optInt("version_code", Int.MAX_VALUE)
        val build = jsonUpdate.optInt("version_build", Int.MAX_VALUE)
        val name = jsonUpdate.optString("version_name")
        val date = jsonUpdate.optString("build_date")

        val links = jsonUpdate.getJSONArray("links").mapObjects {
            UpdateData.UpdateLink(
                it.optString("name", "Unknown"),
                it.optString("url", ""),
                it.optString("type", "site")
            )
        }

        val important = jsonUpdate.getJSONArray("important").mapToString()
        val added = jsonUpdate.getJSONArray("added").mapToString()
        val fixed = jsonUpdate.getJSONArray("fixed").mapToString()
        val changed = jsonUpdate.getJSONArray("changed").mapToString()

        val patternsVersion = jsonUpdate.getInt("patternsVersion")

        return UpdateData(
            code = code,
            build = build,
            name = name,
            date = date,
            links = links,
            important = important,
            added = added,
            fixed = fixed,
            changed = changed,
            patternsVersion = patternsVersion
        )
    }
}