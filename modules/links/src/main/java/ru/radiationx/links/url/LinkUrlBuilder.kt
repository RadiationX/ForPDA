package ru.radiationx.links.url

interface LinkUrlBuilder {
    fun scheme(value: String): LinkUrlBuilder
    fun host(value: String): LinkUrlBuilder
    fun segment(value: String): LinkUrlBuilder
    fun segment(value: Int): LinkUrlBuilder
    fun segment(value: Long): LinkUrlBuilder
    fun query(name: String, value: String): LinkUrlBuilder
    fun query(name: String, value: Int): LinkUrlBuilder
    fun query(name: String, value: Boolean): LinkUrlBuilder
    fun fragment(value: String): LinkUrlBuilder
    fun build(): LinkUrl
}