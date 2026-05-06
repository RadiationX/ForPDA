package forpdateam.ru.forpda.model.preferences

import forpdateam.ru.forpda.common.Preferences
import forpdateam.ru.forpda.common.flowpreferences.FlowPreferences
import kotlinx.coroutines.flow.Flow

class OtherPreferencesHolder(
    private val preferences: FlowPreferences
) {


    private val appFirstStart by lazy {
        preferences.getBoolean(Preferences.Other.APP_FIRST_START, true)
    }

    private val appVersionsHistory by lazy {
        preferences.getString(Preferences.Other.APP_VERSIONS_HISTORY)
    }

    private val searchSettings by lazy {
        preferences.getString(Preferences.Other.SEARCH_SETTINGS)
    }

    private val messagePanelBbCodes by lazy {
        preferences.getString(Preferences.Other.MESSAGE_PANEL_BBCODES_SORT)
    }

    private val showReportWarning by lazy {
        preferences.getBoolean(Preferences.Other.SHOW_REPORT_WARNING, true)
    }

    private val tooltipSearchSettings by lazy {
        preferences.getBoolean(Preferences.Other.TOOLTIP_SEARCH_SETTINGS, true)
    }

    private val tooltipMessagePanelSorting by lazy {
        preferences.getBoolean(Preferences.Other.TOOLTIP_MESSAGE_PANEL_SORTING, true)
    }

    fun observeAppFirstStart(): Flow<Boolean> = appFirstStart

    fun observeAppVersionsHistory(): Flow<String?> = appVersionsHistory

    fun observeSearchSettings(): Flow<String?> = searchSettings

    fun observeMessagePanelBbCodes(): Flow<String?> = messagePanelBbCodes

    fun observeShowReportWarning(): Flow<Boolean> = showReportWarning

    fun observeTooltipSearchSettings(): Flow<Boolean> = tooltipSearchSettings

    fun observeTooltipMessagePanelSorting(): Flow<Boolean> = tooltipMessagePanelSorting


    fun setAppFirstStart(value: Boolean) = appFirstStart.set(value)

    fun setAppVersionsHistory(value: String) = appVersionsHistory.set(value)

    fun setSearchSettings(value: String) = searchSettings.set(value)

    fun setMessagePanelBbCodes(value: String) = messagePanelBbCodes.set(value)

    fun setShowReportWarning(value: Boolean) = showReportWarning.set(value)

    fun setTooltipSearchSettings(value: Boolean) = tooltipSearchSettings.set(value)

    fun setTooltipMessagePanelSorting(value: Boolean) = tooltipMessagePanelSorting.set(value)


    fun deleteMessagePanelBbCodes() = messagePanelBbCodes.remove()


    fun getAppFirstStart(): Boolean = appFirstStart.get()

    fun getAppVersionsHistory(): String? = appVersionsHistory.get()

    fun getSearchSettings(): String? = searchSettings.get()

    fun getMessagePanelBbCodes(): String? = messagePanelBbCodes.get()

    fun getShowReportWarning(): Boolean = showReportWarning.get()

    fun getTooltipSearchSettings(): Boolean = tooltipSearchSettings.get()

    fun getTooltipMessagePanelSorting(): Boolean = tooltipMessagePanelSorting.get()


}