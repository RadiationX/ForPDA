package forpdateam.ru.forpda.model.preferences

import android.content.SharedPreferences
import com.f2prateek.rx.preferences2.RxSharedPreferences
import forpdateam.ru.forpda.common.Preferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.rx2.asFlow

class NotificationPreferencesHolder(
    private val sharedPreferences: SharedPreferences
) {

    private val rxPreferences = RxSharedPreferences.create(sharedPreferences)

    private val mainEnabled by lazy {
        rxPreferences.getBoolean(Preferences.Notifications.Main.ENABLED, true)
    }

    private val mainSoundEnabled by lazy {
        rxPreferences.getBoolean(Preferences.Notifications.Main.SOUND_ENABLED, true)
    }

    private val mainVibrationEnabled by lazy {
        rxPreferences.getBoolean(Preferences.Notifications.Main.VIBRATION_ENABLED, true)
    }

    private val mainIndicatorEnabled by lazy {
        rxPreferences.getBoolean(Preferences.Notifications.Main.INDICATOR_ENABLED, true)
    }

    private val mainAvatarsEnabled by lazy {
        rxPreferences.getBoolean(Preferences.Notifications.Main.AVATARS_ENABLED, true)
    }

    private val mainLimit by lazy {
        rxPreferences.getString(Preferences.Notifications.Main.LIMIT, "10")
    }

    private val favEnabled by lazy {
        rxPreferences.getBoolean(Preferences.Notifications.Favorites.ENABLED, true)
    }

    private val favOnlyImportant by lazy {
        rxPreferences.getBoolean(Preferences.Notifications.Favorites.ONLY_IMPORTANT, false)
    }

    private val favLiveTab by lazy {
        rxPreferences.getBoolean(Preferences.Notifications.Favorites.LIVE_TAB, true)
    }

    private val qmsEnabled by lazy {
        rxPreferences.getBoolean(Preferences.Notifications.Qms.ENABLED, true)
    }

    private val mentionsEnabled by lazy {
        rxPreferences.getBoolean(Preferences.Notifications.Mentions.ENABLED, true)
    }

    private val updateEnabled by lazy {
        rxPreferences.getBoolean(Preferences.Notifications.Update.ENABLED, true)
    }

    private val dataQmsEvents by lazy {
        rxPreferences.getStringSet(Preferences.Notifications.Data.QMS_EVENTS)
    }

    private val dataFavoritesEvents by lazy {
        rxPreferences.getStringSet(Preferences.Notifications.Data.FAVORITES_EVENTS)
    }


    fun observeMainEnabled(): Flow<Boolean> = mainEnabled.asObservable().asFlow()

    fun observeMainSoundEnabled(): Flow<Boolean> = mainSoundEnabled.asObservable().asFlow()

    fun observeMainVibrationEnabled(): Flow<Boolean> = mainVibrationEnabled.asObservable().asFlow()

    fun observeMainIndicatorEnabled(): Flow<Boolean> = mainIndicatorEnabled.asObservable().asFlow()

    fun observeMainAvatarsEnabled(): Flow<Boolean> = mainAvatarsEnabled.asObservable().asFlow()

    fun observeMainLimit(): Flow<Long> = mainLimit.asObservable()
        .map { it.toLong() * 1000 }.asFlow()

    fun observeFavEnabled(): Flow<Boolean> = favEnabled.asObservable().asFlow()

    fun observeFavOnlyImportant(): Flow<Boolean> = favOnlyImportant.asObservable().asFlow()

    fun observeFavLiveTab(): Flow<Boolean> = favLiveTab.asObservable().asFlow()

    fun observeQmsEnabled(): Flow<Boolean> = qmsEnabled.asObservable().asFlow()

    fun observeMentionsEnabled(): Flow<Boolean> = mentionsEnabled.asObservable().asFlow()

    fun observeUpdateEnabled(): Flow<Boolean> = updateEnabled.asObservable().asFlow()

    fun observeDataQmsEvents(): Flow<Set<String>> = dataQmsEvents.asObservable().asFlow()

    fun observeDataFavoritesEvents(): Flow<Set<String>> =
        dataFavoritesEvents.asObservable().asFlow()


    fun setDataQmsEvents(value: Set<String>) = dataQmsEvents.set(value)

    fun setDataFavoritesEvents(value: Set<String>) = dataFavoritesEvents.set(value)


    fun getMainEnabled(): Boolean = mainEnabled.get()

    fun getMainSoundEnabled(): Boolean = mainSoundEnabled.get()

    fun getMainVibrationEnabled(): Boolean = mainVibrationEnabled.get()

    fun getMainIndicatorEnabled(): Boolean = mainIndicatorEnabled.get()

    fun getMainAvatarsEnabled(): Boolean = mainAvatarsEnabled.get()

    fun getMainLimit(): Long = mainLimit.get().toLong() * 1000

    fun getFavEnabled(): Boolean = favEnabled.get()

    fun getFavOnlyImportant(): Boolean = favOnlyImportant.get()

    fun getFavLiveTab(): Boolean = favLiveTab.get()

    fun getQmsEnabled(): Boolean = qmsEnabled.get()

    fun getMentionsEnabled(): Boolean = mentionsEnabled.get()

    fun getUpdateEnabled(): Boolean = updateEnabled.get()

    fun getDataQmsEvents(): Set<String> = dataQmsEvents.get()

    fun getDataFavoritesEvents(): Set<String> = dataFavoritesEvents.get()

}