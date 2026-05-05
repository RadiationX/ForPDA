package forpdateam.ru.forpda.common.realm.wrapper

import io.realm.kotlin.query.RealmQuery
import io.realm.kotlin.query.RealmResults
import io.realm.kotlin.query.Sort
import io.realm.kotlin.types.BaseRealmObject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class RealmQueryWrapper<T : BaseRealmObject>(
    private val query: RealmQuery<T>
) {

    fun sort(property: String, sortOrder: Sort): RealmQueryWrapper<T> {
        return RealmQueryWrapper(query.sort(property, sortOrder))
    }

    fun <R> flowMapAll(transform: (T) -> R): Flow<List<R>> {
        return observeAll().map { it.map(transform) }
    }

    fun <R> flowMapFirst(transform: (T) -> R): Flow<R?> {
        return observeFirst().map { it?.let(transform) }
    }

    suspend fun <R> mapFirst(transform: (T) -> R): R? {
        return getFirst()?.let(transform)
    }

    suspend fun <R> mapAll(transform: (T) -> R): List<R> {
        return getAll().map(transform)
    }

    private fun observeFirst(): Flow<T?> {
        return query.first().asFlow().map { it.obj }
    }

    private fun observeAll(): Flow<RealmResults<T>> {
        return query.asFlow().map { it.list }
    }

    private suspend fun getFirst(): T? {
        return observeFirst().first()
    }

    private suspend fun getAll(): RealmResults<T> {
        return observeAll().first()
    }
}