package forpdateam.ru.forpda.rxapi.apiclasses;

import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.api.mentions.models.MentionsData;
import io.reactivex.Observable;

/**
 * Created by radiationx on 25.03.17.
 */

public class MentionsRx {
    public Observable<MentionsData> getMentions(int st) {
        return Observable.fromCallable(() -> Api.Mentions().getMentions(st));
    }
}
