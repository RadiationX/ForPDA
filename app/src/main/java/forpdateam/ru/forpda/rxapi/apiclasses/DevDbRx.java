package forpdateam.ru.forpda.rxapi.apiclasses;

import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.api.ndevdb.models.Device;
import forpdateam.ru.forpda.api.ndevdb.models.Manufacturer;
import forpdateam.ru.forpda.api.ndevdb.models.Manufacturers;
import forpdateam.ru.forpda.api.theme.editpost.models.EditPostForm;
import io.reactivex.Observable;

/**
 * Created by radiationx on 08.08.17.
 */

public class DevDbRx {
    public Observable<Manufacturers> getManufacturers(String catId) {
        return Observable.fromCallable(() -> Api.DevDb().getManufacturers(catId));
    }

    public Observable<Manufacturer> getManufacturer(String catId, String manId) {
        return Observable.fromCallable(() -> Api.DevDb().getManufacturer(catId, manId));
    }
    public Observable<Device> getDevice(String devId) {
        return Observable.fromCallable(() -> Api.DevDb().getDevice(devId));
    }
}
