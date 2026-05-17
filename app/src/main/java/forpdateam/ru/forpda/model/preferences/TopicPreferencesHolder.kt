package forpdateam.ru.forpda.model.preferences

import forpdateam.ru.forpda.common.Preferences
import ru.radiationx.flowpreferences.FlowPreferences
import javax.inject.Inject

class TopicPreferencesHolder @Inject constructor(
    private val preferences: FlowPreferences
) {

    val showAvatars by lazy {
        preferences.getBoolean(Preferences.Theme.SHOW_AVATARS, true)
    }

    val circleAvatars by lazy {
        preferences.getBoolean(Preferences.Theme.CIRCLE_AVATARS, true)
    }

    val anchorHistory by lazy {
        preferences.getBoolean(Preferences.Theme.ANCHOR_HISTORY, true)
    }

    val hatOpened by lazy {
        preferences.getBoolean(Preferences.Theme.HAT_OPENED, false)
    }
}