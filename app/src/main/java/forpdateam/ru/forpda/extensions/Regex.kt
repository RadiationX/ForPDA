package forpdateam.ru.forpda.extensions

import java.util.regex.Matcher
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

inline fun Matcher.findOnce(action: (Matcher) -> Unit): Matcher {
    if (this.find()) action(this)
    return this
}

inline fun Matcher.findAll(action: (Matcher) -> Unit): Matcher {
    while (this.find()) action(this)
    return this
}

inline fun <R> Matcher.map(transform: (Matcher) -> R): List<R> {
    val data = mutableListOf<R>()
    findAll {
        data.add(transform(this))
    }
    return data
}

inline fun <R> Matcher.mapOnce(transform: (Matcher) -> R): R? {
    var data: R? = null
    findOnce {
        data = transform(this)
    }
    return data
}

@OptIn(ExperimentalContracts::class)
inline fun <R> Matcher.requireOnce(transform: (Matcher) -> R): R {
    contract {
        callsInPlace(transform, InvocationKind.EXACTLY_ONCE)
    }
    check(find()) {
        "Required match not found"
    }
    return transform(this)
}