package forpdateam.ru.forpda.model.preferences

import android.content.SharedPreferences
import com.f2prateek.rx.preferences2.RxSharedPreferences
import forpdateam.ru.forpda.common.Preferences
import io.reactivex.Observable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.rx2.asFlow

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

    fun observeShowAvatars(): Flow<Boolean> = showAvatars.asObservable().asFlow()

    fun observeCircleAvatars(): Flow<Boolean> = circleAvatars.asObservable().asFlow()

    fun observeAnchorHistory(): Flow<Boolean> = anchorHistory.asObservable().asFlow()

    fun observeHatOpened(): Flow<Boolean> = hatOpened.asObservable().asFlow()

    fun getShowAvatars(): Boolean = showAvatars.get()

    fun getCircleAvatars(): Boolean = circleAvatars.get()

    fun getAnchorHistory(): Boolean = anchorHistory.get()

    fun getHatOpened(): Boolean = hatOpened.get()

}