package ru.radiationx.links.url

interface LinkUrlAdapter {
    val scheme: String
    val host: String
    val pathSegments: List<String>
    fun segment(index: Int): String?
    fun query(name: String): String?
    val fragment: String?
}