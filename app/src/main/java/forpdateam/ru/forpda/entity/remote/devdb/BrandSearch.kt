package forpdateam.ru.forpda.entity.remote.devdb

/**
 * Created by radiationx on 06.08.17.
 */

data class BrandSearch(
    val actual: Int,
    val all: Int,
    val devices: List<Brand.DeviceItem>
)
