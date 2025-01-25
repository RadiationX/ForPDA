package forpdateam.ru.forpda.model.data.remote.api.favorites

import java.util.regex.Pattern

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

    companion object {
        private val pattern: Pattern =
            Pattern.compile("<div class=\"forum_sort\"[^>]*?>[\\s\\S]*?<select name=\"sort_key\">[\\s\\S]*?<option value=\"([^\"]*?)\" selected(?:=\"selected\")?[^>]*?>[\\s\\S]*?<select name=\"sort_by\">[\\s\\S]*?<option value=\"([^\"]*?)\" selected(?:=\"selected\")?[^>]*?>")

        fun parse(body: String): Sorting {
            val sorting = Sorting()
            val matcher = pattern.matcher(body)
            if (matcher.find()) {
                when (matcher.group(1)) {
                    Key.LAST_POST -> sorting.key =
                        Key.LAST_POST

                    Key.TITLE -> sorting.key =
                        Key.TITLE
                }
                when (matcher.group(2)) {
                    Order.DESC -> sorting.order = Order.DESC
                    Order.ASC -> sorting.order = Order.ASC
                }
            }
            return sorting
        }
    }
}
