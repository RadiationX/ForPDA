package forpdateam.ru.forpda.model.data.remote.api

import android.text.Spanned
import android.text.TextUtils
import forpdateam.ru.forpda.common.Html
import org.json.JSONObject

/**
 * Created by radiationx on 26.03.17.
 */
object ApiUtils {
    @JvmStatic
    fun coloredFromHtml(s: String): Spanned {
        return Html.fromHtml(s, Html.FROM_HTML_OPTION_USE_CSS_COLORS)
    }

    @JvmStatic
    fun spannedFromHtml(s: String): Spanned {
        return Html.fromHtml(s)
    }

    @JvmStatic
    fun fromHtml(s: String): String {
        return spannedFromHtml(s).toString()
    }

    fun htmlEncode(s: String?): String? {
        if (s == null) return null
        return TextUtils.htmlEncode(s)
    }

    fun escapeNewLine(s: String): String {
        val sb = StringBuilder()
        var c: Char
        val length = s.length
        for (i in 0 until length) {
            c = s[i]
            if (c == '\n') {
                sb.append("<br>")
            } else {
                sb.append(c)
            }
        }
        return sb.toString()
    }

    fun escapeQuotes(s: String?): String {
        var escaped = JSONObject.quote(s)
        escaped = escaped.substring(1, escaped.length - 1)
        return escaped
    }
}
