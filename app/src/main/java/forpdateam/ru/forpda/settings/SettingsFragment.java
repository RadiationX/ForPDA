package forpdateam.ru.forpda.settings;

import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
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
 * Created by radiationx on 25.12.16.
 */

public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        if (ClientHelper.getAuthState()) {
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
        } else {
            findPreference("auth.action.logout").setEnabled(false);
        }

        findPreference("about.application").setSummary("Версия " + BuildConfig.VERSION_NAME);

        findPreference(Preferences.Main.WEBVIEW_FONT_SIZE).setOnPreferenceClickListener(preference -> {
            View v = getActivity().getLayoutInflater().inflate(R.layout.dialog_font_size, null);

            assert v != null;
            final SeekBar seekBar = (SeekBar) v.findViewById(R.id.value_seekbar);
            seekBar.setProgress(Preferences.Main.getWebViewSize() - 1 - 7);
            final TextView textView = (TextView) v.findViewById(R.id.value_textview);
            textView.setText(Integer.toString(seekBar.getProgress() + 1 + 7));
            textView.setTextSize(seekBar.getProgress() + 1 + 7);
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    textView.setText(Integer.toString(i + 1 + 7));
                    textView.setTextSize(i + 1 + 7);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
            AlertDialog dialog = new AlertDialog.Builder(getActivity())
                    .setTitle("Размер шрифта")
                    .setView(v)
                    .setPositiveButton("Ок", (dialog1, which) -> Preferences.Main.setWebViewSize(seekBar.getProgress() + 1 + 7))
                    .setNegativeButton("Отмена", null)
                    .setNeutralButton("Сброс", null)
                    .show();
            dialog.getButton(DialogInterface.BUTTON_NEUTRAL).setOnClickListener(v1 -> {
                seekBar.setProgress(16 - 1 - 7);
                Preferences.Main.setWebViewSize(16);
                App.getInstance().getPreferences().edit().putInt(Preferences.Main.WEBVIEW_FONT_SIZE, 16).apply();
            });
            return false;
        });
    }
}
