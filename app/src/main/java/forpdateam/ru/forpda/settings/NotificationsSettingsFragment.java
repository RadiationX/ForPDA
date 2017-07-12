package forpdateam.ru.forpda.settings;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;

import forpdateam.ru.forpda.R;

/**
 * Created by radiationx on 12.07.17.
 */

public class NotificationsSettingsFragment extends PreferenceFragment {
    public final static String PREFERENCE_SCREEN_NAME = "notifications";
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences_notifications);
        ((SettingsActivity) getActivity()).getSupportActionBar().setTitle(getPreferenceScreen().getTitle());
    }
}
