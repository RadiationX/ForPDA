package ru.radiationx.links.parser

import ru.radiationx.links.Links
import ru.radiationx.links.url.LinkAdapter
import ru.radiationx.links.url.LinkUrlAdapter

class LinkParser(private val adapter: LinkAdapter) {

    fun parse(url: String): Links? {
        return parse(adapter.parse(url))
    }

    fun parse(adapter: LinkUrlAdapter): Links? {

    }
}