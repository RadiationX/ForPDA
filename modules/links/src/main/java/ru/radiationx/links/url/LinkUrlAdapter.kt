package ru.radiationx.links.url

interface LinkUrlAdapter {
    fun parse(url: String): LinkUrl?
    fun builder(): LinkUrlBuilder
}