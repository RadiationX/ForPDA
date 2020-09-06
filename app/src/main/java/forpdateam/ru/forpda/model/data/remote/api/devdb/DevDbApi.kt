package forpdateam.ru.forpda.model.data.remote.api.devdb

import forpdateam.ru.forpda.entity.remote.devdb.Brand
import forpdateam.ru.forpda.entity.remote.devdb.Brands
import forpdateam.ru.forpda.entity.remote.devdb.Device
import forpdateam.ru.forpda.model.data.remote.IWebClient
import java.net.URLDecoder

/**
 * Created by radiationx on 06.08.17.
 */

class DevDbApi(
        private val webClient: IWebClient,
        private val devDbParser: DevDbParser
) {

    fun getBrands(catId: String): Brands {
        val response = webClient.get("https://4pda.ru/devdb/$catId/all")
        return devDbParser.parseBrands(response.body)
    }

    fun getBrand(catId: String, brandId: String): Brand {
        val response = webClient.get("https://4pda.ru/devdb/$catId/$brandId/all")
        return devDbParser.parseBrand(response.body)
    }

    fun getDevice(devId: String): Device {
        val response = webClient.get("https://4pda.ru/devdb/$devId")
        return devDbParser.parseDevice(response.body, devId)
    }

    fun search(query: String): Brand {
        val reqQuery = query.let {
            try {
                URLDecoder.decode(query, "windows-1251")
            } catch (ignore: Exception) {
                it
            }
        }
        val response = webClient.get("http://4pda.ru/devdb/search?s=$reqQuery")
        return devDbParser.parseSearch(response.body)
    }

}
