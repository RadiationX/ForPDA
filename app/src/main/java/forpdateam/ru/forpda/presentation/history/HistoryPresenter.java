package forpdateam.ru.forpda.presentation.history;

import com.arellomobile.mvp.InjectViewState;

import forpdateam.ru.forpda.common.mvp.BasePresenter;
import forpdateam.ru.forpda.model.repository.HistoryRepository;
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
}
