package ru.radiationx.regexparser.adapter

interface RegexAdapter {

    fun getFinder(input: String): Finder

    interface Finder {
        fun next(): Match?
    }

    interface Match {
        fun get(group: Int): String?
    }
}