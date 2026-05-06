package forpdateam.ru.forpda.common

import android.content.res.Configuration
import android.os.Build
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

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

    private val isNightRelay = MutableStateFlow(defaultMode)

    fun observeIsNight(): StateFlow<Boolean> = isNightRelay.asStateFlow()

    fun isNight(): Boolean = isNightRelay.value

    fun setIsNight(isNight: Boolean) {
        isNightRelay.value = isNight
    }

}