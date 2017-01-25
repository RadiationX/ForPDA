package forpdateam.ru.forpda.fragments.news;

import android.support.annotation.NonNull;

import java.util.List;

import forpdateam.ru.forpda.fragments.news.models.NewsModel;
import forpdateam.ru.forpda.fragments.news.models.NewsCallbackModel;

/**
 * Created by isanechek on 12.01.17.
 */

public interface INewsView {
    void showData(List<NewsModel> list);
    void showUpdateData(NewsCallbackModel model);
    void showLoadMore(List<NewsModel> list, boolean hide);
    void showMoreNewNews(NewsCallbackModel model);
    void showUpdateProgress(boolean show);
    void showBackgroundWorkProgress(boolean show);
    void showErrorView(Throwable throwable, @NonNull String codeError);
    void showPopUp(@NonNull String viewCode);
}
