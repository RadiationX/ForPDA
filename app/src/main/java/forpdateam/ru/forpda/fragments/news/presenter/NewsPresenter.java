package forpdateam.ru.forpda.fragments.news.presenter;

import android.support.annotation.NonNull;

import forpdateam.ru.forpda.data.Repository;
import forpdateam.ru.forpda.fragments.news.INewsView;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;

import static forpdateam.ru.forpda.Constants.ERROR_LOAD_DATA;
import static forpdateam.ru.forpda.Constants.ERROR_LOAD_MORE_NEW_DATA;
import static forpdateam.ru.forpda.Constants.ERROR_UPDATE_DATA;
import static forpdateam.ru.forpda.utils.Utils.log;

/**
 * Created by isanechek on 12.01.17.
 */

public class NewsPresenter implements INewsPresenter {
    private static final String TAG = "NewsPresenter";

    private CompositeDisposable disposable;
    private INewsView newsView;
    private Repository repository;

    @Override
    public void bindView(INewsView iNewsView, Repository repository) {
        this.newsView = iNewsView;
        this.repository = repository;
        disposable = new CompositeDisposable();
        log(TAG + " bindView");
    }

    @Override
    public void unbindView() {
        newsView = null;
        disposable.dispose();
        log(TAG + " unbindView");
    }

    /**
     * Основной метод для показа данных из сети или базы.
     * @param category - категория новостей.
     */
    @Override
    public void loadData(String category) {
        newsView.showUpdateProgress(true);
        disposable.add(repository.getNewsList(category)
                .subscribe(newsCallbackModel -> {
                    newsView.showUpdateProgress(false);
                    newsView.showData(newsCallbackModel.getCache());
                    log("load data ->> " + newsCallbackModel.getCache().size());
                    if (!newsCallbackModel.isFromNetwork()) {
                        updateData(category, true); // тут запускаем обновление, ибо данные из бд.
                    }
                }, throwable -> {
                    log("load data error >> " + throwable.toString());
                    newsView.showUpdateProgress(false);
                    newsView.showErrorView(throwable, ERROR_LOAD_DATA);
                }));

    }

    /**
     * Основной метод обноления списка и бд.
     * @param category - категория новостей.
     */
    @Override
    public void updateData(@NonNull String category, boolean background) {
        log(TAG + " updateNewsListData -> " + " category -> " + category);
        if (background) {
            newsView.showBackgroundWorkProgress(true);
        }
        disposable.add(repository.updateNewsListData(category)
                .subscribe(newsCallbackModel -> {
                    log(TAG + " updateData " + newsCallbackModel.getCache().size());
                    if (background) { newsView.showBackgroundWorkProgress(false);}
                    else { newsView.showUpdateProgress(false);}
                    newsView.showUpdateData(newsCallbackModel);
                }, throwable -> {
                    log(TAG + " updateData Error ->> " + throwable.toString());
                    if (background) { newsView.showBackgroundWorkProgress(false);}
                    else { newsView.showUpdateProgress(false);}
                    newsView.showErrorView(throwable, ERROR_UPDATE_DATA);
                }));
    }

    /**
     *
     * @param category
     * @param pageNumber
     * @param lastUrl
     */
    @Override
    public void loadMoreNewItems(@NonNull String category, int pageNumber, String lastUrl) {
        log(TAG + " LoadMoreNewItems");
        disposable.add(repository.getLoadMoreNewsListData(category, pageNumber, lastUrl)
                .subscribe(newsCallbackModel -> {
                    newsView.showMoreNewNews(newsCallbackModel);
                }, throwable -> {

                    newsView.showErrorView(throwable, ERROR_LOAD_MORE_NEW_DATA);
                }));
    }

    /**
     *
     * @param category
     * @param pageNumber
     */
    @Override
    public void loadMore(@NonNull String category, int pageNumber) {
        disposable.add(repository.loadMoreNewsItems(category, pageNumber)
                .subscribe(list -> newsView.showLoadMore(list, false),
                        new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {

                    }
                }));
    }
}
