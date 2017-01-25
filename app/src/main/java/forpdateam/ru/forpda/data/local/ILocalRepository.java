package forpdateam.ru.forpda.data.local;

import android.support.annotation.NonNull;

import java.util.List;

import forpdateam.ru.forpda.fragments.news.models.NewsModel;
import io.reactivex.Single;

/**
 * Created by isanechek on 13.01.17.
 */

public interface ILocalRepository {

    /*News*/
    Single<List<NewsModel>> getLocalNewsList(@NonNull String category);
    List<NewsModel> getLocalNewsList2(@NonNull String category);

    void saveNewsToRealm (NewsModel model);
    void saveNewsToRealm2(List<NewsModel> list);

    void deleteNewsFromRealm(@NonNull String category);
}
