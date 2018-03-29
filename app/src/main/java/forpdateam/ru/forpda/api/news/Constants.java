package forpdateam.ru.forpda.api.news;

/**
 * Created by isanechek on 11/10/16.
 */

public class Constants {
    /*Мне просто лень это писать каждый раз.*/
    public static final String CNBN = "Cannot Be Null";

    public static final int FORPDA_WRITE_EXTERNAL_STORAGE_RERMISSION = 1;


    /**
     * Errors
     */
    public static final String ERROR_LOAD_DATA = "error_load_data";
    public static final String ERROR_UPDATE_DATA = "error_update_data";
    public static final String ERROR_LOAD_MORE_DATA = "error_load_more_data";
    public static final String ERROR_LOAD_MORE_NEW_DATA = "error_load_more_new_data";
    public static final String ERROR_NO_INTERNET = "error_no_internet";
    public static final String ERROR_OTHER_ERRORS = "error_other_errors";

    /**
     * NewsApi Category Url
     */
    public static final String NEWS_CATEGORY_ROOT = "root";
    public static final String NEWS_URL_ROOT = "https://4pda.ru/";
    public static final String NEWS_CATEGORY_ALL = "all_news";
    public static final String NEWS_URL_ALL = "https://4pda.ru/news/";
    public static final String NEWS_CATEGORY_ARTICLES = "articles_news";
    public static final String NEWS_URL_ARTICLES = "https://4pda.ru/articles/";
    public static final String NEWS_CATEGORY_REVIEWS = "reviews_news";
    public static final String NEWS_URL_REVIEWS = "https://4pda.ru/reviews/";
    public static final String NEWS_CATEGORY_SOFTWARE = "software_news";
    public static final String NEWS_URL_SOFTWARE = "https://4pda.ru/software/";
    public static final String NEWS_CATEGORY_GAMES = "games_news";
    public static final String NEWS_URL_GAMES = "https://4pda.ru/games/";

    /**
     * NewsApi Subcategory Url
     */
    /*GAMES*/
    public static final String NEWS_SUBCATEGORY_DEVSTORY_GAMES = "ds_games_news";
    public static final String NEWS_URL_DEVSTORY_GAMES = "https://4pda.ru/games/tag/devstory/";
    public static final String NEWS_SUBCATEGORY_WP7_GAME = "wp_game_news";
    public static final String NEWS_URL_WP7_GAME = "https://4pda.ru/games/tag/games-for-windows-phone-7/";
    public static final String NEWS_SUBCATEGORY_IOS_GAME = "ios_game_news";
    public static final String NEWS_URL_IOS_GAME = "https://4pda.ru/games/tag/games-for-ios/";
    public static final String NEWS_SUBCATEGORY_ANDROID_GAME = "android_game_news";
    public static final String NEWS_URL_ANDROID_GAME = "https://4pda.ru/games/tag/games-for-android/";
    /*SOFTWARE*/
    public static final String NEWS_SUBCATEGORY_DEVSTORY_SOFTWARE = "ds_software_news";
    public static final String NEWS_URL_DEVSTORY_SOFTWARE = "https://4pda.ru/software/tag/devstory/";
    public static final String NEWS_SUBCATEGORY_WP7_SOFTWARE = "software_wp7-news";
    public static final String NEWS_URL_WP7_SOFTWARE = "https://4pda.ru/software/tag/programs-for-windows-phone-7/";
    public static final String NEWS_SUBCATEGORY_IOS_SOFTWARE = "software_ios_news";
    public static final String NEWS_URL_IOS_SOFTWARE= "https://4pda.ru/software/tag/programs-for-ios/";
    public static final String NEWS_SUBCATEGORY_ANDROID_SOFTWARE= "software_android_news";
    public static final String NEWS_URL_ANDROID_SOFTWARE = "https://4pda.ru/software/tag/programs-for-android/";
    /*REVIEWS*/
    public static final String NEWS_SUBCATEGORY_SMARTPHONES_REVIEWS = "s_r_news";
    public static final String NEWS_URL_SMARTPHONES_REVIEWS = "https://4pda.ru/reviews/tag/smartphones/";
    public static final String NEWS_SUBCATEGORY_TABLETS_REVIEWS = "t_r_news";
    public static final String NEWS_URL_TABLETS_REVIEWS = "https://4pda.ru/reviews/tag/tablets/";
    public static final String NEWS_SUBCATEGORY_SMART_WATCH_REVIEWS = "sw_r_news";
    public static final String NEWS_URL_SMART_WATCH_REVIEWS = "https://4pda.ru/reviews/tag/smart-watches/";
    public static final String NEWS_SUBCATEGORY_ACCESSORIES_REVIEWS = "a_r_news";
    public static final String NEWS_URL_ACCESSORIES_REVIEWS = "https://4pda.ru/reviews/tag/accessories/";
    public static final String NEWS_SUBCATEGORY_NOTEBOOKS_REVIEWS = "n_r_news";
    public static final String NEWS_URL_NOTEBOOKS_REVIEWS = "https://4pda.ru/reviews/tag/notebooks/";
    public static final String NEWS_SUBCATEGORY_ACOUSTICS_REVIEWS = "ac_r_news";
    public static final String NEWS_URL_ACOUSTICS_REVIEWS = "https://4pda.ru/reviews/tag/acoustics/";
    /*How to*/
    public static final String NEWS_SUBCATEGORY_HOW_TO_ANDROID = "h_t_a_news";
    public static final String NEWS_URL_HOW_TO_ANDROID = "https://4pda.ru/tag/how-to-android/?utm_source=slider1";
    public static final String NEWS_SUBCATEGORY_HOW_TO_IOS = "h_t_i_news";
    public static final String NEWS_URL_HOW_TO_IOS = "https://4pda.ru/tag/how-to-ios/?utm_source=slider1";
    public static final String NEWS_SUBCATEGORY_HOW_TO_WP = "h_t_w_news";
    public static final String NEWS_URL_HOW_TO_WP = "https://4pda.ru/tag/how-to-wp/?utm_source=slider1";
    public static final String NEWS_SUBCATEGORY_HOW_TO_INTERVIEW = "interview_news";
    public static final String NEWS_URL_HOW_TO_INTERVIEW = "https://4pda.ru/articles/tag/interview/";

    // news tabs
    public static final String TAB_OFFLINE = "offline";
    public static final String TAB_ALL = "news";
    public static final String TAB_ARTICLE = "article";
    public static final String TAB_REVIEWS = "reviews";
    public static final String TAB_SOFTWARE = "software";
    public static final String TAB_GAMES = "games";




    public static final String NEWS_LOAD_DATA_TASK = "news.load.data";
    public static final String NEWS_UPDATE_BACKGROUND_TASK = "update.background";
    public static final String COUNT_NEW_NEWS_ITEMS = "count.items";
    public static final String DETAILS_COVER = "count.items";
    public static final String NEWS_ERROR_LOAD_OR_UPDATE_TASK = "news.load.update.errro";



    // to details fragment args
    public static final String D_TITLE = "d.news.title";
    public static final String D_DATE = "d.news.date";
    public static final String D_USERNAME = "d.news.username";
    public static final String D_URL = "d.news.url";
    public static final String D_IMG = "d.news.img";
    public static final String D_ID = "d.news.id";

    //

}
