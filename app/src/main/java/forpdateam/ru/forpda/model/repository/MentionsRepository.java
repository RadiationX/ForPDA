package forpdateam.ru.forpda.model.repository;

import forpdateam.ru.forpda.api.mentions.Mentions;
import forpdateam.ru.forpda.api.mentions.models.MentionsData;
import forpdateam.ru.forpda.model.system.SchedulersProvider;
import io.reactivex.Observable;

/**
 * Created by radiationx on 01.01.18.
 */

public class MentionsRepository {
    private Mentions mentionsApi;
    private SchedulersProvider schedulers;

    public MentionsRepository(SchedulersProvider schedulers, Mentions mentionsApi) {
        this.mentionsApi = mentionsApi;
        this.schedulers = schedulers;
    }

    public Observable<MentionsData> getMentions(int page) {
        return Observable.fromCallable(() -> mentionsApi.getMentions(page))
                .subscribeOn(schedulers.io())
                .observeOn(schedulers.ui());
    }
}
