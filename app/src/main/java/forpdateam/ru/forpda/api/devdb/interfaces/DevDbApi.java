package forpdateam.ru.forpda.api.devdb.interfaces;

import java.util.ArrayList;

import forpdateam.ru.forpda.api.devdb.models.DevCatalog;
import forpdateam.ru.forpda.client.Client;
import io.reactivex.Observable;


/**
 * Created by isanechek on 30.07.16.
 */

public interface DevDbApi {
    Observable<ArrayList<DevCatalog>> getBrands(Client client, String devicesTypeUrl);
}
