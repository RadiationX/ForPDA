package forpdateam.ru.forpda.api.devdb;

import java.util.ArrayList;

import forpdateam.ru.forpda.api.devdb.interfaces.DevDbApi;
import forpdateam.ru.forpda.api.devdb.models.DevCatalog;
import forpdateam.ru.forpda.client.Client;
import rx.Observable;
import rx.Subscriber;

/**
 * Created by isanechek on 30.07.16.
 */

public class ApiImpl implements DevDbApi {

    private static ApiImpl instance;

    public static ApiImpl getInstance() {
        if (instance == null) {
            instance = new ApiImpl();
        }
        return instance;
    }

    @Override
    public Observable<ArrayList<DevCatalog>> getBrands(final Client client, final String devicesTypeUrl) {
        return Observable.create(new Observable.OnSubscribe<ArrayList<DevCatalog>>() {
            @Override
            public void call(Subscriber<? super ArrayList<DevCatalog>> subscriber) {
                try {
                    subscriber.onNext(Parser.parseBrands(client, devicesTypeUrl));
                    subscriber.onCompleted();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        });
    }
}
