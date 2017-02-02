package forpdateam.ru.forpda.data;

import android.support.annotation.NonNull;

import java.util.List;

import forpdateam.ru.forpda.fragments.news.models.NewsCallbackModel;
import forpdateam.ru.forpda.fragments.news.models.NewsModel;
import io.reactivex.Single;

/**
 * Created by isanechek on 13.01.17.
 */

public interface IRepository {
    /*NEWS*/
    Single<NewsCallbackModel> getNewsList(@NonNull String category);
    Single<NewsCallbackModel> getLoadMoreNewsListData(@NonNull String category, int pageNumber, String lastUrl);
    Single<NewsCallbackModel> updateNewsListData(@NonNull String category);
    Single<List<NewsModel>> loadMoreNewsItems(@NonNull String category, int pageNumber);

}
