package forpdateam.ru.forpda.model.preferences

import forpdateam.ru.forpda.common.Preferences
import forpdateam.ru.forpda.common.flowpreferences.FlowPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.rx2.asFlow

class TopicPreferencesHolder(
    private val preferences: FlowPreferences
) {

    private val showAvatars by lazy {
        preferences.getBoolean(Preferences.Theme.SHOW_AVATARS, true)
    }

    private val circleAvatars by lazy {
        preferences.getBoolean(Preferences.Theme.CIRCLE_AVATARS, true)
    }

    private val anchorHistory by lazy {
        preferences.getBoolean(Preferences.Theme.ANCHOR_HISTORY, true)
    }

    private val hatOpened by lazy {
        preferences.getBoolean(Preferences.Theme.HAT_OPENED, false)
    }

    fun observeShowAvatars(): Flow<Boolean> = showAvatars

    fun observeCircleAvatars(): Flow<Boolean> = circleAvatars

    fun observeAnchorHistory(): Flow<Boolean> = anchorHistory

    fun observeHatOpened(): Flow<Boolean> = hatOpened

    fun getShowAvatars(): Boolean = showAvatars.get()

    fun getCircleAvatars(): Boolean = circleAvatars.get()

    fun getAnchorHistory(): Boolean = anchorHistory.get()

    fun getHatOpened(): Boolean = hatOpened.get()

}