package forpdateam.ru.forpda.entity.remote.mentions

import java.util.ArrayList

import forpdateam.ru.forpda.entity.remote.others.pagination.Pagination

/**
 * Created by radiationx on 21.01.17.
 */

class MentionsData {
    val items: MutableList<MentionItem> = mutableListOf()
    var pagination = Pagination()

    fun addItem(item: MentionItem) {
        items.add(item)
    }
}
