package ru.radiationx.links.url

interface LinkUrlBuilder {
    fun scheme(value: String)
    fun host(value: String)
    fun segment(value: String)
    fun query(name: String, value: String)
    fun fragment(value: String)
    fun build(): LinkUrl
}

fun LinkUrlBuilder.segment(value: Int) {
    segment(value.toString())
}

fun LinkUrlBuilder.segment(value: Long) {
    segment(value.toString())
}

fun LinkUrlBuilder.query(name: String, value: Int) {
    query(name, value.toString())
}

fun LinkUrlBuilder.query(name: String, value: Boolean) {
    query(name, if (value) "1" else "0")
}
