package forpdateam.ru.forpda.model.data.remote.parser

import android.text.Spanned
import forpdateam.ru.forpda.model.data.remote.api.ApiUtils
import java.util.regex.Matcher

open class BaseParser {
    fun String?.fromHtml(): String? = this?.let { ApiUtils.fromHtml(it) }
    fun String?.fromHtmlToColored(): Spanned? = this?.let { ApiUtils.coloredFromHtml(it) }
    fun String?.fromHtmlToSpanned(): Spanned? = this?.let { ApiUtils.spannedFromHtml(it) }
}
