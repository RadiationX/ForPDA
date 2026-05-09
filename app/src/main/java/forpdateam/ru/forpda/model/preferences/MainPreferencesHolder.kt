package forpdateam.ru.forpda.model.preferences

import forpdateam.ru.forpda.common.Preferences
import forpdateam.ru.forpda.common.flowpreferences.FlowPreferences
import forpdateam.ru.forpda.common.flowpreferences.mapping
import kotlin.math.max
import kotlin.math.min

class MainPreferencesHolder(
    val preferences: FlowPreferences
) {


    val webViewFontSize by lazy {
        preferences.getInt(Preferences.Main.WEBVIEW_FONT_SIZE, 16).mapping(
            transformGet = { max(min(it, 64), 8) },
            transformSet = { max(min(it, 64), 8) }
        )
    }

    val systemDownloader by lazy {
        preferences.getBoolean(Preferences.Main.IS_SYSTEM_DOWNLOADER, true)
    }

    val editorMonospace by lazy {
        preferences.getBoolean(Preferences.Main.IS_EDITOR_MONOSPACE, true)
    }

    val editorDefaultHidden by lazy {
        preferences.getBoolean(Preferences.Main.IS_EDITOR_DEFAULT_HIDDEN, true)
    }

    val scrollButtonEnabled by lazy {
        preferences.getBoolean(Preferences.Main.SCROLL_BUTTON_ENABLE, false)
    }

    val themeMode by lazy {
        preferences.getEnum(
            Preferences.Main.Theme.MODE,
            Preferences.Main.ThemeMode.SYSTEM,
            Preferences.Main.ThemeMode::class.java
        )
    }

    val showBottomArrow by lazy {
        preferences.getBoolean(Preferences.Main.SHOW_BOTTOM_ARROW, false)
    }
}