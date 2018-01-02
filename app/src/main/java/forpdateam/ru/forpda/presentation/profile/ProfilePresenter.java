package forpdateam.ru.forpda.presentation.profile;

import com.arellomobile.mvp.InjectViewState;

import forpdateam.ru.forpda.common.mvp.BasePresenter;
import forpdateam.ru.forpda.model.repository.AuthRepository;
import forpdateam.ru.forpda.model.repository.ProfileRepository;
import io.reactivex.disposables.Disposable;

/**
 * Created by radiationx on 02.01.18.
 */

@InjectViewState
public class ProfilePresenter extends BasePresenter<ProfileView> {

    private ProfileRepository profileRepository;

    public ProfilePresenter(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    public void loadProfile(String url) {
        Disposable disposable
                = profileRepository.loadProfile(url)
                .doOnTerminate(() -> getViewState().setRefreshing(true))
                .doAfterTerminate(() -> getViewState().setRefreshing(false))
                .subscribe(profileModel -> getViewState().showProfile(profileModel), this::handleErrorRx);
        addToDisposable(disposable);
    }

    public void saveNote(String note) {
        Disposable disposable
                = profileRepository.saveNote(note)
                .doOnTerminate(() -> getViewState().setRefreshing(true))
                .doAfterTerminate(() -> getViewState().setRefreshing(false))
                .subscribe(success -> getViewState().onSaveNote(success), this::handleErrorRx);
        addToDisposable(disposable);
    }
}
