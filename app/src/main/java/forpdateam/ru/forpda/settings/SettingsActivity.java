package forpdateam.ru.forpda.settings;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

import java.util.Observer;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;

/**
 * Created by radiationx on 25.12.16.
 */

public class SettingsActivity extends AppCompatActivity {
    public final static String ARG_NEW_PREFERENCE_SCREEN = "new_preference_screen";
    private boolean currentThemeIsDark = App.getInstance().isDarkTheme();
    private Observer appThemeChangeObserver = (observable, o) -> {
        if (o == null) return;
        String key = (String) o;
        switch (key) {
            case Preferences.Main.Theme.IS_DARK: {
                boolean themeIsDark = App.getInstance().isDarkTheme();
                Log.d("SETTINGS_WTF", "IS DARK: " + currentThemeIsDark + " : " + themeIsDark);
                if (currentThemeIsDark != themeIsDark) {
                    currentThemeIsDark = themeIsDark;
                    recreate();
                }
                break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentThemeIsDark = App.getInstance().isDarkTheme();
        setTheme(currentThemeIsDark ? R.style.DarkAppTheme : R.style.LightAppTheme);
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        App.getInstance().onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
