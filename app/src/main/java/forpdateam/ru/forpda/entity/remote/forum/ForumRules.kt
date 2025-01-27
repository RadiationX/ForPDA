package forpdateam.ru.forpda.entity.remote.forum

/**
 * Created by radiationx on 16.10.17.
 */

data class ForumRules(
    val items: List<Item>,
    val html: String?
) {

    data class Item(
        val number: String?,
        val text: String?,
        val isHeader: Boolean
    )
}
