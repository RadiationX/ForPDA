package forpdateam.ru.forpda.api.regex;

/**
 * Created by isanechek on 6/13/17.
 */

public class DefaultRegex {

    /*************************************NEWS************************************/

    public static class News {
        public static class List {
            /* Groups news:
            * 1. Link
            * 2. Id
            * 3. Title
            * 4. Image Url
            * 5. Comments Count
            * 6. Date
            * 7. Author id
            * 8. Author
            * 9. Description
            * 10.  Tags
            *
            * Groups  reviews:
            * 11. Ссылка
            * 12. Id
            * 13. Ссылка изображения
            * 14. Заголовок
            * 15. Кол-во комментов
            * 16. Дата курильщика
            * 17. Дата нормального человека
            * 18. Ник автора
            * 19. Название девайса, возможно можно связать с devdb
            * 20. Контент
            *
            * */
            public static String getListPattern() {
                return "<article[^>]*?class=\"post[^\"]*?\"[^>]*?>[^<]*?<div[^>]*?>[^<]*?<a[^>]*?href=\"([^\"]*\\/(\\d+)\\/?)\"[^>]*?title=\"([^\"]*?)\"[\\s\\S]*?<img[^>]*?src=\"([^\"]*?)\"[\\s\\S]*?<a[^>]*?>([^<]*?)<\\/a>[\\s\\S]*?<em[^>]*?class=\"date\"[^>]*?>([^<]*?)<\\/em>[\\s\\S]*?<a[^>]*?href=\"[^\"]*?showuser=(\\d+)\"[^>]*?>([^<]*?)<\\/a>[\\s\\S]*?<div[^>]*?itemprop=\"description\">([\\s\\S]*?)<\\/div>[\\s\\S]*?<div[^>]*?class=\"meta\">([\\s\\S]*?)<\\/div>[\\s\\S]*?<\\/article>|<li itemscope[^>]*?>[^<]*?<div class=\"photo\">[^<]*?<a[^>]*?href=\"([^\"]*\\/(\\d+)\\/?)\"[^>]*?>[^<]*?<img[^>]*?src=\"([^\"]*?)\"[^>]*?title=\"([^\"]*?)\"[^>]*?>[\\s\\S]*?<a[^>]*?class=\"v-count\"[^>]*?>([^<]*?)<\\/a>[\\s\\S]*?<meta[^>]*?content=\"((\\d+-\\d+-\\d+)[^\"]*?)\"[^>]*?>[\\s\\S]*?<div class=\"content\" itemprop=\"description\">[\\s\\S]*?<div class=\"summary\"[^>]*?>[^<]*?<meta[^>]*?content=\"([^\"]*?)\"[^>]*?>[^<]*?<meta[^>]*?content=\"([^\"]*?)\"[^>]*?>[^<]*?<div itemprop=\"description\">([\\s\\S]*?)<\\/div>[^<]*?<\\/div>[^<]*?<span class=\"bg-shadow\">";
            }
        }

        public static class Details {

            /* Groups:
            * 1. Content source
            * 2. Materials items source
            * 3. Magic id for newer/older navigation
            * 4. Comments Count
            * 5. Comments tree source */

            public static String getDetailsRootPattern() {
                return "<div class=\"content-box\" itemprop=\"articleBody\"[^>]*?>([\\s\\S]*?)<\\/div>[^<]*?<\\/div>[^<]*?<\\/div>(?:[^<]*?<div class=\"materials-box\"[^>]*?>[\\s\\S]*?<ul class=\"materials-slider\"[^>]*?>([\\s\\S]*?)<\\\\/ul>[^<]*?<\\/div>)?[^<]*?<ul class=\"page-nav[^\"]*?\">[\\s\\S]*?<a href=\"[^\"]*?\\/(\\d+)\\/\"[\\s\\S]*?<\\/ul>[\\s\\S]*?<div class=\"comment-box\" id=\"comments\"[^>]*?>[^<]*?<div class=\"heading\"[^>]*?>[^>]*?<h2>(\\d+)[^<]*?<\\/h2>[\\s\\S]*?(<ul[\\s\\S]*?<\\/ul>)[^<]*?<form";
            }

            // Content block
            public static String getDetailsContentPattern() {
                return "<div class=\"content-box\" itemprop=\"articleBody\"[^>]*?>([\\s\\S]*?)<\\/div>[^<]*?<\\/div>[^<]*?<\\/div>[^<]*?";
            }

            // Comments block
            public static String getDetailsCommentsPattern() {
                return "<div class=\"comment-box\" id=\"comments\"[^>]*?>[^<]*?<div class=\"heading\"[^>]*?>[^>]*?<h2>(\\d+)[^<]*?<\\/h2>[\\s\\S]*?(<ul[\\s\\S]*?<\\/ul>)[^<]*?<form";
            }

            // Navigation block. Ps. Only id's
            public static String getDetailsNavigationPattern() {
                return "<ul class=\"page-nav[^\"]*?\">[\\s\\S]*?<a href=\"[^\"]*?\\/(\\d+)\\/\"[\\s\\S]*?<\\/ul>[\\s\\S]*?";
            }

            // More news block
             /* Groups:
              * 1. News id
              * 2. Image url
              * 3. Title */
            public static String getDetailsMoreNewsPattern() {
                return "<li class=\"slider-item\"[^>]*?>[^<]*?<a href=\"[^\"]*?\\/(\\d+)\\/\\?[^\"]*?\"[^>]*?><img src=\"([^\"]*?)\"[^>]*?>[^<]*?<\\/a>[^<]*?<h3>[^<]*?<a[^>]*?>([\\s\\S]*?)<\\/a>[^<]*?<\\/h3>";
            }
        }
    }
}
