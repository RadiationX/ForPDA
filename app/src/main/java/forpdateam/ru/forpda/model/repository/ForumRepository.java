package forpdateam.ru.forpda.model.repository;

import java.util.ArrayList;
import java.util.List;

import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.api.favorites.models.FavItem;
import forpdateam.ru.forpda.api.forum.Forum;
import forpdateam.ru.forpda.api.forum.models.Announce;
import forpdateam.ru.forpda.api.forum.models.ForumItemFlat;
import forpdateam.ru.forpda.api.forum.models.ForumItemTree;
import forpdateam.ru.forpda.api.forum.models.ForumRules;
import forpdateam.ru.forpda.entity.db.favorites.FavItemBd;
import forpdateam.ru.forpda.entity.db.forum.ForumItemFlatBd;
import forpdateam.ru.forpda.model.system.SchedulersProvider;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by radiationx on 03.01.18.
 */

public class ForumRepository {

    private SchedulersProvider schedulers;
    private Forum forumApi;

    public ForumRepository(SchedulersProvider schedulers, Forum forumApi) {
        this.schedulers = schedulers;
        this.forumApi = forumApi;
    }

    public Observable<ForumItemTree> getForums() {
        return Observable.fromCallable(() -> forumApi.getForums())
                .subscribeOn(schedulers.io())
                .observeOn(schedulers.ui());
    }

    public Observable<ForumItemTree> getCache() {
        return Observable.fromCallable(() -> {
            List<ForumItemFlat> items = new ArrayList<>();
            try (Realm realm = Realm.getDefaultInstance()) {
                RealmResults<ForumItemFlatBd> results = realm
                        .where(ForumItemFlatBd.class)
                        .findAll();
                for (ForumItemFlatBd itemBd : results) {
                    items.add(new ForumItemFlat(itemBd));
                }
            }
            ForumItemTree forumItemTree = new ForumItemTree();
            forumApi.transformToTree(items, forumItemTree);
            return forumItemTree;
        })
                .subscribeOn(schedulers.io())
                .observeOn(schedulers.ui());
    }

    public Completable saveCache(ForumItemTree rootForum) {
        return Completable.fromRunnable(() -> {
            try (Realm realm = Realm.getDefaultInstance()) {
                realm.executeTransaction(r -> {
                    r.delete(ForumItemFlatBd.class);
                    List<ForumItemFlatBd> items = new ArrayList<>();
                    transformToList(items, rootForum);
                    r.copyToRealmOrUpdate(items);
                    items.clear();
                });
            }
        })
                .subscribeOn(schedulers.io())
                .observeOn(schedulers.ui());
    }

    private void transformToList(List<ForumItemFlatBd> list, ForumItemTree rootForum) {
        if (rootForum.getForums() == null) return;
        for (ForumItemTree item : rootForum.getForums()) {
            list.add(new ForumItemFlatBd(item));
            transformToList(list, item);
        }
    }

}
