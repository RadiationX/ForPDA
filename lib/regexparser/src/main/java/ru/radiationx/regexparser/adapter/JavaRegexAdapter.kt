package ru.radiationx.regexparser.adapter

import java.util.regex.Matcher
import java.util.regex.Pattern

class JavaRegexAdapter(private val pattern: Pattern) : RegexAdapter {

    override fun getFinder(input: String): RegexAdapter.Finder {
        return FinderImpl(pattern.matcher(input))
    }

    private class FinderImpl(private val matcher: Matcher) : RegexAdapter.Finder {

        private val match = MatchImpl(matcher)

        override fun next(): RegexAdapter.Match? {
            if (matcher.find()) {
                return match
            }
            return null
        }
    }

    private class MatchImpl(private val matcher: Matcher) : RegexAdapter.Match {

        override fun get(group: Int): String? {
            return matcher.group(group)
        }
    }
}