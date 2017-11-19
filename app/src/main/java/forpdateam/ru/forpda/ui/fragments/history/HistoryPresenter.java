package forpdateam.ru.forpda.ui.fragments.history;

import android.os.Bundle;

import forpdateam.ru.forpda.common.mvp.BasePresenter;
import forpdateam.ru.forpda.data.realm.history.HistoryItemBd;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by radiationx on 11.11.17.
 */

public class HistoryPresenter extends BasePresenter<HistoryContract.View> implements HistoryContract.Presenter {
    private Realm realm;

    public HistoryPresenter(HistoryContract.View view) {
        super(view);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        realm = Realm.getDefaultInstance();
    }

    @Override
    public void getHistory() {
        if (realm.isClosed()) {
            return;
        }
        view.setRefreshing(true);
        RealmResults<HistoryItemBd> results = realm
                .where(HistoryItemBd.class)
                .findAllSorted("unixTime", Sort.DESCENDING);
        view.showHistory(results);
        view.setRefreshing(false);
    }

    @Override
    public void remove(int id) {
        if (realm.isClosed())
            return;
        realm.executeTransactionAsync(realm1 -> {
            realm1.where(HistoryItemBd.class)
                    .equalTo("id", id)
                    .findAll()
                    .deleteAllFromRealm();
        }, this::getHistory);
    }

    @Override
    public void clear() {
        if (realm.isClosed())
            return;
        realm.executeTransactionAsync(realm1 -> {
            realm1.delete(HistoryItemBd.class);
        }, this::getHistory);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        realm.close();
    }
}
