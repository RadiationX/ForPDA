package forpdateam.ru.forpda.entity.remote.mentions

import forpdateam.ru.forpda.entity.remote.others.pagination.Pagination

/**
 * Created by radiationx on 21.01.17.
 */

class MentionsData {
    val items: MutableList<MentionItem> = mutableListOf()
    var pagination = Pagination.createForumDefault()

    fun addItem(item: MentionItem) {
        items.add(item)
    }
}
