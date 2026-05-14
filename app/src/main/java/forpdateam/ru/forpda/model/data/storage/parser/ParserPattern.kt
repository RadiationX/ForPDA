package forpdateam.ru.forpda.model.data.storage.parser


private typealias JavaPattern = java.util.regex.Pattern

private typealias JavaMatcher = java.util.regex.Matcher

data class ParserContext(
    val scope: String,
    val key: String,
)

class ParserPattern(
    private val context: ParserContext,
    private val javaPattern: JavaPattern
) {

    fun findOnce(input: String, transform: (Matcher) -> Unit) {
        useMatcher(input) { matcher ->
            matcher.find()?.also(transform)
        }
    }

    fun findAll(input: String, transform: (Matcher) -> Unit) {
        useMatcher(input) { matcher ->
            matcher.forEach(transform)
        }
    }

    fun <R> mapOnce(input: String, transform: (Matcher) -> R): R? {
        return useMatcher(input) { matcher ->
            matcher.find()?.let(transform)
        }
    }

    fun <R> map(input: String, transform: (Matcher) -> R): List<R> {
        return useMatcher(input) { matcher ->
            val result = mutableListOf<R>()
            matcher.forEach {
                result.add(transform(it))
            }
            result
        }
    }

    fun <R> requireOnce(input: String, transform: (Matcher) -> R): R {
        return useMatcher(input) { matcher ->
            matcher.find()!!.let(transform)
        }
    }

    private fun <R> useMatcher(input: String, block: (MatcherScope) -> R): R {
        return withExceptionWrapping {
            val matcherScope = MatcherScope(javaPattern.matcher(input))
            block(matcherScope)
        }
    }

    private fun <R> withExceptionWrapping(block: () -> R): R {
        return block()
    }

    class Matcher(
        private val javaMatcher: JavaMatcher
    ) {

        fun get(group: Int): String? {
            return javaMatcher.group(group)
        }

        fun require(group: Int): String {
            return javaMatcher.group(group)
        }

        private fun group(group: Int): String? {
            return javaMatcher.group(group)
        }
    }

    private class MatcherScope(
        private val javaMatcher: JavaMatcher
    ) {

        private val matcher = Matcher(javaMatcher)

        fun find(): Matcher? {
            if (javaMatcher.find()) {
                return matcher
            }
            return null
        }

        fun forEach(block: (Matcher) -> Unit) {
            while (javaMatcher.find()) {
                block.invoke(matcher)
            }
        }
    }
}
