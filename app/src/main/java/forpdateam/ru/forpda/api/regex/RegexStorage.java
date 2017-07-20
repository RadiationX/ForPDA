package forpdateam.ru.forpda.api.regex;

import android.support.annotation.NonNull;

import forpdateam.ru.forpda.App;

/**
 * Created by isanechek on 6/13/17.
 */

public class RegexStorage {

    public static class News {
        public static class List {
            public static void saveListPattern(@NonNull String pattern) {
                App.getInstance().getPreferences().edit().putString("news.list.pattern", pattern).apply();
            }

            public static String getListPattern() {
                return App.getInstance().getPreferences().getString("news.list.pattern", DefaultRegex.News.List.getListPattern());
            }
        }


        public static class Details {

            // Root pattern
            public static void saveRootDetailsPattern(@NonNull String pattern) {
                App.getInstance().getPreferences().edit().putString("news.details.root.pattern", pattern).apply();
            }

            public static String getRootDetailsPattern() {
                return App.getInstance().getPreferences().getString("news.details.root.pattern", DefaultRegex.News.Details.getDetailsRootPattern());
            }

            // Content block
            public static void saveContentPattern(@NonNull String pattern) {
                App.getInstance().getPreferences().edit().putString("news.details.content.pattern", pattern).apply();
            }

            public static String getContentPattern() {
                return App.getInstance().getPreferences().getString("news.details.content.pattern", DefaultRegex.News.Details.getDetailsContentPattern());
            }

            // Comments block
            public static void saveCommentsPattern(@NonNull String pattern) {
                App.getInstance().getPreferences().edit().putString("news.details.comments.pattern", pattern).apply();
            }

            public static String getCommentsPattern() {
                return App.getInstance().getPreferences().getString("news.details.comments.pattern", DefaultRegex.News.Details.getDetailsCommentsPattern());
            }

            // Navigation block
            public static void saveNavigationPattern(@NonNull String pattern) {
                App.getInstance().getPreferences().edit().putString("news.details.navigation.pattern", pattern).apply();
            }

            public static String getNavigationPattern() {
                return App.getInstance().getPreferences().getString("news.details.navigation.pattern", DefaultRegex.News.Details.getDetailsNavigationPattern());
            }

            // More news block
            public static void saveMoreNewsPattern(@NonNull String pattern) {
                App.getInstance().getPreferences().edit().putString("news.details.more.news.pattern", pattern).apply();
            }

            public static String getMoreNewsPattern() {
                return App.getInstance().getPreferences().getString("news.details.more.news.pattern", DefaultRegex.News.Details.getDetailsMoreNewsPattern());
            }
        }

    }
}