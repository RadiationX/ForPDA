package forpdateam.ru.forpda.settings;

import android.content.Context;
import android.content.SharedPreferences;

import forpdateam.ru.forpda.App;

/**
 * Created by radiationx on 28.05.17.
 */

public class Preferences {
    private static SharedPreferences preferences(Context context) {
        return App.getPreferences(context);
    }

    public final static class Main {
        private final static String PREFIX = "main.";
        public final static String SHOW_NOTIFY_DOT = PREFIX + "show_notify_dot";
        public final static String NOTIFY_DOT_FAV = PREFIX + "notify_dot_fav";
        public final static String NOTIFY_DOT_QMS = PREFIX + "notify_dot_qms";
        public final static String NOTIFY_DOT_MENTIONS = PREFIX + "notify_dot_mentions";

        public final static String WEBVIEW_FONT_SIZE = PREFIX + "webview.font_size";
        public final static String IS_SYSTEM_DOWNLOADER = PREFIX + "is_system_downloader";
        public final static String IS_TABS_BOTTOM = PREFIX + "drawers.tab_stack_bottom";
        public final static String IS_EDITOR_MONOSPACE = "message_panel.is_monospace";
        public final static String IS_EDITOR_DEFAULT_HIDDEN = "message_panel.is_default_hidden";
        public final static String SCROLL_BUTTON_ENABLE = PREFIX + "scroll_button.enable";

        public static boolean isShowNotifyDot(Context context) {
            return preferences(context).getBoolean(SHOW_NOTIFY_DOT, true);
        }

        public static boolean isShowNotifyDotFav(Context context) {
            return preferences(context).getBoolean(NOTIFY_DOT_FAV, true);
        }

        public static boolean isShowNotifyDotQms(Context context) {
            return preferences(context).getBoolean(NOTIFY_DOT_QMS, true);
        }

        public static boolean isShowNotifyDotMentions(Context context) {
            return preferences(context).getBoolean(NOTIFY_DOT_MENTIONS, true);
        }

        public static boolean isSystemDownloader(Context context) {
            return preferences(context).getBoolean(IS_SYSTEM_DOWNLOADER, true);
        }

        public static boolean isTabsBottom(Context context) {
            return preferences(context).getBoolean(IS_TABS_BOTTOM, false);
        }

        public static boolean isEditorMonospace(Context context) {
            return preferences(context).getBoolean(IS_EDITOR_MONOSPACE, true);
        }

        public static boolean isEditorDefaultHidden(Context context) {
            return preferences(context).getBoolean(IS_EDITOR_DEFAULT_HIDDEN, true);
        }

        public static boolean isScrollButtonEnable(Context context) {
            return preferences(context).getBoolean(SCROLL_BUTTON_ENABLE, false);
        }

        public static int getWebViewSize(Context context) {
            int size = preferences(context).getInt(Preferences.Main.WEBVIEW_FONT_SIZE, 16);
            size = Math.max(Math.min(size, 64), 8);
            return size;
        }

        public static void setWebViewSize(Context context, int size) {
            size = Math.max(Math.min(size, 64), 8);
            preferences(context).edit().putInt(Preferences.Main.WEBVIEW_FONT_SIZE, size).apply();
        }

        public final static class Theme {
            private final static String PREFIX = Main.PREFIX + "theme.";
            public final static String IS_DARK = PREFIX + "is_dark";

            public static boolean isDark(Context context) {
                return preferences(context).getBoolean(IS_DARK, false);
            }
        }
    }

    public final static class Lists {
        private final static String PREFIX = "lists.";

        public final static class Topic {
            private final static String PREFIX = Lists.PREFIX + "topic.";
            public final static String UNREAD_TOP = PREFIX + "unread_top";
            public final static String SHOW_DOT = PREFIX + "show_dot";

            public static boolean isUnreadTop(Context context) {
                return preferences(context).getBoolean(UNREAD_TOP, false);
            }

            public static boolean isShowDot(Context context) {
                return preferences(context).getBoolean(SHOW_DOT, false);
            }
        }

        public final static class Favorites {
            private final static String PREFIX = Lists.PREFIX + "favorites.";
            public final static String LOAD_ALL = PREFIX + "load_all";
            public final static String SORTING_KEY = PREFIX + "sorting_key";
            public final static String SORTING_ORDER = PREFIX + "sorting_order";

            public static boolean isLoadAll(Context context) {
                return preferences(context).getBoolean(LOAD_ALL, false);
            }

            public static String getSortingKey(Context context) {
                return preferences(context).getString(SORTING_KEY, "");
            }

            public static String getSortingOrder(Context context) {
                return preferences(context).getString(SORTING_ORDER, "");
            }

            public static void setSortingKey(Context context, String key) {
                preferences(context).edit().putString(SORTING_KEY, key).apply();
            }

            public static void setSortingOrder(Context context, String order) {
                preferences(context).edit().putString(SORTING_ORDER, order).apply();
            }
        }
    }

    public final static class Theme {
        private final static String PREFIX = "theme.";
        public final static String SHOW_AVATARS = PREFIX + "show_avatars";
        public final static String CIRCLE_AVATARS = PREFIX + "circle_avatars";

        public static boolean isShowAvatars(Context context) {
            return preferences(context).getBoolean(SHOW_AVATARS, true);
        }

        public static boolean isCircleAvatars(Context context) {
            return preferences(context).getBoolean(CIRCLE_AVATARS, true);
        }
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

            public static boolean isEnabled(Context context) {
                return preferences(context).getBoolean(ENABLED, true);
            }

            public static boolean isSoundEnabled(Context context) {
                return preferences(context).getBoolean(SOUND_ENABLED, true);
            }

            public static boolean isVibrationEnabled(Context context) {
                return preferences(context).getBoolean(VIBRATION_ENABLED, true);
            }

            public static boolean isIndicatorEnabled(Context context) {
                return preferences(context).getBoolean(INDICATOR_ENABLED, true);
            }

            public static boolean isAvatarsEnabled(Context context) {
                return preferences(context).getBoolean(AVATARS_ENABLED, true);
            }

            public static long getLimit(Context context) {
                return Integer.parseInt(preferences(context).getString(LIMIT, "10")) * 1000;
            }
        }

        public static final class Favorites {
            private final static String PREFIX = Notifications.PREFIX + "fav.";
            public final static String ENABLED = PREFIX + "enabled";
            public final static String ONLY_IMPORTANT = PREFIX + "only_important";
            public final static String LIVE_TAB = PREFIX + "live_tab";

            public static boolean isEnabled(Context context) {
                return preferences(context).getBoolean(ENABLED, true);
            }

            public static boolean isOnlyImportant(Context context) {
                return preferences(context).getBoolean(ONLY_IMPORTANT, false);
            }

            public static boolean isLiveTab(Context context) {
                return preferences(context).getBoolean(LIVE_TAB, true);
            }
        }

        public static final class Qms {
            private final static String PREFIX = Notifications.PREFIX + "qms.";
            public final static String ENABLED = PREFIX + "enabled";

            public static boolean isEnabled(Context context) {
                return preferences(context).getBoolean(ENABLED, true);
            }
        }

        public static final class Mentions {
            private final static String PREFIX = Notifications.PREFIX + "mentions.";
            public final static String ENABLED = PREFIX + "enabled";

            public static boolean isEnabled(Context context) {
                return preferences(context).getBoolean(ENABLED, true);
            }
        }

        public static final class Update {
            private final static String PREFIX = Notifications.PREFIX + "update.";
            public final static String ENABLED = PREFIX + "enabled";

            public static boolean isEnabled(Context context) {
                return preferences(context).getBoolean(ENABLED, true);
            }
        }
    }
}
