package forpdateam.ru.forpda.rxapi.apiclasses;

import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.util.SparseArray;

import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.api.devdb.models.Brand;
import forpdateam.ru.forpda.api.devdb.models.Brands;
import forpdateam.ru.forpda.api.devdb.models.Device;
import io.reactivex.Observable;

/**
 * Created by radiationx on 08.08.17.
 */

public class DevDbRx {
    private SparseArray<ColorFilter> colorFilters = new SparseArray<>();

    public DevDbRx() {
        colorFilters.put(1, new PorterDuffColorFilter(Color.parseColor("#850113"), PorterDuff.Mode.SRC_IN));
        colorFilters.put(2, new PorterDuffColorFilter(Color.parseColor("#d50000"), PorterDuff.Mode.SRC_IN));
        colorFilters.put(3, new PorterDuffColorFilter(Color.parseColor("#ffac00"), PorterDuff.Mode.SRC_IN));
        colorFilters.put(4, new PorterDuffColorFilter(Color.parseColor("#99cc00"), PorterDuff.Mode.SRC_IN));
        colorFilters.put(5, new PorterDuffColorFilter(Color.parseColor("#339900"), PorterDuff.Mode.SRC_IN));
    }

    public ColorFilter getColorFilter(int rating) {
        return colorFilters.get(Api.DevDb().getRatingCode(rating));
    }

    public Observable<Brands> getBrands(String catId) {
        return Observable.fromCallable(() -> Api.DevDb().getBrands(catId));
    }

    public Observable<Brand> getBrand(String catId, String brandId) {
        return Observable.fromCallable(() -> Api.DevDb().getBrand(catId, brandId));
    }

    public Observable<Device> getDevice(String devId) {
        return Observable.fromCallable(() -> Api.DevDb().getDevice(devId));
    }

    public Observable<Brand> search(String query) {
        return Observable.fromCallable(() -> Api.DevDb().search(query));
    }
}
