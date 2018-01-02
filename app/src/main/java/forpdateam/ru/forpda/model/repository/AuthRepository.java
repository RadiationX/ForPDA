package forpdateam.ru.forpda.model.repository;

import forpdateam.ru.forpda.api.auth.Auth;
import forpdateam.ru.forpda.api.auth.models.AuthForm;
import forpdateam.ru.forpda.api.favorites.Favorites;
import forpdateam.ru.forpda.model.system.SchedulersProvider;
import io.reactivex.Observable;

/**
 * Created by radiationx on 02.01.18.
 */

public class AuthRepository {

    private SchedulersProvider schedulers;
    private Auth authApi;

    public AuthRepository(SchedulersProvider schedulers, Auth authApi) {
        this.schedulers = schedulers;
        this.authApi = authApi;
    }

    public Observable<AuthForm> loadForm() {
        return Observable.fromCallable(() -> authApi.getForm())
                .subscribeOn(schedulers.io())
                .observeOn(schedulers.ui());
    }

    public Observable<Boolean> signIn(AuthForm authForm) {
        return Observable.fromCallable(() -> authApi.login(authForm))
                .subscribeOn(schedulers.io())
                .observeOn(schedulers.ui());
    }

}
