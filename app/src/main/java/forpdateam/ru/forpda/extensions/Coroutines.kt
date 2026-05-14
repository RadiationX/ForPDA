package forpdateam.ru.forpda.extensions

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.coroutines.cancellation.CancellationException

inline fun <T, R> T.coRunCatching(block: T.() -> R): Result<R> {
    return try {
        Result.success(block())
    } catch (e: Throwable) {
        if (e is CancellationException) {
            throw e
        } else {
            Result.failure(e)
        }
    }
}

fun <T, R> Flow<List<T>>.mapInnerList(transform: suspend (T) -> (R)): Flow<List<R>> {
    return map { innerList ->
        innerList.map {
            transform.invoke(it)
        }
    }
}

