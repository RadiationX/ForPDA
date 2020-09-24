package forpdateam.ru.forpda.common

import android.content.res.Configuration
import android.os.Build
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Observable

class DayNightHelper(
        private val defaultMode: Boolean
) {

    companion object {

        fun isUiModeNight(configuration: Configuration): Boolean {
            val currentNightMode = configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            return currentNightMode == Configuration.UI_MODE_NIGHT_YES
        }


        fun applyTheme(prefMode: String) {
            val mode = Preferences.Main.ThemeMode.valueOf(prefMode)
            applyTheme(mode)
        }

        fun applyTheme(mode: Preferences.Main.ThemeMode) {
            Log.d("kekeke", "DayNightHelper applyTheme $mode")
            val delegateMode = when (mode) {
                Preferences.Main.ThemeMode.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
                Preferences.Main.ThemeMode.DARK -> AppCompatDelegate.MODE_NIGHT_YES
                Preferences.Main.ThemeMode.SYSTEM -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                    } else {
                        AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
                    }
                }
            }
            AppCompatDelegate.setDefaultNightMode(delegateMode)
        }
    }

    private val isNightRelay = BehaviorRelay.createDefault(defaultMode)

    fun observeIsNight(): Observable<Boolean> = isNightRelay.hide().distinctUntilChanged()

    fun isNight(): Boolean = isNightRelay.value ?: false

    fun setIsNight(isNight: Boolean) {
        isNightRelay.accept(isNight)
    }

}