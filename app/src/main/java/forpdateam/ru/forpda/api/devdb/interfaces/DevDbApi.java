package forpdateam.ru.forpda.api.devdb.interfaces;

import java.util.ArrayList;

import forpdateam.ru.forpda.api.IWebClient;
import forpdateam.ru.forpda.api.devdb.models.DevCatalog;
import io.reactivex.Observable;


/**
 * Created by isanechek on 30.07.16.
 */

public interface DevDbApi {
    Observable<ArrayList<DevCatalog>> getBrands(IWebClient client, String devicesTypeUrl);
}
