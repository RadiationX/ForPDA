package forpdateam.ru.forpda.presentation.favorites;

import com.arellomobile.mvp.InjectViewState;

import java.util.List;

import forpdateam.ru.forpda.api.favorites.Sorting;
import forpdateam.ru.forpda.api.favorites.models.FavItem;
import forpdateam.ru.forpda.common.mvp.BasePresenter;
import forpdateam.ru.forpda.entity.app.TabNotification;
import forpdateam.ru.forpda.model.repository.FavoritesRepository;
import io.reactivex.disposables.Disposable;

/**
 * Created by radiationx on 11.11.17.
 */

@InjectViewState
public class FavoritesPresenter extends BasePresenter<FavoritesView> {
    private FavoritesRepository favoritesRepository;

    public FavoritesPresenter(FavoritesRepository favoritesRepository) {
        this.favoritesRepository = favoritesRepository;
    }

    public void getFavorites(int st, boolean all, Sorting sorting) {
        Disposable disposable
                = favoritesRepository.loadFavorites(st, all, sorting)
                .doOnTerminate(() -> getViewState().setRefreshing(true))
                .doAfterTerminate(() -> getViewState().setRefreshing(false))
                .subscribe(favData -> getViewState().onLoadFavorites(favData), this::handleErrorRx);
        addToDisposable(disposable);
    }

    public void saveFavorites(List<FavItem> items) {
        Disposable disposable
                = favoritesRepository.saveFavorites(items)
                .subscribe(this::showFavorites, this::handleErrorRx);
        addToDisposable(disposable);
    }

    public void showFavorites() {
        favoritesRepository.getCache()
                .subscribe(favItems -> getViewState().onShowFavorite(favItems));
    }

    public void markRead(int topicId) {
        Disposable disposable
                = favoritesRepository.markRead(topicId)
                .subscribe(this::showFavorites, this::handleErrorRx);
        addToDisposable(disposable);
    }

    public void handleEvent(TabNotification event, Sorting sorting, int count) {
        if (event.isWebSocket() && event.getEvent().isNew()) return;
        Disposable disposable
                = favoritesRepository.handleEvent(event, sorting, count)
                .subscribe(integer -> getViewState().onHandleEvent(integer), this::handleErrorRx);
        addToDisposable(disposable);
    }
}
