package forpdateam.ru.forpda.apirx.apiclasses;

import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.api.topcis.models.TopicsData;
import io.reactivex.Observable;

/**
 * Created by radiationx on 25.03.17.
 */

public class TopicsRx {
    public Observable<TopicsData> getTopics(int id, int st) {
        return Observable.fromCallable(() -> Api.Topics().getTopics(id, st));
    }
}
