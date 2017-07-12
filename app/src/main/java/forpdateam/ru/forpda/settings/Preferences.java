package forpdateam.ru.forpda.settings;

import android.content.SharedPreferences;

import forpdateam.ru.forpda.App;

/**
 * Created by radiationx on 28.05.17.
 */

public class Preferences {
    private static SharedPreferences preferences() {
        return App.getInstance().getPreferences();
    }

    public static final class Main {
        public final static String SHOW_NOTIFY_DOT = "main.show_notify_dot";
        public final static String WEBVIEW_FONT_SIZE = "main.webview.font_size";
        public final static String IS_SYSTEM_DOWNLOADER = "main.is_system_downloader";


        public static int getWebViewSize() {
            int size = App.getInstance().getPreferences().getInt(Preferences.Main.WEBVIEW_FONT_SIZE, 16);
            size = Math.max(Math.min(size, 64), 8);
            return size;
        }

        public static void setWebViewSize(int size) {
            size = Math.max(Math.min(size, 64), 8);
            preferences().edit().putInt(Preferences.Main.WEBVIEW_FONT_SIZE, size).apply();
        }

        public static boolean isSystemDownloader() {
            return preferences().getBoolean(Main.IS_SYSTEM_DOWNLOADER, true);
        }

        public final class Theme {
            public final static String IS_DARK = "main.theme.is_dark";
        }
    }

    public final class Favorites {
        public final static String UNREAD_TOP = "lists.topic.unread_top";
        public final static String SHOW_DOT = "lists.topic.show_dot";
    }

    public final class Theme {
        public final static String SHOW_AVATARS = "theme.show_avatars";
        public final static String CIRCLE_AVATARS = "theme.circle_avatars";
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

            public static boolean isEnabled() {
                return preferences().getBoolean(ENABLED, true);
            }

            public static boolean isSoundEnabled() {
                return preferences().getBoolean(SOUND_ENABLED, true);
            }

            public static boolean isVibrationEnabled() {
                return preferences().getBoolean(VIBRATION_ENABLED, true);
            }

            public static boolean isIndicatorEnabled() {
                return preferences().getBoolean(INDICATOR_ENABLED, true);
            }

            public static boolean isAvatarsEnabled() {
                return preferences().getBoolean(AVATARS_ENABLED, true);
            }
        }

        public static final class Favorites {
            private final static String PREFIX = Notifications.PREFIX + "fav.";
            public final static String ENABLED = PREFIX + "enabled";
            public final static String ONLY_IMPORTANT = PREFIX + "only_important";

            public static boolean isEnabled() {
                return preferences().getBoolean(ENABLED, true);
            }

            public static boolean isOnlyImportant() {
                return preferences().getBoolean(ONLY_IMPORTANT, false);
            }
        }

        public static final class Qms {
            private final static String PREFIX = Notifications.PREFIX + "qms.";
            public final static String ENABLED = PREFIX + "enabled";

            public static boolean isEnabled() {
                return preferences().getBoolean(ENABLED, true);
            }
        }

        public static final class Mentions {
            private final static String PREFIX = Notifications.PREFIX + "mentions.";
            public final static String ENABLED = PREFIX + "enabled";

            public static boolean isEnabled() {
                return preferences().getBoolean(ENABLED, true);
            }
        }
    }
}
