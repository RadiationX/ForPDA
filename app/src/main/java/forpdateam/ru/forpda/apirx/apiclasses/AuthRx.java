package forpdateam.ru.forpda.apirx.apiclasses;

import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.api.auth.models.AuthForm;
import io.reactivex.Observable;

/**
 * Created by radiationx on 25.03.17.
 */

public class AuthRx {
    public Observable<AuthForm> getForm() {
        return Observable.fromCallable(() -> Api.Auth().getForm());
    }

    public Observable<Boolean> login(final AuthForm authForm) {
        return Observable.fromCallable(() -> Api.Auth().login(authForm));
    }

    public Observable<Boolean> logout() {
        return Observable.fromCallable(() -> Api.Auth().logout());
    }
}
