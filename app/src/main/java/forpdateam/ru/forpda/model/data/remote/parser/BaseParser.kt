package forpdateam.ru.forpda.model.data.remote.parser

import android.text.Spanned
import forpdateam.ru.forpda.model.data.remote.api.ApiUtils

open class BaseParser {
    fun String.fromHtml(): String {
        return ApiUtils.fromHtml(this)
    }

    fun String.fromHtmlToColored(): Spanned {
        return ApiUtils.coloredFromHtml(this)
    }

    fun String.fromHtmlToSpanned(): Spanned {
        return ApiUtils.spannedFromHtml(this)
    }
}
