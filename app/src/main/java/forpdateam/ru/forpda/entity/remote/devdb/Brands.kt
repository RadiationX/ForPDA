package forpdateam.ru.forpda.entity.remote.devdb

import java.util.LinkedHashMap

/**
 * Created by radiationx on 06.08.17.
 */

class Brands {
    val letterMap = LinkedHashMap<String, List<Item>>()
    var catId: String? = null
    var catTitle: String? = null
    var actual = 0
    var all = 0

    class Item {
        var title: String? = null
        var id: String? = null
        var count = 0
    }
}
