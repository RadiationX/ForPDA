package forpdateam.ru.forpda.api.auth;

import java.util.Observer;

import forpdateam.ru.forpda.api.auth.models.AuthForm;
import io.reactivex.Observable;

/**
 * Created by RadiationX on 12.08.2016.
 */
public class Auth {
    private AuthParser parser = new AuthParser();
    private LoginObservable observable = new LoginObservable();
    private boolean authState = false;

    public Auth() {
    }

    public boolean getState() {
        return authState;
    }

    public void setState(boolean b) {
        authState = b;
    }

    public Observable<AuthForm> getForm() {
        return parser.getForm();
    }

    public Observable<Boolean> tryLogin(final AuthForm authForm) {
        return parser.tryLogin(authForm);
    }

    public void tryLogout() throws Exception {
        parser.tryLogout();
    }

    public void addLoginObserver(Observer observer) {
        observable.addObserver(observer);
    }

    public void doOnLogin() {
        authState = true;
        observable.notifyObservers(true);
    }

    public void doOnLogout() {
        authState = false;
        observable.notifyObservers(false);
    }

    private class LoginObservable extends java.util.Observable {
        @Override
        public synchronized boolean hasChanged() {
            return true;
        }
    }
}
