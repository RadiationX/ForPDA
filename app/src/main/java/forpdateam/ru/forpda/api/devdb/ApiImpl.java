package forpdateam.ru.forpda.api.devdb;

import java.util.ArrayList;

import forpdateam.ru.forpda.api.devdb.interfaces.DevDbApi;
import forpdateam.ru.forpda.api.devdb.models.DevCatalog;
import forpdateam.ru.forpda.client.Client;
import rx.Observable;

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
        return Observable.defer(() -> Observable.just(Parser.parseBrands(client, devicesTypeUrl)));
    }
}
