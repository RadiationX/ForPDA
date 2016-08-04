package forpdateam.ru.forpda.test.testdevdb;

import java.util.ArrayList;

import forpdateam.ru.forpda.api.devdb.models.DevCatalog;
import forpdateam.ru.forpda.mvp.MvpView;

/**
 * Created by isanechek on 04.08.16.
 */

public interface DevDbMvpView extends MvpView {
    void loadData(ArrayList<DevCatalog> brands);
}
