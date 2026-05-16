package ru.radiationx.regexparser.core


interface RegexFinder {

    fun next(): RegexMatch?

    fun requireNext(): RegexMatch

    fun forEach(block: (RegexMatch) -> Unit)
}