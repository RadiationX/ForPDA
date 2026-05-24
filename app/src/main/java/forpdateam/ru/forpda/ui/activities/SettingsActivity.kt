package forpdateam.ru.forpda.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.ui.fragments.settings.NotificationsSettingsFragment
import forpdateam.ru.forpda.ui.fragments.settings.SettingsFragment

/**
 * Created by radiationx on 25.12.16.
 */

class SettingsActivity : AppCompatActivity(R.layout.activity_settings) {

    companion object {
        private const val ARG_NEW_PREFERENCE_SCREEN = "new_preference_screen"
        fun newIntent(context: Context, screenName: String? = null): Intent {
            return Intent(context, SettingsActivity::class.java).apply {
                putExtra(ARG_NEW_PREFERENCE_SCREEN, screenName)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.DayNightPreferenceTheme)
        super.onCreate(savedInstanceState)

        supportActionBar?.apply {
            setHomeButtonEnabled(true)
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(true)
            setTitle(R.string.activity_title_settings)
            elevation = 0f
        }


        val fragment: PreferenceFragmentCompat =
            if (intent?.getStringExtra(ARG_NEW_PREFERENCE_SCREEN) == NotificationsSettingsFragment.PREFERENCE_SCREEN_NAME) {
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
}
