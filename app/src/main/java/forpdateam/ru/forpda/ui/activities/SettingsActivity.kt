package forpdateam.ru.forpda.ui.activities

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat
import android.view.MenuItem
import forpdateam.ru.forpda.App
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.common.LocaleHelper
import forpdateam.ru.forpda.ui.fragments.settings.NotificationsSettingsFragment
import forpdateam.ru.forpda.ui.fragments.settings.SettingsFragment
import io.reactivex.disposables.CompositeDisposable

/**
 * Created by radiationx on 25.12.16.
 */

class SettingsActivity : AppCompatActivity() {

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LocaleHelper.onAttach(base))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.DayNightPreferenceTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        supportActionBar?.apply {
            setHomeButtonEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(true)
            setTitle(R.string.activity_title_settings)
            elevation = 0f
        }


        val fragment: PreferenceFragmentCompat = if (intent?.getStringExtra(ARG_NEW_PREFERENCE_SCREEN) == NotificationsSettingsFragment.PREFERENCE_SCREEN_NAME) {
            NotificationsSettingsFragment()
        } else {
            SettingsFragment()
        }

        supportFragmentManager.beginTransaction().replace(R.id.fragment_content, fragment).commit()
    }


    override fun onResume() {
        super.onResume()
        updateStatusBar()
    }

    private fun updateStatusBar() {
        val defaultSb = MainActivity.getDefaultLightStatusBar(this)
        MainActivity.setLightStatusBar(this, defaultSb)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home)
            finish()
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        App.get().onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    companion object {
        const val ARG_NEW_PREFERENCE_SCREEN = "new_preference_screen"
    }
}
