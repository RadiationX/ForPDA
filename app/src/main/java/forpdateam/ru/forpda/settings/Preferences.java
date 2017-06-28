package forpdateam.ru.forpda.settings;

import forpdateam.ru.forpda.App;

/**
 * Created by radiationx on 28.05.17.
 */

public class Preferences {

    public static final class Main {
        public final static String SHOW_NOTIFY_DOT = "main.show_notify_dot";
        public final static String WEBVIEW_FONT_SIZE = "main.webview.font_size";

        public static int getWebViewSize() {
            int size = App.getInstance().getPreferences().getInt(Preferences.Main.WEBVIEW_FONT_SIZE, 16);
            size = Math.max(Math.min(size, 64), 8);
            return size;
        }

        public static void setWebViewSize(int size) {
            size = Math.max(Math.min(size, 64), 8);
            App.getInstance().getPreferences().edit().putInt(Preferences.Main.WEBVIEW_FONT_SIZE, size).apply();
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
}
