package forpdateam.ru.forpda.model.preferences

import android.content.SharedPreferences
import com.f2prateek.rx.preferences2.RxSharedPreferences
import forpdateam.ru.forpda.common.Preferences
import io.reactivex.Observable

class TopicPreferencesHolder(
        private val sharedPreferences: SharedPreferences
) {

    private val rxPreferences = RxSharedPreferences.create(sharedPreferences)

    private val showAvatars by lazy {
        rxPreferences.getBoolean(Preferences.Theme.SHOW_AVATARS, true)
    }

    private val circleAvatars by lazy {
        rxPreferences.getBoolean(Preferences.Theme.CIRCLE_AVATARS, true)
    }

    private val anchorHistory by lazy {
        rxPreferences.getBoolean(Preferences.Theme.ANCHOR_HISTORY, true)
    }

    private val hatOpened by lazy {
        rxPreferences.getBoolean(Preferences.Theme.HAT_OPENED, false)
    }

    fun observeShowAvatars(): Observable<Boolean> = showAvatars.asObservable()

    fun observeCircleAvatars(): Observable<Boolean> = circleAvatars.asObservable()

    fun observeAnchorHistory(): Observable<Boolean> = anchorHistory.asObservable()

    fun observeHatOpened(): Observable<Boolean> = hatOpened.asObservable()

    fun getShowAvatars(): Boolean = showAvatars.get()

    fun getCircleAvatars(): Boolean = circleAvatars.get()

    fun getAnchorHistory(): Boolean = anchorHistory.get()

    fun getHatOpened(): Boolean = hatOpened.get()

}