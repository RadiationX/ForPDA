package forpdateam.ru.forpda.test.testdevdb;

import android.support.annotation.NonNull;

import forpdateam.ru.forpda.api.devdb.ApiImpl;
import forpdateam.ru.forpda.client.Client;
import forpdateam.ru.forpda.mvp.MvpPresenter;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by isanechek on 04.08.16.
 */

class TestDevDbPresenter extends MvpPresenter<DevDbMvpView> {

    private ApiImpl api;

    TestDevDbPresenter() {
        api = ApiImpl.getInstance();
    }

    void loadData(@NonNull String url) {
        checkViewAttached();
        getMvpView().setProgress(true);
        api.getBrands(Client.getInstance(), url)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(catalogs -> {
                    getMvpView().setProgress(false);
                    getMvpView().loadData(catalogs);
                }, throwable -> {
                    getMvpView().setProgress(true);
                    getMvpView().onError(0, throwable.toString());
                });
    }
}
