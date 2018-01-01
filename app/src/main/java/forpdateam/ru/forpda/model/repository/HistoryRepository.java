package forpdateam.ru.forpda.model.repository;

import java.util.ArrayList;
import java.util.List;

import forpdateam.ru.forpda.entity.app.history.HistoryItem;
import forpdateam.ru.forpda.entity.db.history.HistoryItemBd;
import forpdateam.ru.forpda.model.system.SchedulersProvider;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by radiationx on 01.01.18.
 */

public class HistoryRepository {

    private SchedulersProvider schedulers;

    public HistoryRepository(SchedulersProvider schedulers) {
        this.schedulers = schedulers;
    }

    public Observable<List<HistoryItem>> getHistory() {
        return Observable.fromCallable(() -> {
            List<HistoryItem> items = new ArrayList<>();
            try (Realm realm = Realm.getDefaultInstance()) {
                RealmResults<HistoryItemBd> results = realm
                        .where(HistoryItemBd.class)
                        .findAllSorted("unixTime", Sort.DESCENDING);
                for(HistoryItemBd itemBd: results){
                    items.add(new HistoryItem(itemBd));
                }
            }
            return items;
        })
                .subscribeOn(schedulers.io())
                .observeOn(schedulers.ui());
    }

    public Completable remove(int id) {
        return Completable.fromRunnable(() -> {
            try (Realm realm = Realm.getDefaultInstance()) {
                realm.executeTransaction(realm1 -> realm1.where(HistoryItemBd.class)
                        .equalTo("id", id)
                        .findAll()
                        .deleteAllFromRealm());
            }
        })
                .subscribeOn(schedulers.io())
                .observeOn(schedulers.ui());
    }

    public Completable clear() {
        return Completable.fromRunnable(() -> {
            try (Realm realm = Realm.getDefaultInstance()) {
                realm.executeTransaction(realm1 -> realm1.delete(HistoryItemBd.class));
            }
        })
                .subscribeOn(schedulers.io())
                .observeOn(schedulers.ui());
    }
}
