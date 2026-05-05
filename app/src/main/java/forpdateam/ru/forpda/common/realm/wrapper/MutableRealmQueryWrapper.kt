package forpdateam.ru.forpda.common.realm.wrapper

import io.realm.kotlin.query.RealmQuery
import io.realm.kotlin.query.RealmResults
import io.realm.kotlin.types.BaseRealmObject

class MutableRealmQueryWrapper<T : BaseRealmObject>(
    private val query: RealmQuery<T>
) {

    fun first(): T? {
        return query.first().find()
    }

    fun all(): RealmResults<T> {
        return query.find()
    }
}