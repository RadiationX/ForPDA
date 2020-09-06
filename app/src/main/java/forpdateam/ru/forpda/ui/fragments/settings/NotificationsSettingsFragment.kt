package forpdateam.ru.forpda.ui.fragments.settings

import android.os.Bundle
import android.support.v7.app.ActionBar

import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.ui.activities.SettingsActivity

/**
 * Created by radiationx on 12.07.17.
 */

class NotificationsSettingsFragment : BaseSettingFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.preferences_notifications)
        (activity as? SettingsActivity)?.supportActionBar?.title = preferenceScreen.title
    }

    companion object {
        const val PREFERENCE_SCREEN_NAME = "notifications"
    }
}
