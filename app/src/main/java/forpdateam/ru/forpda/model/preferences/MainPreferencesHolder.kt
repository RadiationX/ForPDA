package forpdateam.ru.forpda.model.preferences

import android.content.SharedPreferences
import com.f2prateek.rx.preferences2.RxSharedPreferences
import forpdateam.ru.forpda.common.Preferences
import io.reactivex.Observable
import kotlin.math.max
import kotlin.math.min

class MainPreferencesHolder(
        private val sharedPreferences: SharedPreferences
) {

    private val rxPreferences = RxSharedPreferences.create(sharedPreferences)

    private val webViewFontSize by lazy {
        rxPreferences.getInteger(Preferences.Main.WEBVIEW_FONT_SIZE, 16)
    }

    private val systemDownloader by lazy {
        rxPreferences.getBoolean(Preferences.Main.IS_SYSTEM_DOWNLOADER, true)
    }

    private val editorMonospace by lazy {
        rxPreferences.getBoolean(Preferences.Main.IS_EDITOR_MONOSPACE, true)
    }

    private val editorDefaultHidden by lazy {
        rxPreferences.getBoolean(Preferences.Main.IS_EDITOR_DEFAULT_HIDDEN, true)
    }

    private val scrollButtonEnabled by lazy {
        rxPreferences.getBoolean(Preferences.Main.SCROLL_BUTTON_ENABLE, false)
    }

    private val themeIsDark by lazy {
        rxPreferences.getBoolean(Preferences.Main.Theme.IS_DARK, false)
    }

    private val showBottomArrow by lazy {
        rxPreferences.getBoolean(Preferences.Main.SHOW_BOTTOM_ARROW, false)
    }

    fun observeWebViewFontSize(): Observable<Int> = webViewFontSize.asObservable()
            .map { max(min(it, 64), 8) }

    fun observeSystemDownloader(): Observable<Boolean> = systemDownloader.asObservable()

    fun observeEditorMonospace(): Observable<Boolean> = editorMonospace.asObservable()

    fun observeEditorDefaultHidden(): Observable<Boolean> = editorDefaultHidden.asObservable()

    fun observeScrollButtonEnabled(): Observable<Boolean> = scrollButtonEnabled.asObservable()

    fun observeThemeIsDark(): Observable<Boolean> = themeIsDark.asObservable()

    fun observeShowBottomArrow(): Observable<Boolean> = showBottomArrow.asObservable()


    fun getWebViewFontSize(): Int = max(min(webViewFontSize.get(), 64), 8)

    fun getSystemDownloader(): Boolean = systemDownloader.get()

    fun getEditorMonospace(): Boolean = editorMonospace.get()

    fun getEditorDefaultHidden(): Boolean = editorDefaultHidden.get()

    fun getScrollButtonEnabled(): Boolean = scrollButtonEnabled.get()

    fun getThemeIsDark(): Boolean = themeIsDark.get()

    fun getShowBottomArrow(): Boolean = showBottomArrow.get()


    fun setWebViewFontSize(size: Int): Unit = webViewFontSize.set(max(min(size, 64), 8))

}