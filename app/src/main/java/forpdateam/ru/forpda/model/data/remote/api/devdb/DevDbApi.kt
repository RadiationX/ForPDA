package forpdateam.ru.forpda.model.data.remote.api.devdb

import forpdateam.ru.forpda.entity.remote.devdb.Brand
import forpdateam.ru.forpda.entity.remote.devdb.BrandSearch
import forpdateam.ru.forpda.entity.remote.devdb.Brands
import forpdateam.ru.forpda.entity.remote.devdb.Device
import forpdateam.ru.forpda.model.data.remote.IWebClient
import java.net.URLDecoder
import javax.inject.Inject

/**
 * Created by radiationx on 06.08.17.
 */

class DevDbApi @Inject constructor(
    private val webClient: IWebClient,
    private val devDbParser: DevDbParser
) {

    suspend fun getBrands(catId: String): Brands {
        val response = webClient.get("https://4pda.to/devdb/$catId/all")
        return devDbParser.parseBrands(response.body)
    }

    suspend fun getBrand(catId: String, brandId: String): Brand {
        val response = webClient.get("https://4pda.to/devdb/$catId/$brandId/all")
        return devDbParser.parseBrand(response.body)
    }

    suspend fun getDevice(devId: String): Device {
        val response = webClient.get("https://4pda.to/devdb/$devId")
        return devDbParser.parseDevice(response.body, devId)
    }

    suspend fun search(query: String): BrandSearch {
        val reqQuery = query.let {
            try {
                URLDecoder.decode(query, "windows-1251")
            } catch (ignore: Exception) {
                it
            }
        }
        val response = webClient.get("http://4pda.to/devdb/search?s=$reqQuery")
        return devDbParser.parseSearch(response.body)
    }

}
