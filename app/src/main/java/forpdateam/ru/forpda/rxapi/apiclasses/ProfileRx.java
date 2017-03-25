package forpdateam.ru.forpda.rxapi.apiclasses;

import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.api.profile.models.ProfileModel;
import io.reactivex.Observable;

/**
 * Created by radiationx on 25.03.17.
 */

public class ProfileRx {
    public Observable<ProfileModel> getProfile(String url) {
        return Observable.fromCallable(() -> Api.Profile().getProfile(url));
    }

    public Observable<Boolean> saveNote(final String note) {
        return Observable.fromCallable(() -> Api.Profile().saveNote(note));
    }
}
