package ru.radiationx.links.url

interface LinkBuilderAdapter {
    fun scheme(value: String): LinkBuilderAdapter
    fun host(value: String): LinkBuilderAdapter
    fun segment(value: String): LinkBuilderAdapter
    fun segment(value: Int): LinkBuilderAdapter
    fun segment(value: Long): LinkBuilderAdapter
    fun query(name: String, value: String): LinkBuilderAdapter
    fun query(name: String, value: Int): LinkBuilderAdapter
    fun query(name: String, value: Boolean): LinkBuilderAdapter
    fun fragment(value: String): LinkBuilderAdapter
    fun build(): LinkUrlAdapter
}