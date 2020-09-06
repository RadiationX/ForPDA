package forpdateam.ru.forpda.model.data.remote.api.checker

import forpdateam.ru.forpda.entity.remote.checker.UpdateData
import org.json.JSONObject

/**
 * Created by radiationx on 27.01.18.
 */
class CheckerParser() {

    fun parse(httpResponse: String): UpdateData {
        val resData = UpdateData()
        val responseJson = JSONObject(httpResponse)
        val jsonUpdate = responseJson.getJSONObject("update")

        resData.code = jsonUpdate.optInt("version_code", Int.MAX_VALUE)
        resData.build = jsonUpdate.optInt("version_build", Int.MAX_VALUE)
        resData.name = jsonUpdate.optString("version_name")
        resData.date = jsonUpdate.optString("build_date")

        jsonUpdate.getJSONArray("links")?.let {
            for (i in 0 until it.length()) {
                it.optJSONObject(i)?.let { linkJson ->
                    resData.links.add(UpdateData.UpdateLink(
                            linkJson.optString("name", "Unknown"),
                            linkJson.optString("url", ""),
                            linkJson.optString("type", "site")
                    ))
                }
            }
        }

        jsonUpdate.getJSONArray("important")?.let { importantJson ->
            for (i in 0 until importantJson.length()) {
                importantJson.optString(i, null)?.let {
                    resData.important.add(it)
                }
            }
        }

        jsonUpdate.getJSONArray("added")?.let { addedJson ->
            for (i in 0 until addedJson.length()) {
                addedJson.optString(i, null)?.let {
                    resData.added.add(it)
                }
            }
        }

        jsonUpdate.getJSONArray("fixed")?.let { fixedJson ->
            for (i in 0 until fixedJson.length()) {
                fixedJson.optString(i, null)?.let {
                    resData.fixed.add(it)
                }
            }
        }

        jsonUpdate.getJSONArray("changed")?.let { changedJson ->
            for (i in 0 until changedJson.length()) {
                changedJson.optString(i, null)?.let {
                    resData.changed.add(it)
                }
            }
        }

        resData.patternsVersion = jsonUpdate.getInt("patternsVersion")

        return resData
    }
}