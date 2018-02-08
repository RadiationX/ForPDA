package forpdateam.ru.forpda.presentation.favorites;

import android.os.Bundle;

import com.arellomobile.mvp.InjectViewState;

import java.util.List;

import forpdateam.ru.forpda.api.favorites.Sorting;
import forpdateam.ru.forpda.api.favorites.models.FavItem;
import forpdateam.ru.forpda.common.IntentHandler;
import forpdateam.ru.forpda.common.Utils;
import forpdateam.ru.forpda.common.mvp.BasePresenter;
import forpdateam.ru.forpda.entity.app.TabNotification;
import forpdateam.ru.forpda.model.repository.FavoritesRepository;
import forpdateam.ru.forpda.ui.fragments.TabFragment;
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
        Disposable disposable
                = favoritesRepository.getCache()
                .subscribe(favItems -> getViewState().onShowFavorite(favItems));
        addToDisposable(disposable);
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


    public void onItemClick(FavItem item) {
        Bundle args = new Bundle();
        args.putString(TabFragment.ARG_TITLE, item.getTopicTitle());
        if (item.isForum()) {
            IntentHandler.handle("https://4pda.ru/forum/index.php?showforum=" + item.getForumId(), args);
        } else {
            IntentHandler.handle("https://4pda.ru/forum/index.php?showtopic=" + item.getTopicId() + "&view=getnewpost", args);
        }
    }

    public void onItemLongClick(FavItem item) {
        getViewState().showItemDialogMenu(item);
    }

    public void copyLink(FavItem item) {
        if (item.isForum()) {
            Utils.copyToClipBoard("https://4pda.ru/forum/index.php?showforum=".concat(Integer.toString(item.getForumId())));
        } else {
            Utils.copyToClipBoard("https://4pda.ru/forum/index.php?showtopic=".concat(Integer.toString(item.getTopicId())));
        }
    }

    public void openAttachments(FavItem item) {
        IntentHandler.handle("https://4pda.ru/forum/index.php?act=attach&code=showtopic&tid=" + item.getTopicId());
    }

    public void openForum(FavItem item) {
        IntentHandler.handle("https://4pda.ru/forum/index.php?showforum=" + item.getForumId());
    }

    public void changeFav(int action, String type, int favId) {
        getViewState().changeFav(action, type, favId);
    }

    public void showSubscribeDialog(FavItem item) {
        getViewState().showSubscribeDialog(item);
    }
}
