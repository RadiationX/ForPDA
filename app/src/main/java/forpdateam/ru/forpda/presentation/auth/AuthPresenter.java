package forpdateam.ru.forpda.presentation.auth;

import com.arellomobile.mvp.InjectViewState;

import forpdateam.ru.forpda.api.auth.models.AuthForm;
import forpdateam.ru.forpda.common.mvp.BasePresenter;
import forpdateam.ru.forpda.model.repository.AuthRepository;
import forpdateam.ru.forpda.model.repository.ProfileRepository;
import io.reactivex.disposables.Disposable;

/**
 * Created by radiationx on 02.01.18.
 */

@InjectViewState
public class AuthPresenter extends BasePresenter<AuthView> {

    private AuthRepository authRepository;
    private ProfileRepository profileRepository;

    public AuthPresenter(AuthRepository authRepository, ProfileRepository profileRepository) {
        this.authRepository = authRepository;
        this.profileRepository = profileRepository;
    }

    public void loadForm() {
        Disposable disposable
                = authRepository.loadForm()
                .doOnTerminate(() -> getViewState().setRefreshing(true))
                .doAfterTerminate(() -> getViewState().setRefreshing(false))
                .subscribe(authForm -> getViewState().showForm(authForm), this::handleErrorRx);
        addToDisposable(disposable);
    }

    public void signIn(AuthForm authForm) {
        Disposable disposable
                = authRepository.signIn(authForm)
                .doOnTerminate(() -> getViewState().setRefreshing(true))
                .doAfterTerminate(() -> getViewState().setRefreshing(false))
                .subscribe(success -> getViewState().showLoginResult(success), this::handleErrorRx);
        addToDisposable(disposable);
    }

    public void loadProfile(String url) {
        Disposable disposable
                = profileRepository.loadProfile(url)
                .doOnTerminate(() -> getViewState().setRefreshing(true))
                .doAfterTerminate(() -> getViewState().setRefreshing(false))
                .subscribe(profileModel -> getViewState().showProfile(profileModel), this::handleErrorRx);
        addToDisposable(disposable);
    }
}
