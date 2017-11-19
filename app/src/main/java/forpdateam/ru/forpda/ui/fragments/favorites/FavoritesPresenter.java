package forpdateam.ru.forpda.ui.fragments.favorites;

import android.os.Bundle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import forpdateam.ru.forpda.api.events.models.NotificationEvent;
import forpdateam.ru.forpda.api.favorites.Sorting;
import forpdateam.ru.forpda.api.favorites.interfaces.IFavItem;
import forpdateam.ru.forpda.api.favorites.models.FavData;
import forpdateam.ru.forpda.api.favorites.models.FavItem;
import forpdateam.ru.forpda.apirx.RxApi;
import forpdateam.ru.forpda.client.ClientHelper;
import forpdateam.ru.forpda.common.mvp.BasePresenter;
import forpdateam.ru.forpda.data.models.TabNotification;
import forpdateam.ru.forpda.data.realm.favorites.FavItemBd;
import io.reactivex.Observable;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by radiationx on 11.11.17.
 */

public class FavoritesPresenter extends BasePresenter<FavoritesContract.View> implements FavoritesContract.Presenter {
    private Realm realm;

    FavoritesPresenter(FavoritesContract.View view) {
        super(view);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        realm = Realm.getDefaultInstance();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    @Override
    public void getFavorites(int st, boolean all, Sorting sorting) {
        view.setRefreshing(true);
        subscribe(RxApi.Favorites().getFavorites(st, all, sorting), favData -> {
            view.setRefreshing(false);
            view.onLoadFavorites(favData);
        }, new FavData(), v -> getFavorites(st, all, sorting));
    }

    @Override
    public void saveFavorites(List<FavItem> items) {
        if (realm.isClosed())
            return;
        realm.executeTransactionAsync(r -> {
            r.delete(FavItemBd.class);
            List<FavItemBd> bdList = new ArrayList<>();
            for (FavItem item : items) {
                bdList.add(new FavItemBd(item));
            }
            r.copyToRealmOrUpdate(bdList);
            bdList.clear();
        }, this::showFavorites);
    }

    @Override
    public void showFavorites() {
        RealmResults<FavItemBd> itemsBd = realm
                .where(FavItemBd.class)
                .findAll();
        Observable.fromIterable(itemsBd)
                .map(FavItem::new)
                .toList()
                .subscribe(items -> view.onShowFavorite(items));
    }

    @Override
    public void handleEvent(TabNotification event, Sorting sorting, int count) {
        if (event.isWebSocket() && event.getEvent().isNew()) return;
        RealmResults<FavItemBd> results = realm
                .where(FavItemBd.class)
                .findAll();
        ArrayList<IFavItem> currentItems = new ArrayList<>();
        for (FavItemBd itemBd : results) {
            currentItems.add(new FavItem(itemBd));
        }

        NotificationEvent loadedEvent = event.getEvent();
        int id = loadedEvent.getSourceId();
        boolean isRead = loadedEvent.isRead();

        if (isRead) {
            count--;
            for (IFavItem item : currentItems) {
                if (item.getTopicId() == id) {
                    item.setNew(false);
                    break;
                }
            }
        } else {
            count = event.getLoadedEvents().size();
            for (IFavItem item : currentItems) {
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
                for (IFavItem item : currentItems) {
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
        view.onHandleEvent(count);
    }

    @Override
    public void markRead(int topicId) {
        realm.executeTransactionAsync(realm1 -> {
            IFavItem favItem = realm1
                    .where(FavItemBd.class)
                    .equalTo("topicId", topicId)
                    .findFirst();
            if (favItem != null) {
                favItem.setNew(false);
            }
        });
    }
}
