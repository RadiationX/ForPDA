package forpdateam.ru.forpda.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.view.MenuItem;

import java.util.Observer;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.common.LocaleHelper;
import forpdateam.ru.forpda.common.Preferences;
import forpdateam.ru.forpda.ui.fragments.settings.NotificationsSettingsFragment;
import forpdateam.ru.forpda.ui.fragments.settings.SettingsFragment;

/**
 * Created by radiationx on 25.12.16.
 */

public class SettingsActivity extends AppCompatActivity {
    public final static String ARG_NEW_PREFERENCE_SCREEN = "new_preference_screen";
    private boolean currentThemeIsDark = App.get().isDarkTheme();
    private Observer appThemeChangeObserver = (observable, o) -> {
        if (o == null) return;
        String key = (String) o;
        switch (key) {
            case Preferences.Main.Theme.IS_DARK: {
                boolean themeIsDark = App.get().isDarkTheme();
                if (currentThemeIsDark != themeIsDark) {
                    currentThemeIsDark = themeIsDark;
                    recreate();
                }
                break;
            }
        }
    };

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentThemeIsDark = App.get().isDarkTheme();
        setTheme(currentThemeIsDark ? R.style.PreferenceAppThemeDark : R.style.PreferenceAppThemeLight);
        setContentView(R.layout.activity_settings);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setTitle(R.string.activity_title_settings);
        }
        PreferenceFragmentCompat fragment = null;
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

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_content, fragment).commit();

        /*View view = findViewById(R.id.fragment_content);
        view.setBackgroundColor(Color.TRANSPARENT);
        view.setBackgroundColor(Color.rgb(4, 26, 55));*/

        App.get().addPreferenceChangeObserver(appThemeChangeObserver);
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
        App.get().removePreferenceChangeObserver(appThemeChangeObserver);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        App.get().onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
