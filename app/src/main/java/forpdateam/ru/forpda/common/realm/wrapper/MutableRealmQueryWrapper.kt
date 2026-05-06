package forpdateam.ru.forpda.common.realm.wrapper

import io.github.xilinjia.krdb.query.RealmQuery
import io.github.xilinjia.krdb.query.RealmResults
import io.github.xilinjia.krdb.types.BaseRealmObject

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