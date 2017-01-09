package forpdateam.ru.forpda.settings;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import forpdateam.ru.forpda.R;

/**
 * Created by radiationx on 25.12.16.
 */

public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}
