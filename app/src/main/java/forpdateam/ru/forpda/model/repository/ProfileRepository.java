package forpdateam.ru.forpda.model.repository;

import forpdateam.ru.forpda.api.profile.Profile;
import forpdateam.ru.forpda.api.profile.models.ProfileModel;
import forpdateam.ru.forpda.model.system.SchedulersProvider;
import io.reactivex.Observable;

/**
 * Created by radiationx on 02.01.18.
 */

public class ProfileRepository {

    private SchedulersProvider schedulers;
    private Profile profileApi;

    public ProfileRepository(SchedulersProvider schedulers, Profile profileApi) {
        this.schedulers = schedulers;
        this.profileApi = profileApi;
    }

    public Observable<ProfileModel> loadProfile(String url) {
        return Observable.fromCallable(() -> profileApi.getProfile(url))
                .subscribeOn(schedulers.io())
                .observeOn(schedulers.ui());
    }

    public Observable<Boolean> saveNote(String note) {
        return Observable.fromCallable(() -> profileApi.saveNote(note))
                .subscribeOn(schedulers.io())
                .observeOn(schedulers.ui());
    }
}
