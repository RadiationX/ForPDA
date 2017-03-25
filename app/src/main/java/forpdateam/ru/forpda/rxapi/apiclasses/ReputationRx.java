package forpdateam.ru.forpda.rxapi.apiclasses;

import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.api.reputation.models.RepData;
import io.reactivex.Observable;

/**
 * Created by radiationx on 25.03.17.
 */

public class ReputationRx {
    public Observable<RepData> getReputation(RepData data) {
        return Observable.fromCallable(() -> Api.Reputation().getReputation(data));
    }

    public Observable<String> editReputation(int postId, int userId, boolean type, String message) {
        return Observable.fromCallable(() -> Api.Reputation().editReputation(postId, userId, type, message));
    }
}
