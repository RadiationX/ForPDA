package forpdateam.ru.forpda.model.repository;

import forpdateam.ru.forpda.api.reputation.Reputation;
import forpdateam.ru.forpda.api.reputation.models.RepData;
import forpdateam.ru.forpda.model.system.SchedulersProvider;
import io.reactivex.Observable;

/**
 * Created by radiationx on 03.01.18.
 */

public class ReputationRepository {

    private SchedulersProvider schedulers;
    private Reputation reputationApi;

    public ReputationRepository(SchedulersProvider schedulers, Reputation reputationApi) {
        this.schedulers = schedulers;
        this.reputationApi = reputationApi;
    }


    public Observable<RepData> loadReputation(RepData repData) {
        return Observable.fromCallable(() -> reputationApi.getReputation(repData))
                .subscribeOn(schedulers.io())
                .observeOn(schedulers.ui());
    }

    public Observable<String> changeReputation(int postId, int userId, boolean type, String message) {
        return Observable.fromCallable(() -> reputationApi.editReputation(postId, userId, type, message))
                .subscribeOn(schedulers.io())
                .observeOn(schedulers.ui());
    }
}
