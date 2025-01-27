package forpdateam.ru.forpda.entity.remote.devdb

/**
 * Created by radiationx on 06.08.17.
 */

data class Brand(
    val id: String,
    val title: String,
    val catId: String,
    val catTitle: String,
    val actual: Int,
    val all: Int,
    val devices: List<DeviceItem>
) {

    data class DeviceItem(
        val id: String,
        val title: String,
        val price: String?,
        val imageSrc: String?,
        val rating: Int,
        val specs: List<Pair<String, String>>
    )
}
