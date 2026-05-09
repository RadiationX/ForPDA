package forpdateam.ru.forpda.model.preferences

import forpdateam.ru.forpda.common.Preferences
import forpdateam.ru.forpda.common.flowpreferences.FlowPreferences

class OtherPreferencesHolder(
    private val preferences: FlowPreferences
) {

    val appFirstStart by lazy {
        preferences.getBoolean(Preferences.Other.APP_FIRST_START, true)
    }

    val appVersionsHistory by lazy {
        preferences.getString(Preferences.Other.APP_VERSIONS_HISTORY)
    }

    val searchSettings by lazy {
        preferences.getString(Preferences.Other.SEARCH_SETTINGS)
    }

    val messagePanelBbCodes by lazy {
        preferences.getString(Preferences.Other.MESSAGE_PANEL_BBCODES_SORT)
    }

    val showReportWarning by lazy {
        preferences.getBoolean(Preferences.Other.SHOW_REPORT_WARNING, true)
    }

    val tooltipSearchSettings by lazy {
        preferences.getBoolean(Preferences.Other.TOOLTIP_SEARCH_SETTINGS, true)
    }

    val tooltipMessagePanelSorting by lazy {
        preferences.getBoolean(Preferences.Other.TOOLTIP_MESSAGE_PANEL_SORTING, true)
    }
}