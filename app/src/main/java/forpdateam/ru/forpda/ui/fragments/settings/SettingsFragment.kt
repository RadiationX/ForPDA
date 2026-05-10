package forpdateam.ru.forpda.ui.fragments.settings

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.preference.Preference
import forpdateam.ru.forpda.App
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.common.AppBuildConfig
import forpdateam.ru.forpda.common.Preferences
import forpdateam.ru.forpda.extensions.coRunCatching
import forpdateam.ru.forpda.ui.activities.SettingsActivity
import forpdateam.ru.forpda.ui.activities.updatechecker.UpdateCheckerActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Created by radiationx on 25.12.16.
 */

class SettingsFragment : BaseSettingFragment() {
    private val authRepository = App.get().Di().authRepository
    private val authHolder = App.get().Di().authHolder
    private val mainPreferencesHolder = App.get().Di().mainPreferencesHolder
    private var logoutJob: Job? = null

    @SuppressLint("InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.preferences)
        if (authHolder.get().isAuth()) {
            findPreference<Preference>("auth.action.logout")?.apply {
                setOnPreferenceClickListener {
                    AlertDialog.Builder(requireActivity())
                        .setMessage(R.string.ask_logout)
                        .setPositiveButton(R.string.ok) { _, _ ->
                            logoutRequest()
                        }
                        .setNegativeButton(R.string.no, null)
                        .show()
                    false
                }
            }
        } else {
            findPreference<Preference>("auth.action.logout")?.apply {
                isEnabled = false
            }
        }

        findPreference<Preference>("clear_menu_sequence")?.apply {
            setOnPreferenceClickListener {
                AlertDialog.Builder(requireActivity())
                    .setMessage("Подтвердите действие")
                    .setPositiveButton(R.string.ok) { _, _ ->
                        App.get().Di().preferences.edit().remove("menu_items_sequence").apply()
                    }
                    .setNegativeButton(R.string.cancel, null)
                    .show()
                false
            }
        }

        findPreference<Preference>("about.application")?.apply {
            summary = String.format(getString(R.string.version_Build), AppBuildConfig.versionName)
        }

        findPreference<Preference>("about.check_update")?.apply {
            setOnPreferenceClickListener {
                startActivity(Intent(activity, UpdateCheckerActivity::class.java))
                false
            }
        }

        findPreference<Preference>(Preferences.Main.WEBVIEW_FONT_SIZE)?.apply {
            setOnPreferenceClickListener { _ ->

                val dialogView =
                    requireActivity().layoutInflater.inflate(R.layout.dialog_font_size, null)!!
                val seekBar = dialogView.findViewById<View>(R.id.value_seekbar) as SeekBar
                val textView = dialogView.findViewById<View>(R.id.value_textview) as TextView

                seekBar.progress = mainPreferencesHolder.webViewFontSize.get() - 1 - 7

                textView.text = (seekBar.progress + 1 + 7).toString()
                textView.textSize = (seekBar.progress + 1 + 7).toFloat()

                seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                        textView.text = (i + 1 + 7).toString()
                        textView.textSize = (i + 1 + 7).toFloat()
                    }

                    override fun onStartTrackingTouch(seekBar: SeekBar) {}
                    override fun onStopTrackingTouch(seekBar: SeekBar) {}
                })
                AlertDialog.Builder(requireActivity())
                    .setTitle(R.string.text_size)
                    .setView(dialogView)
                    .setPositiveButton(R.string.ok) { _, _ ->
                        mainPreferencesHolder.webViewFontSize.set(seekBar.progress + 1 + 7)
                    }
                    .setNegativeButton(R.string.cancel, null)
                    .setNeutralButton(R.string.reset, null)
                    .show()
                    .getButton(DialogInterface.BUTTON_NEUTRAL)
                    .setOnClickListener {
                        seekBar.progress = 16 - 1 - 7
                        mainPreferencesHolder.webViewFontSize.set(16)
                    }

                false
            }
        }

        findPreference<Preference>("open_notifications")?.apply {
            setOnPreferenceClickListener {
                val intent = Intent(activity, SettingsActivity::class.java)
                intent.putExtra(
                    SettingsActivity.ARG_NEW_PREFERENCE_SCREEN,
                    NotificationsSettingsFragment.PREFERENCE_SCREEN_NAME
                )
                startActivity(intent)
                true
            }
        }
    }

    private fun logoutRequest() {
        logoutJob?.cancel()
        logoutJob = GlobalScope.launch(Dispatchers.Main) {
            coRunCatching {
                authRepository.signOut()
            }.onSuccess {
                Toast.makeText(App.getContext(), "Logout complete", Toast.LENGTH_LONG).show()
            }.onFailure {
                Toast.makeText(App.getContext(), "Logout error: $it", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        logoutJob?.cancel()
    }
}
