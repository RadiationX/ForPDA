package ru.radiationx.links.url

interface LinkAdapter {
    fun parse(url: String): LinkUrlAdapter
    fun builder(): LinkBuilderAdapter
}