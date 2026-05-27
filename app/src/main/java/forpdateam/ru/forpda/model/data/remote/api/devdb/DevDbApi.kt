package forpdateam.ru.forpda.model.data.remote.api.devdb

import forpdateam.ru.forpda.common.ApiRequest
import forpdateam.ru.forpda.entity.remote.devdb.Brand
import forpdateam.ru.forpda.entity.remote.devdb.BrandSearch
import forpdateam.ru.forpda.entity.remote.devdb.Brands
import forpdateam.ru.forpda.entity.remote.devdb.Device
import forpdateam.ru.forpda.model.data.remote.WebClient
import ru.radiationx.coretypes.DevDbCategoryId
import ru.radiationx.coretypes.DevDbDeviceId
import ru.radiationx.coretypes.DevDbDevicesId
import javax.inject.Inject

/**
 * Created by radiationx on 06.08.17.
 */

class DevDbApi @Inject constructor(
    private val webClient: WebClient,
    private val devDbParser: DevDbParser
) {

    suspend fun getBrands(categoryId: DevDbCategoryId): Brands {
        val response = webClient.request(ApiRequest.DevDb.GetBrands(categoryId))
        return devDbParser.parseBrands(response.body)
    }

    suspend fun getDevices(devicesId: DevDbDevicesId): Brand {
        val response = webClient.request(ApiRequest.DevDb.GetDevices(devicesId))
        return devDbParser.parseBrand(response.body)
    }

    suspend fun getDevice(deviceId: DevDbDeviceId): Device {
        val response = webClient.request(ApiRequest.DevDb.GetDevice(deviceId))
        return devDbParser.parseDevice(response.body, deviceId)
    }

    suspend fun search(query: String): BrandSearch {
        val response = webClient.request(ApiRequest.DevDb.Search(query))
        return devDbParser.parseSearch(response.body)
    }

}
