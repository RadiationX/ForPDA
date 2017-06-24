package forpdateam.ru.forpda.settings;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.BuildConfig;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.client.ClientHelper;
import forpdateam.ru.forpda.rxapi.RxApi;
import forpdateam.ru.forpda.utils.ourparser.Html;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by radiationx on 25.12.16.
 */

public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        if(ClientHelper.getAuthState()){
            findPreference("auth.action.logout").setOnPreferenceClickListener(preference -> {
                new AlertDialog.Builder(getActivity())
                        .setMessage("Вы уверены, что хотите выйти из аккаунта?")
                        .setPositiveButton("Да", (dialog, which) -> RxApi.Auth().logout().onErrorReturn(throwable -> false)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(aBoolean -> {
                                    if (aBoolean) {
                                        Toast.makeText(App.getContext(), "Logout complete", Toast.LENGTH_LONG).show();
                                        ClientHelper.getInstance().notifyAuthChanged(ClientHelper.AUTH_STATE_LOGOUT);
                                    } else {
                                        Toast.makeText(App.getContext(), "Logout error", Toast.LENGTH_LONG).show();
                                    }
                                }))
                        .setNegativeButton("Нет", null)
                        .show();
                return false;
            });
        }else {
            findPreference("auth.action.logout").setEnabled(false);
        }

        findPreference("about.application").setSummary("Версия " + BuildConfig.VERSION_NAME);
    }
}
