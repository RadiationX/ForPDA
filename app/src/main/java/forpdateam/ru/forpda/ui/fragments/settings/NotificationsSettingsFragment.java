package forpdateam.ru.forpda.ui.fragments.settings;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;

import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.ui.activities.SettingsActivity;

/**
 * Created by radiationx on 12.07.17.
 */

public class NotificationsSettingsFragment extends BaseSettingFragment {
    public final static String PREFERENCE_SCREEN_NAME = "notifications";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences_notifications);
        ActionBar actionBar = ((SettingsActivity) getActivity()).getSupportActionBar();
        if (actionBar != null)
            actionBar.setTitle(getPreferenceScreen().getTitle());
    }
}
