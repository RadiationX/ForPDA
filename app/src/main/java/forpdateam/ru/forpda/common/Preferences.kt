package forpdateam.ru.forpda.common

/**
 * Created by radiationx on 28.05.17.
 */
class Preferences {

    object Other {
        const val APP_FIRST_START: String = "main.is_first_start"
        const val SEARCH_SETTINGS: String = "search_settings_v2"
        const val MESSAGE_PANEL_BBCODES_SORT: String = "message_panel.bb_codes.sorted"


        const val SHOW_REPORT_WARNING: String = "show_report_warning"

        const val TOOLTIP_MESSAGE_PANEL_SORTING: String = "message_panel.tooltip.user_sorting"
    }

    object Main {
        private const val PREFIX = "main."

        const val WEBVIEW_FONT_SIZE: String = PREFIX + "webview.font_size_v2"
        const val IS_SYSTEM_DOWNLOADER: String = PREFIX + "is_system_downloader"
        const val IS_EDITOR_MONOSPACE: String = "message_panel.is_monospace"
        const val IS_EDITOR_DEFAULT_HIDDEN: String = "message_panel.is_default_hidden"
        const val SCROLL_BUTTON_ENABLE: String = PREFIX + "scroll_button.enable"
        const val SHOW_BOTTOM_ARROW: String = PREFIX + "show_bottom_arrow"

        object Theme {
            private const val PREFIX = Main.PREFIX + "theme."
            const val MODE: String = PREFIX + "mode"
        }

        enum class ThemeMode {
            LIGHT, DARK, SYSTEM
        }
    }

    object Lists {
        private const val PREFIX = "lists."

        object Topic {
            private const val PREFIX = Lists.PREFIX + "topic."
            const val UNREAD_TOP: String = PREFIX + "unread_top"
            const val SHOW_DOT: String = PREFIX + "show_dot"
        }

        object Favorites {
            private const val PREFIX = Lists.PREFIX + "favorites."
            const val LOAD_ALL: String = PREFIX + "load_all"
            const val SORTING_KEY: String = PREFIX + "sorting_key"
            const val SORTING_ORDER: String = PREFIX + "sorting_order"
        }
    }

    object Theme {
        private const val PREFIX = "theme."
        const val SHOW_AVATARS: String = PREFIX + "show_avatars"
        const val CIRCLE_AVATARS: String = PREFIX + "circle_avatars"
        const val ANCHOR_HISTORY: String = PREFIX + "anchor_history"
        const val HAT_OPENED: String = PREFIX + "hat_opened"
    }

    object Notifications {
        private const val PREFIX = "notifications."


        object Data {
            private const val PREFIX = Notifications.PREFIX + "data."
            const val QMS_EVENTS: String = PREFIX + "qms_events"
            const val FAVORITES_EVENTS: String = PREFIX + "favorites_events"
        }

        object Main {
            private const val PREFIX = Notifications.PREFIX + "main."
            const val ENABLED: String = PREFIX + "enabled"
            const val SOUND_ENABLED: String = PREFIX + "sound_enabled"
            const val VIBRATION_ENABLED: String = PREFIX + "vibration_enabled"
            const val INDICATOR_ENABLED: String = PREFIX + "indicator_enabled"
            const val AVATARS_ENABLED: String = PREFIX + "avatars_enabled"
            const val PERIOD_SEC: String = PREFIX + "period_sec"
        }

        object Favorites {
            private const val PREFIX = Notifications.PREFIX + "fav."
            const val ENABLED: String = PREFIX + "enabled"
            const val ONLY_IMPORTANT: String = PREFIX + "only_important"
        }

        object Qms {
            private const val PREFIX = Notifications.PREFIX + "qms."
            const val ENABLED: String = PREFIX + "enabled"
        }

        object TopicMentions {
            private const val PREFIX = Notifications.PREFIX + "mentions."
            const val ENABLED: String = PREFIX + "enabled"
        }

        object SiteMentions {
            private const val PREFIX = Notifications.PREFIX + "comments."
            const val ENABLED: String = PREFIX + "enabled"
        }

        object Forums {
            private const val PREFIX = Notifications.PREFIX + "forums."
            const val ENABLED: String = PREFIX + "enabled"
        }

        object Update {
            private const val PREFIX = Notifications.PREFIX + "update."
            const val ENABLED: String = PREFIX + "enabled"
        }
    }
}
