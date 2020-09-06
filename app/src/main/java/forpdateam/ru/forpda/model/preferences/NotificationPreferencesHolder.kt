package forpdateam.ru.forpda.model.preferences

import android.content.SharedPreferences
import android.support.v4.util.ArraySet
import com.f2prateek.rx.preferences2.RxSharedPreferences
import forpdateam.ru.forpda.common.Preferences
import io.reactivex.Observable

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


    fun observeMainEnabled(): Observable<Boolean> = mainEnabled.asObservable()

    fun observeMainSoundEnabled(): Observable<Boolean> = mainSoundEnabled.asObservable()

    fun observeMainVibrationEnabled(): Observable<Boolean> = mainVibrationEnabled.asObservable()

    fun observeMainIndicatorEnabled(): Observable<Boolean> = mainIndicatorEnabled.asObservable()

    fun observeMainAvatarsEnabled(): Observable<Boolean> = mainAvatarsEnabled.asObservable()

    fun observeMainLimit(): Observable<Long> = mainLimit.asObservable()
            .map { it.toLong() * 1000 }

    fun observeFavEnabled(): Observable<Boolean> = favEnabled.asObservable()

    fun observeFavOnlyImportant(): Observable<Boolean> = favOnlyImportant.asObservable()

    fun observeFavLiveTab(): Observable<Boolean> = favLiveTab.asObservable()

    fun observeQmsEnabled(): Observable<Boolean> = qmsEnabled.asObservable()

    fun observeMentionsEnabled(): Observable<Boolean> = mentionsEnabled.asObservable()

    fun observeUpdateEnabled(): Observable<Boolean> = updateEnabled.asObservable()

    fun observeDataQmsEvents(): Observable<Set<String>> = dataQmsEvents.asObservable()

    fun observeDataFavoritesEvents(): Observable<Set<String>> = dataFavoritesEvents.asObservable()


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