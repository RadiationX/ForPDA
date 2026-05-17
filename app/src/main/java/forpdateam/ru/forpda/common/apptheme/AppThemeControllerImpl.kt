package forpdateam.ru.forpda.common.apptheme

import android.app.Application
import android.content.res.Configuration
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import forpdateam.ru.forpda.common.Preferences
import ru.radiationx.flowpreferences.FlowPreferences
import ru.radiationx.flowpreferences.ext.mapping
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.plus
import javax.inject.Inject

class AppThemeControllerImpl @Inject constructor(
    private val application: Application,
    private val preferences: FlowPreferences
) : AppThemeController {

    companion object {
        private const val APP_THEME_KEY = Preferences.Main.Theme.MODE
    }

    private val modePreference by lazy {
        preferences.getString(APP_THEME_KEY).mapping(
            transformGet = { value ->
                AppThemeMode.entries.find { it.value == value } ?: AppThemeMode.SYSTEM
            },
            transformSet = {
                it.value
            }
        )
    }

    override fun init() {
        observeMode()
            .onEach { applyMode(it) }
            .launchIn(GlobalScope + Dispatchers.Main.immediate)
    }

    override fun observeTheme(): Flow<AppTheme> = modePreference
        .map { it.toAppTheme() }
        .distinctUntilChanged()

    override fun getTheme(): AppTheme {
        return getMode().toAppTheme()
    }

    override fun observeMode(): Flow<AppThemeMode> {
        return modePreference
    }

    override fun getMode(): AppThemeMode {
        return modePreference.get()
    }

    override fun setMode(mode: AppThemeMode) {
        modePreference.set(mode)
    }

    private fun AppThemeMode.toAppTheme(): AppTheme {
        return when (this) {
            AppThemeMode.LIGHT -> AppTheme.LIGHT
            AppThemeMode.DARK -> AppTheme.DARK
            AppThemeMode.SYSTEM -> getAppThemeFromContext()
        }
    }

    private fun getAppThemeFromContext(): AppTheme {
        val currentNightMode = application.resources.configuration.uiMode.let {
            it and Configuration.UI_MODE_NIGHT_MASK
        }
        return if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
            AppTheme.DARK
        } else {
            AppTheme.LIGHT
        }
    }

    private fun applyMode(mode: AppThemeMode) {
        val delegateMode = when (mode) {
            AppThemeMode.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            AppThemeMode.DARK -> AppCompatDelegate.MODE_NIGHT_YES
            AppThemeMode.SYSTEM -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            } else {
                AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
            }
        }
        AppCompatDelegate.setDefaultNightMode(delegateMode)
    }

}