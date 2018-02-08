package forpdateam.ru.forpda.presentation.forum;

import android.os.Bundle;

import com.arellomobile.mvp.InjectViewState;

import forpdateam.ru.forpda.api.forum.models.ForumItemTree;
import forpdateam.ru.forpda.common.Utils;
import forpdateam.ru.forpda.common.mvp.BasePresenter;
import forpdateam.ru.forpda.model.repository.ForumRepository;
import forpdateam.ru.forpda.ui.TabManager;
import forpdateam.ru.forpda.ui.fragments.TabFragment;
import forpdateam.ru.forpda.ui.fragments.search.SearchFragment;
import forpdateam.ru.forpda.ui.fragments.topics.TopicsFragment;
import io.reactivex.disposables.Disposable;

/**
 * Created by radiationx on 03.01.18.
 */

@InjectViewState
public class ForumPresenter extends BasePresenter<ForumView> {

    private ForumRepository forumRepository;

    public ForumPresenter(ForumRepository forumRepository) {
        this.forumRepository = forumRepository;
    }

    public void loadForums() {
        Disposable disposable
                = forumRepository.getForums()
                .doOnTerminate(() -> getViewState().setRefreshing(true))
                .doAfterTerminate(() -> getViewState().setRefreshing(false))
                .subscribe(forums -> {
                    getViewState().showForums(forums);
                    saveCacheForums(forums);
                }, this::handleErrorRx);
        addToDisposable(disposable);
    }

    public void getCacheForums() {
        Disposable disposable
                = forumRepository.getCache()
                .doOnTerminate(() -> getViewState().setRefreshing(true))
                .doAfterTerminate(() -> getViewState().setRefreshing(false))
                .subscribe(forums -> {
                    if (forums.getForums() == null) {
                        loadForums();
                    } else {
                        getViewState().showForums(forums);
                    }
                }, this::handleErrorRx);
        addToDisposable(disposable);
    }

    private void saveCacheForums(ForumItemTree rootForum) {
        Disposable disposable
                = forumRepository.saveCache(rootForum)
                .doOnTerminate(() -> getViewState().setRefreshing(true))
                .doAfterTerminate(() -> getViewState().setRefreshing(false))
                .subscribe(() -> {

                }, this::handleErrorRx);
        addToDisposable(disposable);
    }

    public void copyLink(ForumItemTree item) {
        Utils.copyToClipBoard("https://4pda.ru/forum/index.php?showforum=" + item.getId());
    }

    public void navigateToForum(ForumItemTree item) {
        Bundle args = new Bundle();
        args.putInt(TopicsFragment.TOPICS_ID_ARG, item.getId());
        TabManager.get().add(TopicsFragment.class, args);
    }

    public void navigateToSearch(ForumItemTree item) {
        String url = "https://4pda.ru/forum/index.php?act=search&source=all&forums%5B%5D=" + item.getId();
        Bundle args = new Bundle();
        args.putString(TabFragment.ARG_TAB, url);
        TabManager.get().add(SearchFragment.class, args);
    }
}
