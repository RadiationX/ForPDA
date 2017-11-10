package forpdateam.ru.forpda.ui.fragments.settings;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.Preference;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.BuildConfig;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.apirx.RxApi;
import forpdateam.ru.forpda.client.ClientHelper;
import forpdateam.ru.forpda.common.IntentHandler;
import forpdateam.ru.forpda.common.Preferences;
import forpdateam.ru.forpda.ui.activities.SettingsActivity;
import forpdateam.ru.forpda.ui.activities.updatechecker.UpdateCheckerActivity;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by radiationx on 25.12.16.
 */

public class SettingsFragment extends BaseSettingFragment {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        if (ClientHelper.getAuthState()) {
            findPreference("auth.action.logout")
                    .setOnPreferenceClickListener(preference -> {
                        new AlertDialog.Builder(getActivity())
                                .setMessage(R.string.ask_logout)
                                .setPositiveButton(R.string.ok, (dialog, which) -> RxApi.Auth().logout().onErrorReturn(throwable -> false)
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(aBoolean -> {
                                            if (aBoolean) {
                                                Toast.makeText(App.getContext(), "Logout complete", Toast.LENGTH_LONG).show();
                                                ClientHelper.get().notifyAuthChanged(ClientHelper.AUTH_STATE_LOGOUT);
                                            } else {
                                                Toast.makeText(App.getContext(), "Logout error", Toast.LENGTH_LONG).show();
                                            }
                                        }))
                                .setNegativeButton(R.string.no, null)
                                .show();
                        return false;
                    });
        } else {
            findPreference("auth.action.logout")
                    .setEnabled(false);
        }

        findPreference("about.application")
                .setSummary(String.format(getString(R.string.version_Build), BuildConfig.VERSION_NAME));

        {
            Preference fPref = findPreference("about.app_faq");
            fPref.setIcon(App.getAppVecDrawable(R.drawable.contact_site));
            fPref.setOnPreferenceClickListener(preference -> {
                IntentHandler.externalIntent("http://4pda.ru/forum/index.php?s=&showtopic=820313&view=findpost&p=64077514");
                return false;
            });
        }

        {
            Preference fPref = findPreference("about.app_topic");
            fPref.setIcon(App.getAppVecDrawable(R.drawable.contact_site));
            fPref.setOnPreferenceClickListener(preference -> {
                IntentHandler.externalIntent("https://4pda.ru/forum/index.php?showtopic=820313");
                return false;
            });
        }

        findPreference("about.check_update")
                .setOnPreferenceClickListener(preference -> {
                    startActivity(new Intent(getActivity(), UpdateCheckerActivity.class));
                    return false;
                });

        findPreference(Preferences.Main.WEBVIEW_FONT_SIZE)
                .setOnPreferenceClickListener(preference -> {
                    View v = getActivity().getLayoutInflater().inflate(R.layout.dialog_font_size, null);

                    assert v != null;
                    final SeekBar seekBar = (SeekBar) v.findViewById(R.id.value_seekbar);
                    seekBar.setProgress(Preferences.Main.getWebViewSize(getContext()) - 1 - 7);
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
                            .setTitle(R.string.text_size)
                            .setView(v)
                            .setPositiveButton(R.string.ok, (dialog1, which) -> Preferences.Main.setWebViewSize(getContext(), seekBar.getProgress() + 1 + 7))
                            .setNegativeButton(R.string.cancel, null)
                            .setNeutralButton(R.string.reset, null)
                            .show();
                    dialog.getButton(DialogInterface.BUTTON_NEUTRAL).setOnClickListener(v1 -> {
                        seekBar.setProgress(16 - 1 - 7);
                        Preferences.Main.setWebViewSize(getContext(), 16);
                        App.get().getPreferences().edit().putInt(Preferences.Main.WEBVIEW_FONT_SIZE, 16).apply();
                    });
                    return false;
                });

        findPreference("open_notifications")
                .setOnPreferenceClickListener(preference -> {
                    Intent intent = new Intent(getActivity(), SettingsActivity.class);
                    intent.putExtra(SettingsActivity.ARG_NEW_PREFERENCE_SCREEN, NotificationsSettingsFragment.PREFERENCE_SCREEN_NAME);
                    startActivity(intent);
                    return true;
                });
    }
}
