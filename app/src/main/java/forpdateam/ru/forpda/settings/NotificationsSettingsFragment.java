package forpdateam.ru.forpda.settings;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.BuildConfig;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.client.ClientHelper;
import forpdateam.ru.forpda.rxapi.RxApi;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by radiationx on 12.07.17.
 */

public class NotificationsSettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences_notifications);
        ((SettingsActivity) getActivity()).getSupportActionBar().setTitle(getPreferenceScreen().getTitle());
    }
}
