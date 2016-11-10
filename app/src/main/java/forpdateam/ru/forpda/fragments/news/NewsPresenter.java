package forpdateam.ru.forpda.fragments.news;

import android.support.annotation.NonNull;

import forpdateam.ru.forpda.data.Repository;
import forpdateam.ru.forpda.utils.schedulers.BaseSchedulerProvider;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.realm.RealmResults;

import static forpdateam.ru.forpda.utils.Utils.checkNotNull;

/**
 * Created by isanechek on 11/3/16.
 */

public class NewsPresenter implements NewsContract.Presenter {

    @NonNull
    private final Repository repository;

    @NonNull
    private final NewsContract.View view;

    @NonNull
    private final BaseSchedulerProvider scheduler;

    private boolean firstLoad = true;

    @NonNull
    private CompositeDisposable subscriptions;

    public NewsPresenter(@NonNull Repository repository,
                         @NonNull NewsContract.View view,
                         @NonNull BaseSchedulerProvider scheduler) {
        this.repository = checkNotNull(repository, "Repository cannot be null");
        this.view = checkNotNull(view, "View cannot be null!");
        this.scheduler = checkNotNull(scheduler, "SchedulerProvider cannot be null");

        subscriptions = new CompositeDisposable();
        view.setPresenter(this);
    }

    @Override
    public void loadNews(boolean forceUpdate) {

    }

    @Override
    public void openTaskDetails(@NonNull NewsModel requestedNews) {

    }

    @Override
    public void subscribe() {

    }

    @Override
    public void unSubscribe() {

    }

    private void loadNews(final String category,
                          final boolean forceUpdate,
                          final boolean showLoadingUI) {
        if (showLoadingUI) {
            view.setLoadingIndicator(true);
        }

        if (forceUpdate) {
            repository.refreshNewsList(category);
        }

        subscriptions.clear();


    }
}
