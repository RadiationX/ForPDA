package forpdateam.ru.forpda.model.data.remote.api.favorites

/**
 * Created by radiationx on 12.08.17.
 */
class Sorting {
    object Key {
        const val HEADER: String = "sort_key"
        const val LAST_POST: String = "last_post"
        const val TITLE: String = "title"
    }

    object Order {
        const val HEADER: String = "sort_by"
        const val DESC: String = "Z-A"
        const val ASC: String = "A-Z"
    }

    constructor()

    constructor(key: String, order: String) {
        this.key = key
        this.order = order
    }

    var key: String = ""
    var order: String = ""
}
