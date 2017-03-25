package forpdateam.ru.forpda.rxapi.apiclasses;

import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.api.forum.models.ForumItemTree;
import io.reactivex.Observable;

/**
 * Created by radiationx on 25.03.17.
 */

public class ForumRx {
    public Observable<ForumItemTree> getForums() {
        return Observable.fromCallable(() -> Api.Forum().getForums());
    }
}
