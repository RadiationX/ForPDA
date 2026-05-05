package forpdateam.ru.forpda.common.realm.wrapper

import io.realm.kotlin.Realm
import io.realm.kotlin.query.TRUE_PREDICATE
import io.realm.kotlin.types.TypedRealmObject
import kotlin.reflect.KClass

class RealmWrapper(
    private val realm: Realm
) {

    fun <T : TypedRealmObject> rawQuery(
        clazz: KClass<T>,
        query: String,
        vararg args: Any?
    ): RealmQueryWrapper<T> {
        return RealmQueryWrapper(realm.query(clazz, query, *args))
    }

    suspend fun write(block: MutableRealmWrapper.() -> Unit) {
        realm.write {
            block(MutableRealmWrapper(this))
        }
    }
}

inline fun <reified T : TypedRealmObject> RealmWrapper.query(): RealmQueryWrapper<T> {
    return rawQuery(T::class, TRUE_PREDICATE)
}

inline fun <reified T : TypedRealmObject> RealmWrapper.queryEquals(
    propertyName: String,
    value: Any
): RealmQueryWrapper<T> {
    return rawQuery(T::class, "$propertyName == $0", value)
}