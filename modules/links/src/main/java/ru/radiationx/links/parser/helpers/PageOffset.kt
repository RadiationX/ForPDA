package ru.radiationx.links.parser.helpers

import ru.radiationx.coretypes.PageOffset
import ru.radiationx.coretypes.ext.orDefault
import ru.radiationx.links.url.LinkUrl
import ru.radiationx.links.url.LinkUrlBuilder
import ru.radiationx.links.url.query

internal fun String?.parsePageOffset(): PageOffset {
    return this?.toIntOrNull()?.let { PageOffset(it) }.orDefault()
}

internal fun LinkUrl.parsePageOffset(): PageOffset {
    return query("st").parsePageOffset()
}

internal fun LinkUrlBuilder.query(offset: PageOffset) {
    query("st", offset.value)
}
