package ru.radiationx.regexparser

import ru.radiationx.regexparser.adapter.RegexAdapter
import ru.radiationx.regexparser.core.RegexContext
import ru.radiationx.regexparser.core.RegexFinder
import ru.radiationx.regexparser.core.RegexMatch
import ru.radiationx.regexparser.exceptions.RegexParserException

class RegexParser(
    private val context: RegexContext,
    private val adapter: RegexAdapter
) {

    fun findOnce(input: String, transform: (RegexMatch) -> Unit) {
        withFinder(input) { matcher ->
            matcher.next()?.also(transform)
        }
    }

    fun findAll(input: String, transform: (RegexMatch) -> Unit) {
        withFinder(input) { matcher ->
            matcher.forEach(transform)
        }
    }

    fun <R> mapOnce(input: String, transform: (RegexMatch) -> R): R? {
        return withFinder(input) { matcher ->
            matcher.next()?.let(transform)
        }
    }

    fun <R> map(input: String, transform: (RegexMatch) -> R): List<R> {
        return withFinder(input) { matcher ->
            val result = mutableListOf<R>()
            matcher.forEach {
                result.add(transform(it))
            }
            result
        }
    }

    fun <R> requireOnce(input: String, transform: (RegexMatch) -> R): R {
        return withFinder(input) { matcher ->
            matcher.requireNext().let(transform)
        }
    }

    private fun <R> withFinder(input: String, block: (RegexFinder) -> R): R {
        return withExceptionWrapping {
            val finder = FinderImpl(adapter.getFinder(input))
            try {
                block(finder)
            } catch (ex: Exception) {
                throw (ex as? RegexParserException ?: RegexParserException(ex)).apply {
                    setFinderState(finder.lastMatchIndex, finder.lastUsedGroup)
                }
            }
        }
    }

    private fun <R> withExceptionWrapping(block: () -> R): R {
        return try {
            block()
        } catch (ex: Exception) {
            throw (ex as? RegexParserException ?: RegexParserException(ex)).apply {
                setContext(context)
                complete()
            }
        }
    }

    private class FinderImpl(private val adapterFinder: RegexAdapter.Finder) : RegexFinder {

        var lastMatchIndex = -1
        var lastUsedGroup = -1

        override fun next(): RegexMatch? {
            lastMatchIndex++
            return adapterFinder.next()?.let {
                MatchImpl(it, this)
            }
        }

        override fun requireNext(): RegexMatch {
            return next() ?: throw RegexParserException().apply {
                setRequiredMatch()
            }
        }

        override fun forEach(block: (RegexMatch) -> Unit) {
            while (true) {
                val match = next() ?: break
                block.invoke(match)
            }
        }
    }

    private class MatchImpl(val adapterMatch: RegexAdapter.Match, val finder: FinderImpl) : RegexMatch {

        override fun get(group: Int): String? {
            finder.lastUsedGroup = group
            return adapterMatch.get(group)
        }

        override fun require(group: Int): String {
            return get(group) ?: throw RegexParserException().apply {
                setRequiredGroup(group)
            }
        }
    }
}
