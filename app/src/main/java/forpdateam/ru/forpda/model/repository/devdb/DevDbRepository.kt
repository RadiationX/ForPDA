package forpdateam.ru.forpda.model.repository.devdb

import forpdateam.ru.forpda.entity.remote.devdb.Brand
import forpdateam.ru.forpda.entity.remote.devdb.BrandSearch
import forpdateam.ru.forpda.entity.remote.devdb.Brands
import forpdateam.ru.forpda.entity.remote.devdb.Device
import forpdateam.ru.forpda.model.data.remote.api.devdb.DevDbApi
import javax.inject.Inject

/**
 * Created by radiationx on 01.01.18.
 */

class DevDbRepository @Inject constructor(
    private val devDbApi: DevDbApi
) {

    suspend fun getBrands(catId: String): Brands {
        return devDbApi.getBrands(catId)
    }

    suspend fun getBrand(catId: String, brandId: String): Brand {
        return devDbApi.getBrand(catId, brandId)
    }

    suspend fun getDevice(devId: String): Device {
        return devDbApi.getDevice(devId)
    }

    suspend fun search(query: String): BrandSearch {
        return devDbApi.search(query)
    }

}
