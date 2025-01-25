package forpdateam.ru.forpda.entity.remote.forum

/**
 * Created by radiationx on 16.10.17.
 */

class ForumRules {
    val items = mutableListOf<Item>()
    var html: String? = null
    var date: String? = null

    fun addItem(item: Item) {
        this.items.add(item)
    }

    class Item {
        var number: String? = null
        var text: String? = null
        var isHeader = false
    }

}
