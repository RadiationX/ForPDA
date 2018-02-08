package forpdateam.ru.forpda.presentation.reputation;

import com.arellomobile.mvp.InjectViewState;

import forpdateam.ru.forpda.api.reputation.Reputation;
import forpdateam.ru.forpda.api.reputation.models.RepData;
import forpdateam.ru.forpda.api.reputation.models.RepItem;
import forpdateam.ru.forpda.common.IntentHandler;
import forpdateam.ru.forpda.common.mvp.BasePresenter;
import forpdateam.ru.forpda.model.repository.ReputationRepository;
import io.reactivex.disposables.Disposable;

/**
 * Created by radiationx on 03.01.18.
 */

@InjectViewState
public class ReputationPresenter extends BasePresenter<ReputationView> {

    private RepData currentData = new RepData();

    private ReputationRepository reputationRepository;

    public ReputationPresenter(ReputationRepository reputationRepository) {
        this.reputationRepository = reputationRepository;
    }

    public void setCurrentData(RepData data) {
        currentData = data;
    }

    public RepData getCurrentData() {
        return currentData;
    }

    public void loadReputation() {
        Disposable disposable
                = reputationRepository.loadReputation(currentData)
                .doOnTerminate(() -> getViewState().setRefreshing(true))
                .doAfterTerminate(() -> getViewState().setRefreshing(false))
                .subscribe(repData1 -> getViewState().showReputation(repData1), this::handleErrorRx);

        addToDisposable(disposable);
    }

    public void changeReputation(boolean type, String message) {
        Disposable disposable
                = reputationRepository.changeReputation(0, currentData.getId(), type, message)
                .doOnTerminate(() -> getViewState().setRefreshing(true))
                .doAfterTerminate(() -> getViewState().setRefreshing(false))
                .subscribe(result -> {
                    getViewState().onChangeReputation(result);
                    loadReputation();
                }, this::handleErrorRx);

        addToDisposable(disposable);
    }

    public void selectPage(int page) {
        currentData.getPagination().setSt(page);
        loadReputation();
    }

    public void setSort(String sort) {
        currentData.setSort(sort);
        loadReputation();
    }

    public void changeReputationMode() {
        currentData.setMode(currentData.getMode().equals(Reputation.MODE_FROM) ? Reputation.MODE_TO : Reputation.MODE_FROM);
        loadReputation();
    }

    public void onItemClick(RepItem item) {
        getViewState().showItemDialogMenu(item);
    }

    public void onItemLongClick(RepItem item) {
        getViewState().showItemDialogMenu(item);
    }

    public void navigateToProfile(int userId) {
        IntentHandler.handle("https://4pda.ru/forum/index.php?showuser=" + userId);
    }

    public void navigateToMessage(RepItem item) {
        IntentHandler.handle(item.getSourceUrl());
    }
}
