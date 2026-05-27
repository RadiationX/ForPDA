package forpdateam.ru.forpda.entity.remote.devdb

import ru.radiationx.coretypes.DevDbBrandId
import ru.radiationx.coretypes.DevDbCategoryId

/**
 * Created by radiationx on 06.08.17.
 */

data class Brands(
    val id: DevDbCategoryId,
    val title: String,
    val actual: Int,
    val all: Int,
    val letterMap: Map<String, List<Item>>
) {
    data class Item(
        val id: DevDbBrandId,
        val title: String,
        val count: Int,
    )
}
