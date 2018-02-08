package forpdateam.ru.forpda.presentation.auth;

import forpdateam.ru.forpda.api.auth.models.AuthForm;
import forpdateam.ru.forpda.api.profile.models.ProfileModel;
import forpdateam.ru.forpda.common.mvp.IBaseView;

/**
 * Created by radiationx on 02.01.18.
 */

public interface AuthView extends IBaseView {

    void showForm(AuthForm authForm);

    void showLoginResult(boolean success);

    void showProfile(ProfileModel profile);
}
