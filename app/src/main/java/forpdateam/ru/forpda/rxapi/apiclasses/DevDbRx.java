package forpdateam.ru.forpda.rxapi.apiclasses;

import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.api.ndevdb.models.Brand;
import forpdateam.ru.forpda.api.ndevdb.models.Brands;
import forpdateam.ru.forpda.api.ndevdb.models.Device;
import io.reactivex.Observable;

/**
 * Created by radiationx on 08.08.17.
 */

public class DevDbRx {
    public Observable<Brands> getBrands(String catId) {
        return Observable.fromCallable(() -> Api.DevDb().getBrands(catId));
    }

    public Observable<Brand> getBrand(String catId, String brandId) {
        return Observable.fromCallable(() -> Api.DevDb().getBrand(catId, brandId));
    }
    public Observable<Device> getDevice(String devId) {
        return Observable.fromCallable(() -> Api.DevDb().getDevice(devId));
    }
}
