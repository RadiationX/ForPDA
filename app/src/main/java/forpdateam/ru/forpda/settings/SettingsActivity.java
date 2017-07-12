package forpdateam.ru.forpda.settings;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import java.util.Observer;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;

/**
 * Created by radiationx on 25.12.16.
 */

public class SettingsActivity extends AppCompatActivity {
    public final static String ARG_NEW_PREFERENCE_SCREEN = "new_preference_screen";
    private Observer appThemeChangeObserver = (observable, o) -> {
        if (o == null) return;
        String key = (String) o;
        switch (key) {
            case Preferences.Main.Theme.IS_DARK: {
                recreate();
                break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(App.getInstance().isDarkTheme() ? R.style.DarkAppTheme : R.style.LightAppTheme);
        setContentView(R.layout.activity_settings);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);
        }
        PreferenceFragment fragment = null;
        Intent intent = getIntent();
        if (intent != null) {
            String settingsArgument = intent.getStringExtra(ARG_NEW_PREFERENCE_SCREEN);
            if (settingsArgument != null) {
                if (settingsArgument.equals(NotificationsSettingsFragment.PREFERENCE_SCREEN_NAME)) {
                    fragment = new NotificationsSettingsFragment();
                }
            }
        }
        if (fragment == null) {
            fragment = new SettingsFragment();
        }

        getFragmentManager().beginTransaction().replace(R.id.fragment_content, fragment).commit();


        App.getInstance().addPreferenceChangeObserver(appThemeChangeObserver);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
            finish();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        App.getInstance().removePreferenceChangeObserver(appThemeChangeObserver);
    }
}
