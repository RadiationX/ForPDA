package forpdateam.ru.forpda.extensions

import org.json.JSONArray
import org.json.JSONObject


fun JSONObject.nullString(field: String, fallback: String? = null): String? {
    if (isNull(field))
        return null
    return optString(field, fallback)
}

fun JSONObject.nullGet(field: String): Any? {
    if (isNull(field))
        return null
    return get(field)
}

fun <R> JSONArray.mapObjects(block: (JSONObject) -> R): List<R> {
    val result = mutableListOf<R>()
    for (j in 0 until this.length()) {
        val jsonObject = this.getJSONObject(j)
        result.add(block.invoke(jsonObject))
    }
    return result
}

fun JSONArray.mapToString(): List<String> {
    val result = mutableListOf<String>()
    for (j in 0 until this.length()) {
        result.add(this.getString(j))
    }
    return result
}

