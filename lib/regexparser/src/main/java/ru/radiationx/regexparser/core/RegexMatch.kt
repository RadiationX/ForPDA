package ru.radiationx.regexparser.core

interface RegexMatch {

    fun get(group: Int): String?

    fun require(group: Int): String
}