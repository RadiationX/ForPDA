package forpdateam.ru.forpda.model.preferences

import forpdateam.ru.forpda.common.Preferences
import forpdateam.ru.forpda.common.flowpreferences.FlowPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class NotificationPreferencesHolder(
    private val preferences: FlowPreferences
) {


    private val mainEnabled by lazy {
        preferences.getBoolean(Preferences.Notifications.Main.ENABLED, true)
    }

    private val mainSoundEnabled by lazy {
        preferences.getBoolean(Preferences.Notifications.Main.SOUND_ENABLED, true)
    }

    private val mainVibrationEnabled by lazy {
        preferences.getBoolean(Preferences.Notifications.Main.VIBRATION_ENABLED, true)
    }

    private val mainIndicatorEnabled by lazy {
        preferences.getBoolean(Preferences.Notifications.Main.INDICATOR_ENABLED, true)
    }

    private val mainAvatarsEnabled by lazy {
        preferences.getBoolean(Preferences.Notifications.Main.AVATARS_ENABLED, true)
    }

    private val mainLimit by lazy {
        preferences.getString(Preferences.Notifications.Main.LIMIT, "10")
    }

    private val favEnabled by lazy {
        preferences.getBoolean(Preferences.Notifications.Favorites.ENABLED, true)
    }

    private val favOnlyImportant by lazy {
        preferences.getBoolean(Preferences.Notifications.Favorites.ONLY_IMPORTANT, false)
    }

    private val favLiveTab by lazy {
        preferences.getBoolean(Preferences.Notifications.Favorites.LIVE_TAB, true)
    }

    private val qmsEnabled by lazy {
        preferences.getBoolean(Preferences.Notifications.Qms.ENABLED, true)
    }

    private val mentionsEnabled by lazy {
        preferences.getBoolean(Preferences.Notifications.Mentions.ENABLED, true)
    }

    private val updateEnabled by lazy {
        preferences.getBoolean(Preferences.Notifications.Update.ENABLED, true)
    }

    private val dataQmsEvents by lazy {
        preferences.getStringSet(Preferences.Notifications.Data.QMS_EVENTS, emptySet())
    }

    private val dataFavoritesEvents by lazy {
        preferences.getStringSet(Preferences.Notifications.Data.FAVORITES_EVENTS, emptySet())
    }


    fun observeMainEnabled(): Flow<Boolean> = mainEnabled

    fun observeMainSoundEnabled(): Flow<Boolean> = mainSoundEnabled

    fun observeMainVibrationEnabled(): Flow<Boolean> = mainVibrationEnabled

    fun observeMainIndicatorEnabled(): Flow<Boolean> = mainIndicatorEnabled

    fun observeMainAvatarsEnabled(): Flow<Boolean> = mainAvatarsEnabled

    fun observeMainLimit(): Flow<Long> = mainLimit.map { (it?.toLong() ?: 10) * 1000 }

    fun observeFavEnabled(): Flow<Boolean> = favEnabled

    fun observeFavOnlyImportant(): Flow<Boolean> = favOnlyImportant

    fun observeFavLiveTab(): Flow<Boolean> = favLiveTab

    fun observeQmsEnabled(): Flow<Boolean> = qmsEnabled

    fun observeMentionsEnabled(): Flow<Boolean> = mentionsEnabled

    fun observeUpdateEnabled(): Flow<Boolean> = updateEnabled

    fun observeDataQmsEvents(): Flow<Set<String>?> = dataQmsEvents

    fun observeDataFavoritesEvents(): Flow<Set<String>?> = dataFavoritesEvents


    fun setDataQmsEvents(value: Set<String>) = dataQmsEvents.set(value)

    fun setDataFavoritesEvents(value: Set<String>) = dataFavoritesEvents.set(value)


    fun getMainEnabled(): Boolean = mainEnabled.get()

    fun getMainSoundEnabled(): Boolean = mainSoundEnabled.get()

    fun getMainVibrationEnabled(): Boolean = mainVibrationEnabled.get()

    fun getMainIndicatorEnabled(): Boolean = mainIndicatorEnabled.get()

    fun getMainAvatarsEnabled(): Boolean = mainAvatarsEnabled.get()

    fun getMainLimit(): Long = (mainLimit.get()?.toLong() ?: 10) * 1000

    fun getFavEnabled(): Boolean = favEnabled.get()

    fun getFavOnlyImportant(): Boolean = favOnlyImportant.get()

    fun getFavLiveTab(): Boolean = favLiveTab.get()

    fun getQmsEnabled(): Boolean = qmsEnabled.get()

    fun getMentionsEnabled(): Boolean = mentionsEnabled.get()

    fun getUpdateEnabled(): Boolean = updateEnabled.get()

    fun getDataQmsEvents(): Set<String>? = dataQmsEvents.get()

    fun getDataFavoritesEvents(): Set<String>? = dataFavoritesEvents.get()

}