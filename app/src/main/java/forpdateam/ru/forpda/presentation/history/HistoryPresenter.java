package forpdateam.ru.forpda.presentation.history;

import android.os.Bundle;

import com.arellomobile.mvp.InjectViewState;

import forpdateam.ru.forpda.common.IntentHandler;
import forpdateam.ru.forpda.common.Utils;
import forpdateam.ru.forpda.common.mvp.BasePresenter;
import forpdateam.ru.forpda.entity.app.history.HistoryItem;
import forpdateam.ru.forpda.model.repository.HistoryRepository;
import forpdateam.ru.forpda.ui.fragments.TabFragment;
import io.reactivex.disposables.Disposable;

/**
 * Created by radiationx on 11.11.17.
 */

@InjectViewState
public class HistoryPresenter extends BasePresenter<HistoryView> {

    private HistoryRepository historyRepository;

    public HistoryPresenter(HistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }

    public void getHistory() {
        Disposable disposable
                = historyRepository.getHistory()
                .doOnTerminate(() -> getViewState().setRefreshing(true))
                .doAfterTerminate(() -> getViewState().setRefreshing(false))
                .subscribe(historyItemBds -> {
                    getViewState().showHistory(historyItemBds);
                }, this::handleErrorRx);
        addToDisposable(disposable);
    }

    public void remove(int id) {
        Disposable disposable
                = historyRepository.remove(id)
                .doOnTerminate(() -> getViewState().setRefreshing(true))
                .doAfterTerminate(() -> getViewState().setRefreshing(false))
                .subscribe(this::getHistory, this::handleErrorRx);
        addToDisposable(disposable);
    }

    public void clear() {
        Disposable disposable
                = historyRepository.clear()
                .doOnTerminate(() -> getViewState().setRefreshing(true))
                .doAfterTerminate(() -> getViewState().setRefreshing(false))
                .subscribe(this::getHistory, this::handleErrorRx);
        addToDisposable(disposable);
    }

    public void copyLink(HistoryItem item) {
        Utils.copyToClipBoard(item.getUrl());
    }

    public void onItemClick(HistoryItem item) {
        Bundle args = new Bundle();
        args.putString(TabFragment.ARG_TITLE, item.getTitle());
        IntentHandler.handle(item.getUrl(), args);
    }

    public void onItemLongClick(HistoryItem item) {
        getViewState().showItemDialogMenu(item);
    }
}
