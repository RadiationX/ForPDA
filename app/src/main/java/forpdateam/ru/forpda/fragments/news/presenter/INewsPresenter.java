package forpdateam.ru.forpda.fragments.news.presenter;

import android.support.annotation.NonNull;

import forpdateam.ru.forpda.data.Repository;
import forpdateam.ru.forpda.fragments.news.INewsView;

/**
 * Created by isanechek on 12.01.17.
 */

public interface INewsPresenter {

    void bindView(INewsView iNewsView, Repository repository);
    void unbindView();

    void loadData(String category);
    void updateData(@NonNull String category, boolean background);
    void loadMoreNewItems(@NonNull String category, int pageNumber, String lastUrl);
    void loadMore(@NonNull String category, int pageNumber);
}
