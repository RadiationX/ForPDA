package forpdateam.ru.forpda.model.repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import forpdateam.ru.forpda.api.events.models.NotificationEvent;
import forpdateam.ru.forpda.api.favorites.Favorites;
import forpdateam.ru.forpda.api.favorites.Sorting;
import forpdateam.ru.forpda.api.favorites.interfaces.IFavItem;
import forpdateam.ru.forpda.api.favorites.models.FavData;
import forpdateam.ru.forpda.api.favorites.models.FavItem;
import forpdateam.ru.forpda.client.ClientHelper;
import forpdateam.ru.forpda.entity.app.TabNotification;
import forpdateam.ru.forpda.entity.db.favorites.FavItemBd;
import forpdateam.ru.forpda.model.system.SchedulersProvider;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by radiationx on 01.01.18.
 */

public class FavoritesRepository {

    private SchedulersProvider schedulers;
    private Favorites favoritesApi;

    public FavoritesRepository(SchedulersProvider schedulers, Favorites favoritesApi) {
        this.schedulers = schedulers;
        this.favoritesApi = favoritesApi;
    }

    public Observable<FavData> loadFavorites(int st, boolean all, Sorting sorting) {
        return Observable.fromCallable(() -> favoritesApi.getFavorites(st, all, sorting))
                .subscribeOn(schedulers.io())
                .observeOn(schedulers.ui());
    }

    public Observable<List<FavItem>> getCache() {
        return Observable.fromCallable(() -> {
            List<FavItem> items = new ArrayList<>();
            try (Realm realm = Realm.getDefaultInstance()) {
                RealmResults<FavItemBd>  results = realm
                        .where(FavItemBd.class)
                        .findAll();
                for (FavItemBd itemBd : results) {
                    items.add(new FavItem(itemBd));
                }
            }
            return items;
        })
                .subscribeOn(schedulers.io())
                .observeOn(schedulers.ui());
    }

    public Completable saveFavorites(List<FavItem> items) {
        return Completable.fromRunnable(() -> {
            try (Realm realm = Realm.getDefaultInstance()) {
                realm.executeTransaction(r -> saveFavorites(r, items));
            }
        })
                .subscribeOn(schedulers.io())
                .observeOn(schedulers.ui());
    }

    public Completable markRead(int topicId) {
        return Completable.fromRunnable(() -> {
            try (Realm realm = Realm.getDefaultInstance()) {
                realm.executeTransaction(realm1 -> {
                    IFavItem favItem = realm1
                            .where(FavItemBd.class)
                            .equalTo("topicId", topicId)
                            .findFirst();
                    if (favItem != null) {
                        favItem.setNew(false);
                    }
                });
            }
        })
                .subscribeOn(schedulers.io())
                .observeOn(schedulers.ui());
    }

    public Observable<Integer> handleEvent(TabNotification event, Sorting sorting, int count) {
        return Observable.fromCallable(() -> {
            final int[] newCount = {0};
            try (Realm realm = Realm.getDefaultInstance()) {
                realm.executeTransaction(realm1 -> {
                    newCount[0] = handleEventTransaction(realm, event, sorting, count);
                });
            }
            return newCount[0];
        })
                .subscribeOn(schedulers.io())
                .observeOn(schedulers.ui());
    }

    private void saveFavorites(Realm realm, List<FavItem> items) {
        realm.delete(FavItemBd.class);
        List<FavItemBd> bdList = new ArrayList<>();
        for (FavItem item : items) {
            bdList.add(new FavItemBd(item));
        }
        realm.copyToRealmOrUpdate(bdList);
        bdList.clear();
    }

    private int handleEventTransaction(Realm realm, TabNotification event, Sorting sorting, int count) {
        RealmResults<FavItemBd> results = realm
                .where(FavItemBd.class)
                .findAll();
        ArrayList<FavItem> currentItems = new ArrayList<>();
        for (FavItemBd itemBd : results) {
            currentItems.add(new FavItem(itemBd));
        }

        NotificationEvent loadedEvent = event.getEvent();
        int id = loadedEvent.getSourceId();
        boolean isRead = loadedEvent.isRead();

        if (isRead) {
            count--;
            for (FavItem item : currentItems) {
                if (item.getTopicId() == id) {
                    item.setNew(false);
                    break;
                }
            }
        } else {
            count = event.getLoadedEvents().size();
            for (FavItem item : currentItems) {
                if (item.getTopicId() == id) {
                    if (item.getLastUserId() != ClientHelper.getUserId())
                        item.setNew(true);
                    item.setLastUserNick(loadedEvent.getUserNick());
                    item.setLastUserId(loadedEvent.getUserId());
                    item.setPin(loadedEvent.isImportant());
                    break;
                }
            }
            if (sorting.getKey().equals(Sorting.Key.TITLE)) {
                Collections.sort(currentItems, (o1, o2) -> {
                    if (sorting.getOrder().equals(Sorting.Order.ASC))
                        return o1.getTopicTitle().compareToIgnoreCase(o2.getTopicTitle());
                    return o2.getTopicTitle().compareToIgnoreCase(o1.getTopicTitle());
                });
            }

            if (sorting.getKey().equals(Sorting.Key.LAST_POST)) {
                for (FavItem item : currentItems) {
                    if (item.getTopicId() == id) {
                        currentItems.remove(item);
                        int index = 0;
                        if (sorting.getOrder().equals(Sorting.Order.ASC)) {
                            index = currentItems.size();
                        }
                        currentItems.add(index, item);
                        break;
                    }
                }
            }
        }
        saveFavorites(realm, currentItems);
        return count;
    }
}
