package forpdateam.ru.forpda.entity.remote.devdb

import ru.radiationx.coretypes.DevDbDeviceId
import ru.radiationx.coretypes.DevDbDevicesId

/**
 * Created by radiationx on 06.08.17.
 */

data class Brand(
    val id: DevDbDevicesId,
    val title: String,
    val catTitle: String,
    val actual: Int,
    val all: Int,
    val devices: List<DeviceItem>
) {

    data class DeviceItem(
        val id: DevDbDeviceId,
        val title: String,
        val price: String?,
        val imageSrc: String?,
        val rating: Int,
        val specs: List<Device.Spec>
    )
}
