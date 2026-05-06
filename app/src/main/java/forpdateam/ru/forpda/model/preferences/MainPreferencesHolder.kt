package forpdateam.ru.forpda.model.preferences

import forpdateam.ru.forpda.common.Preferences
import forpdateam.ru.forpda.common.flowpreferences.FlowPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.math.max
import kotlin.math.min

class MainPreferencesHolder(
    private val preferences: FlowPreferences
) {


    private val webViewFontSize by lazy {
        preferences.getInt(Preferences.Main.WEBVIEW_FONT_SIZE, 16)
    }

    private val systemDownloader by lazy {
        preferences.getBoolean(Preferences.Main.IS_SYSTEM_DOWNLOADER, true)
    }

    private val editorMonospace by lazy {
        preferences.getBoolean(Preferences.Main.IS_EDITOR_MONOSPACE, true)
    }

    private val editorDefaultHidden by lazy {
        preferences.getBoolean(Preferences.Main.IS_EDITOR_DEFAULT_HIDDEN, true)
    }

    private val scrollButtonEnabled by lazy {
        preferences.getBoolean(Preferences.Main.SCROLL_BUTTON_ENABLE, false)
    }

    private val themeMode by lazy {
        preferences.getEnum(
            Preferences.Main.Theme.MODE,
            Preferences.Main.ThemeMode.SYSTEM,
            Preferences.Main.ThemeMode::class.java
        )
    }

    private val showBottomArrow by lazy {
        preferences.getBoolean(Preferences.Main.SHOW_BOTTOM_ARROW, false)
    }

    fun observeWebViewFontSize(): Flow<Int> = webViewFontSize.map { max(min(it, 64), 8) }

    fun observeSystemDownloader(): Flow<Boolean> = systemDownloader

    fun observeEditorMonospace(): Flow<Boolean> = editorMonospace

    fun observeEditorDefaultHidden(): Flow<Boolean> = editorDefaultHidden

    fun observeScrollButtonEnabled(): Flow<Boolean> = scrollButtonEnabled

    fun observeThemeMode(): Flow<Preferences.Main.ThemeMode> = themeMode

    fun observeShowBottomArrow(): Flow<Boolean> = showBottomArrow


    fun getWebViewFontSize(): Int = max(min(webViewFontSize.get(), 64), 8)

    fun getSystemDownloader(): Boolean = systemDownloader.get()

    fun getEditorMonospace(): Boolean = editorMonospace.get()

    fun getEditorDefaultHidden(): Boolean = editorDefaultHidden.get()

    fun getScrollButtonEnabled(): Boolean = scrollButtonEnabled.get()

    fun getThemeMode(): Preferences.Main.ThemeMode = themeMode.get()

    fun getShowBottomArrow(): Boolean = showBottomArrow.get()


    fun setWebViewFontSize(size: Int): Unit = webViewFontSize.set(max(min(size, 64), 8))

    fun setThemeMode(mode: Preferences.Main.ThemeMode) = themeMode.set(mode)

}