package forpdateam.ru.forpda.fragments.news;

import android.support.annotation.NonNull;

import forpdateam.ru.forpda.BasePresenter;
import forpdateam.ru.forpda.BaseView;
import io.realm.RealmResults;

/**
 * Created by isanechek on 11/3/16.
 */

public interface NewsContract {
    interface View extends BaseView<Presenter> {
        void setLoadingIndicator(boolean active);
        void showNews(RealmResults<NewsModel> news);
        void showNewsDetailsUi(String newsId);
        void showLoadingNewsError();
        void showNoNews();

    }

    interface Presenter extends BasePresenter {
        void loadNews(boolean forceUpdate);
        void openTaskDetails(@NonNull NewsModel requestedNews);
    }
}
