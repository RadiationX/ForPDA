package forpdateam.ru.forpda.entity.remote.devdb

/**
 * Created by radiationx on 06.08.17.
 */

data class Brands(
    val catId: String,
    val catTitle: String,
    val actual: Int,
    val all: Int,
    val letterMap: Map<String, List<Item>>
) {
    data class Item(
        val id: String,
        val title: String,
        val count: Int,
    )
}
