package forpdateam.ru.forpda.model.data.remote.parser

import android.text.Spanned
import forpdateam.ru.forpda.model.data.remote.api.ApiUtils
import java.util.regex.Matcher

open class BaseParser {
    fun String?.fromHtml(): String? = this?.let { ApiUtils.fromHtml(it) }
    fun String?.fromHtmlToColored(): Spanned? = this?.let { ApiUtils.coloredFromHtml(it) }
    fun String?.fromHtmlToSpanned(): Spanned? = this?.let { ApiUtils.spannedFromHtml(it) }

    inline fun Matcher.findOnce(action: (Matcher) -> Unit): Matcher {
        if (this.find()) action(this)
        return this
    }

    inline fun Matcher.findAll(action: (Matcher) -> Unit): Matcher {
        while (this.find()) action(this)
        return this
    }

    inline fun <R> Matcher.map(transform: (Matcher) -> R): List<R> {
        val data = mutableListOf<R>()
        findAll {
            data.add(transform(this))
        }
        return data
    }

    inline fun <R> Matcher.mapOnce(transform: (Matcher) -> R): R? {
        var data: R? = null
        findOnce {
            data = transform(this)
        }
        return data
    }
}
