package forpdateam.ru.forpda.model.preferences

import android.content.SharedPreferences
import com.f2prateek.rx.preferences2.RxSharedPreferences
import forpdateam.ru.forpda.common.Preferences
import io.reactivex.Observable

class OtherPreferencesHolder(
        private val sharedPreferences: SharedPreferences
) {

    private val rxPreferences = RxSharedPreferences.create(sharedPreferences)

    private val appFirstStart by lazy {
        rxPreferences.getBoolean(Preferences.Other.APP_FIRST_START, true)
    }

    private val appVersionsHistory by lazy {
        rxPreferences.getString(Preferences.Other.APP_VERSIONS_HISTORY)
    }

    private val searchSettings by lazy {
        rxPreferences.getString(Preferences.Other.SEARCH_SETTINGS)
    }

    private val messagePanelBbCodes by lazy {
        rxPreferences.getString(Preferences.Other.MESSAGE_PANEL_BBCODES_SORT)
    }

    private val showReportWarning by lazy {
        rxPreferences.getBoolean(Preferences.Other.SHOW_REPORT_WARNING, true)
    }

    private val tooltipSearchSettings by lazy {
        rxPreferences.getBoolean(Preferences.Other.TOOLTIP_SEARCH_SETTINGS, true)
    }

    private val tooltipMessagePanelSorting by lazy {
        rxPreferences.getBoolean(Preferences.Other.TOOLTIP_MESSAGE_PANEL_SORTING, true)
    }

    fun observeAppFirstStart(): Observable<Boolean> = appFirstStart.asObservable()

    fun observeAppVersionsHistory(): Observable<String> = appVersionsHistory.asObservable()

    fun observeSearchSettings(): Observable<String> = searchSettings.asObservable()

    fun observeMessagePanelBbCodes(): Observable<String> = messagePanelBbCodes.asObservable()

    fun observeShowReportWarning(): Observable<Boolean> = showReportWarning.asObservable()

    fun observeTooltipSearchSettings(): Observable<Boolean> = tooltipSearchSettings.asObservable()

    fun observeTooltipMessagePanelSorting(): Observable<Boolean> = tooltipMessagePanelSorting.asObservable()


    fun setAppFirstStart(value: Boolean) = appFirstStart.set(value)

    fun setAppVersionsHistory(value: String) = appVersionsHistory.set(value)

    fun setSearchSettings(value: String) = searchSettings.set(value)

    fun setMessagePanelBbCodes(value: String) = messagePanelBbCodes.set(value)

    fun setShowReportWarning(value: Boolean) = showReportWarning.set(value)

    fun setTooltipSearchSettings(value: Boolean) = tooltipSearchSettings.set(value)

    fun setTooltipMessagePanelSorting(value: Boolean) = tooltipMessagePanelSorting.set(value)


    fun deleteMessagePanelBbCodes() = messagePanelBbCodes.delete()


    fun getAppFirstStart(): Boolean = appFirstStart.get()

    fun getAppVersionsHistory(): String = appVersionsHistory.get()

    fun getSearchSettings(): String = searchSettings.get()

    fun getMessagePanelBbCodes(): String = messagePanelBbCodes.get()

    fun getShowReportWarning(): Boolean = showReportWarning.get()

    fun getTooltipSearchSettings(): Boolean = tooltipSearchSettings.get()

    fun getTooltipMessagePanelSorting(): Boolean = tooltipMessagePanelSorting.get()


}