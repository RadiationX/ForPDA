package forpdateam.ru.forpda.model.data.remote.api.devdb

import forpdateam.ru.forpda.common.ApiRequest
import forpdateam.ru.forpda.entity.remote.devdb.Brand
import forpdateam.ru.forpda.entity.remote.devdb.BrandSearch
import forpdateam.ru.forpda.entity.remote.devdb.Brands
import forpdateam.ru.forpda.entity.remote.devdb.Device
import forpdateam.ru.forpda.model.data.remote.WebClient
import javax.inject.Inject

/**
 * Created by radiationx on 06.08.17.
 */

class DevDbApi @Inject constructor(
    private val webClient: WebClient,
    private val devDbParser: DevDbParser
) {

    suspend fun getBrands(catId: String): Brands {
        val response = webClient.request(ApiRequest.DevDb.GetBrands(catId))
        return devDbParser.parseBrands(response.body)
    }

    suspend fun getBrand(catId: String, brandId: String): Brand {
        val response = webClient.request(ApiRequest.DevDb.GetBrand(catId, brandId))
        return devDbParser.parseBrand(response.body)
    }

    suspend fun getDevice(devId: String): Device {
        val response = webClient.request(ApiRequest.DevDb.GetDevice(devId))
        return devDbParser.parseDevice(response.body, devId)
    }

    suspend fun search(query: String): BrandSearch {
        val response = webClient.request(ApiRequest.DevDb.Search(query))
        return devDbParser.parseSearch(response.body)
    }

}
