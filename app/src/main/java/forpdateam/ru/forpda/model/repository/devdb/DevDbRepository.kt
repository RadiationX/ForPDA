package forpdateam.ru.forpda.model.repository.devdb

import forpdateam.ru.forpda.entity.remote.devdb.Brand
import forpdateam.ru.forpda.entity.remote.devdb.BrandSearch
import forpdateam.ru.forpda.entity.remote.devdb.Brands
import forpdateam.ru.forpda.entity.remote.devdb.Device
import forpdateam.ru.forpda.model.data.remote.api.devdb.DevDbApi
import ru.radiationx.coretypes.DevDbCategoryId
import ru.radiationx.coretypes.DevDbDeviceId
import ru.radiationx.coretypes.DevDbDevicesId
import javax.inject.Inject

/**
 * Created by radiationx on 01.01.18.
 */

class DevDbRepository @Inject constructor(
    private val devDbApi: DevDbApi
) {

    suspend fun getBrands(categoryId: DevDbCategoryId): Brands {
        return devDbApi.getBrands(categoryId)
    }

    suspend fun getDevices(devicesId: DevDbDevicesId): Brand {
        return devDbApi.getDevices(devicesId)
    }

    suspend fun getDevice(deviceId: DevDbDeviceId): Device {
        return devDbApi.getDevice(deviceId)
    }

    suspend fun search(query: String): BrandSearch {
        return devDbApi.search(query)
    }

}
