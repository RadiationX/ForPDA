package forpdateam.ru.forpda.presentation.profile;

import forpdateam.ru.forpda.api.profile.models.ProfileModel;
import forpdateam.ru.forpda.common.mvp.IBaseView;

/**
 * Created by radiationx on 02.01.18.
 */

public interface ProfileView extends IBaseView {

    void showProfile(ProfileModel profile);

    void onSaveNote(boolean success);
}
