package ru.radiationx.regexparser.adapter

class KotlinRegexAdapter(private val regex: Regex) : RegexAdapter {

    override fun getFinder(input: String): RegexAdapter.Finder {
        return FinderImpl(regex.findAll(input))
    }

    private class FinderImpl(sequence: Sequence<MatchResult>) : RegexAdapter.Finder {

        private val iterator = sequence.iterator()

        override fun next(): RegexAdapter.Match? {
            if (iterator.hasNext()) {
                return MatchImpl(iterator.next())
            }
            return null
        }
    }

    private class MatchImpl(val result: MatchResult) : RegexAdapter.Match {

        override fun get(group: Int): String? {
            return result.groups[group]?.value
        }
    }
}