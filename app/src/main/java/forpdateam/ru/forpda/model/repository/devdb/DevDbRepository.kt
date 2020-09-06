package forpdateam.ru.forpda.model.repository.devdb

import forpdateam.ru.forpda.entity.remote.devdb.Brand
import forpdateam.ru.forpda.entity.remote.devdb.Brands
import forpdateam.ru.forpda.entity.remote.devdb.Device
import forpdateam.ru.forpda.model.SchedulersProvider
import forpdateam.ru.forpda.model.data.remote.api.devdb.DevDbApi
import forpdateam.ru.forpda.model.repository.BaseRepository
import io.reactivex.Single

/**
 * Created by radiationx on 01.01.18.
 */

class DevDbRepository(
        private val schedulers: SchedulersProvider,
        private val devDbApi: DevDbApi
) : BaseRepository(schedulers) {

    fun getBrands(catId: String): Single<Brands> = Single
            .fromCallable { devDbApi.getBrands(catId) }
            .runInIoToUi()

    fun getBrand(catId: String, brandId: String): Single<Brand> = Single
            .fromCallable { devDbApi.getBrand(catId, brandId) }
            .runInIoToUi()

    fun getDevice(devId: String): Single<Device> = Single
            .fromCallable { devDbApi.getDevice(devId) }
            .runInIoToUi()

    fun search(query: String): Single<Brand> = Single
            .fromCallable { devDbApi.search(query) }
            .runInIoToUi()

}
