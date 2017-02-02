package forpdateam.ru.forpda.data.local;

import android.support.annotation.NonNull;

import java.util.List;

import forpdateam.ru.forpda.fragments.news.models.NewsModel;
import io.reactivex.Single;
import io.realm.Realm;

import static forpdateam.ru.forpda.utils.Utils.log;

/**
 * Created by isanechek on 13.01.17.
 */

public class LocalRepository implements ILocalRepository {

    private static final String TAG = "LocalRepository";
    private static LocalRepository INSTANCE;

    private Realm realm;

    public static LocalRepository getInstance(Realm realm) {
        if (INSTANCE == null) {
            INSTANCE = new LocalRepository(realm);
        }
        return INSTANCE;
    }

    public static void removeInstance() {
        log(TAG + " remove local repo");
        INSTANCE = null;
    }

    private LocalRepository(Realm realm) {
        this.realm = realm;
        log(TAG + " Local Repository");
    }


    @Override
    public Single<List<NewsModel>> getLocalNewsList(@NonNull String category) {
        log(TAG + " getLocalNewsList -> category -> " + category);
        return Single.fromCallable(() -> getLocalNewsList2(category));
    }

    @Override
    public List<NewsModel> getLocalNewsList2(@NonNull String category) {
        log(TAG + " getLocalNewsList2 -> category -> " + category);
        return realm.where(NewsModel.class).equalTo("category", category).findAll();
    }

    @Override
    public void saveNewsToRealm(NewsModel model) {
        log(TAG + " saveNewsToRealm -> " + model.getTitle());
        realm.executeTransaction(r -> r.insertOrUpdate(model));
    }

    @Override
    public void saveNewsToRealm2(List<NewsModel> list) {
        log(TAG + " saveNewsToRealm2 -> " + list.size());
        realm.executeTransaction(r -> r.insertOrUpdate(list));
    }

    @Override
    public void deleteNewsFromRealm(@NonNull String category) {
        log("delete from realm " + category);
        realm.executeTransaction(r -> r
                .where(NewsModel.class)
                .equalTo(NewsModel.CATEGORY, category)
                .findAll()
                .deleteAllFromRealm());
    }

}
