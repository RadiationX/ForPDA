package forpdateam.ru.forpda.common.realm.wrapper

import io.github.xilinjia.krdb.Deleteable
import io.github.xilinjia.krdb.MutableRealm
import io.github.xilinjia.krdb.UpdatePolicy
import io.github.xilinjia.krdb.query.TRUE_PREDICATE
import io.github.xilinjia.krdb.types.RealmObject
import io.github.xilinjia.krdb.types.TypedRealmObject
import kotlin.reflect.KClass

class MutableRealmWrapper(
    private val realm: MutableRealm
) {

    fun <T : TypedRealmObject> rawQuery(
        clazz: KClass<T>,
        query: String,
        vararg args: Any?
    ): MutableRealmQueryWrapper<T> {
        return MutableRealmQueryWrapper(realm.query(clazz, query, *args))
    }

    fun <T : RealmObject> upsert(instance: T) {
        realm.copyToRealm(instance, UpdatePolicy.ALL)
    }

    fun <T : RealmObject> upsertAll(instances: List<T>) {
        instances.forEach {
            upsert(it)
        }
    }

    fun delete(deleteable: Deleteable) {
        realm.delete(deleteable)
    }

    fun <T : TypedRealmObject> delete(schemaClass: KClass<T>) {
        realm.delete(schemaClass)
    }
}

inline fun <reified T : TypedRealmObject> MutableRealmWrapper.query(): MutableRealmQueryWrapper<T> {
    return rawQuery(T::class, TRUE_PREDICATE)
}

inline fun <reified T : TypedRealmObject> MutableRealmWrapper.queryEquals(
    propertyName: String,
    value: Any
): MutableRealmQueryWrapper<T> {
    return rawQuery(T::class, "$propertyName == $0", value)
}