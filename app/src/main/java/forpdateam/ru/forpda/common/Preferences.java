package forpdateam.ru.forpda.common;

import android.content.Context;
import android.content.SharedPreferences;

import forpdateam.ru.forpda.App;

/**
 * Created by radiationx on 28.05.17.
 */

public class Preferences {

    public final static class Auth {
        public final static String USER_ID = "member_id";
        public final static String AUTH_KEY = "auth_key";

        public final static String COOKIE_MEMBER_ID = "cookie_member_id";
        public final static String COOKIE_PASS_HASH = "cookie_pass_hash";
        public final static String COOKIE_SESSION_ID = "cookie_session_id";
        public final static String COOKIE_ANONYMOUS = "cookie_anonymous";
        public final static String COOKIE_CF_CLEARANCE = "cookie_cf_clearance";
    }

    public final static class Other {
        public final static String APP_FIRST_START = "main.is_first_start";
        public final static String APP_VERSIONS_HISTORY = "app.versions.history";
        public final static String SEARCH_SETTINGS = "search_settings";
        public final static String MESSAGE_PANEL_BBCODES_SORT = "message_panel.bb_codes.sorted";


        public final static String SHOW_REPORT_WARNING = "show_report_warning";

        public final static String TOOLTIP_SEARCH_SETTINGS = "search.tooltip.settings";
        public final static String TOOLTIP_THEME_LONG_CLICK_SEND = "theme.tooltip.long_click_send";
        public final static String TOOLTIP_MESSAGE_PANEL_SORTING = "message_panel.tooltip.user_sorting";

    }

    public final static class Main {
        private final static String PREFIX = "main.";

        public final static String WEBVIEW_FONT_SIZE = PREFIX + "webview.font_size_v2";
        public final static String IS_SYSTEM_DOWNLOADER = PREFIX + "is_system_downloader";
        public final static String IS_EDITOR_MONOSPACE = "message_panel.is_monospace";
        public final static String IS_EDITOR_DEFAULT_HIDDEN = "message_panel.is_default_hidden";
        public final static String SCROLL_BUTTON_ENABLE = PREFIX + "scroll_button.enable";
        public final static String SHOW_BOTTOM_ARROW = PREFIX + "show_bottom_arrow";

        public final static class Theme {
            private final static String PREFIX = Main.PREFIX + "theme.";
            public final static String IS_DARK = PREFIX + "is_dark";
        }
    }

    public final static class Lists {
        private final static String PREFIX = "lists.";

        public final static class Topic {
            private final static String PREFIX = Lists.PREFIX + "topic.";
            public final static String UNREAD_TOP = PREFIX + "unread_top";
            public final static String SHOW_DOT = PREFIX + "show_dot";
        }

        public final static class Favorites {
            private final static String PREFIX = Lists.PREFIX + "favorites.";
            public final static String LOAD_ALL = PREFIX + "load_all";
            public final static String SORTING_KEY = PREFIX + "sorting_key";
            public final static String SORTING_ORDER = PREFIX + "sorting_order";
        }
    }

    public final static class Theme {
        private final static String PREFIX = "theme.";
        public final static String SHOW_AVATARS = PREFIX + "show_avatars";
        public final static String CIRCLE_AVATARS = PREFIX + "circle_avatars";
        public final static String ANCHOR_HISTORY = PREFIX + "anchor_history";
        public final static String HAT_OPENED = PREFIX + "hat_opened";
    }

    public static final class Notifications {
        private final static String PREFIX = "notifications.";


        public static final class Data {
            private final static String PREFIX = Notifications.PREFIX + "data.";
            public final static String QMS_EVENTS = PREFIX + "qms_events";
            public final static String FAVORITES_EVENTS = PREFIX + "favorites_events";

        }

        public static final class Main {
            private final static String PREFIX = Notifications.PREFIX + "main.";
            public final static String ENABLED = PREFIX + "enabled";
            public final static String SOUND_ENABLED = PREFIX + "sound_enabled";
            public final static String VIBRATION_ENABLED = PREFIX + "vibration_enabled";
            public final static String INDICATOR_ENABLED = PREFIX + "indicator_enabled";
            public final static String AVATARS_ENABLED = PREFIX + "avatars_enabled";
            public final static String LIMIT = PREFIX + "limit_period";
        }

        public static final class Favorites {
            private final static String PREFIX = Notifications.PREFIX + "fav.";
            public final static String ENABLED = PREFIX + "enabled";
            public final static String ONLY_IMPORTANT = PREFIX + "only_important";
            public final static String LIVE_TAB = PREFIX + "live_tab";
        }

        public static final class Qms {
            private final static String PREFIX = Notifications.PREFIX + "qms.";
            public final static String ENABLED = PREFIX + "enabled";
        }

        public static final class Mentions {
            private final static String PREFIX = Notifications.PREFIX + "mentions.";
            public final static String ENABLED = PREFIX + "enabled";
        }

        public static final class Update {
            private final static String PREFIX = Notifications.PREFIX + "update.";
            public final static String ENABLED = PREFIX + "enabled";
        }
    }
}
