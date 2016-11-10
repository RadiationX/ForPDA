package forpdateam.ru.forpda.data;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import forpdateam.ru.forpda.fragments.news.NewsModel;
import io.reactivex.Observable;
import io.realm.RealmResults;

/**
 * Created by isanechek on 11/10/16.
 */

public class LocalRepository implements DataSource.LocalDataSource {

    @Nullable
    private static LocalRepository INSTANCE = null;


    public LocalRepository() {
    }

    public static LocalRepository getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new LocalRepository();
        }
        return INSTANCE;
    }

    @Override
    public Observable<RealmResults<NewsModel>> getListNewsAll() {
        return null;
    }

    @Override
    public Observable<RealmResults<NewsModel>> getListNewsCategory(@NonNull String category) {
        return null;
    }


    @Override
    public void readNewsState(@NonNull String idNews) {

    }

    @Override
    public void saveOfflineNews(@NonNull String idNews, @NonNull String source) {
    }

    @Override
    public void deleteThemeLastPostUrl(@NonNull String idTheme) {

    }

    @Override
    public void saveThemeLastPostUrl(@NonNull String idTheme, @NonNull String postUrl) {

    }

    @Override
    public Observable<String> getThemeLastPostUrl(@NonNull String idTheme) {
        return null;
    }
}