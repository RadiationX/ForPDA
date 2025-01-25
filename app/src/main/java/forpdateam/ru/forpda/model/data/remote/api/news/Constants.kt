package forpdateam.ru.forpda.model.data.remote.api.news

/**
 * Created by isanechek on 11/10/16.
 */
object Constants {
    /*Мне просто лень это писать каждый раз.*/
    const val CNBN: String = "Cannot Be Null"

    const val FORPDA_WRITE_EXTERNAL_STORAGE_RERMISSION: Int = 1


    /**
     * Errors
     */
    const val ERROR_LOAD_DATA: String = "error_load_data"
    const val ERROR_UPDATE_DATA: String = "error_update_data"
    const val ERROR_LOAD_MORE_DATA: String = "error_load_more_data"
    const val ERROR_LOAD_MORE_NEW_DATA: String = "error_load_more_new_data"
    const val ERROR_NO_INTERNET: String = "error_no_internet"
    const val ERROR_OTHER_ERRORS: String = "error_other_errors"

    /**
     * NewsApi Category Url
     */
    const val NEWS_CATEGORY_ROOT: String = "root"
    const val NEWS_URL_ROOT: String = "https://4pda.to/"
    const val NEWS_CATEGORY_ALL: String = "all_news"
    const val NEWS_URL_ALL: String = "https://4pda.to/news/"
    const val NEWS_CATEGORY_ARTICLES: String = "articles_news"
    const val NEWS_URL_ARTICLES: String = "https://4pda.to/articles/"
    const val NEWS_CATEGORY_REVIEWS: String = "reviews_news"
    const val NEWS_URL_REVIEWS: String = "https://4pda.to/reviews/"
    const val NEWS_CATEGORY_SOFTWARE: String = "software_news"
    const val NEWS_URL_SOFTWARE: String = "https://4pda.to/software/"
    const val NEWS_CATEGORY_GAMES: String = "games_news"
    const val NEWS_URL_GAMES: String = "https://4pda.to/games/"

    /**
     * NewsApi Subcategory Url
     */
    /*GAMES*/
    const val NEWS_SUBCATEGORY_DEVSTORY_GAMES: String = "ds_games_news"
    const val NEWS_URL_DEVSTORY_GAMES: String = "https://4pda.to/games/tag/devstory/"
    const val NEWS_SUBCATEGORY_WP7_GAME: String = "wp_game_news"
    const val NEWS_URL_WP7_GAME: String = "https://4pda.to/games/tag/games-for-windows-phone-7/"
    const val NEWS_SUBCATEGORY_IOS_GAME: String = "ios_game_news"
    const val NEWS_URL_IOS_GAME: String = "https://4pda.to/games/tag/games-for-ios/"
    const val NEWS_SUBCATEGORY_ANDROID_GAME: String = "android_game_news"
    const val NEWS_URL_ANDROID_GAME: String = "https://4pda.to/games/tag/games-for-android/"

    /*SOFTWARE*/
    const val NEWS_SUBCATEGORY_DEVSTORY_SOFTWARE: String = "ds_software_news"
    const val NEWS_URL_DEVSTORY_SOFTWARE: String = "https://4pda.to/software/tag/devstory/"
    const val NEWS_SUBCATEGORY_WP7_SOFTWARE: String = "software_wp7-news"
    const val NEWS_URL_WP7_SOFTWARE: String =
        "https://4pda.to/software/tag/programs-for-windows-phone-7/"
    const val NEWS_SUBCATEGORY_IOS_SOFTWARE: String = "software_ios_news"
    const val NEWS_URL_IOS_SOFTWARE: String = "https://4pda.to/software/tag/programs-for-ios/"
    const val NEWS_SUBCATEGORY_ANDROID_SOFTWARE: String = "software_android_news"
    const val NEWS_URL_ANDROID_SOFTWARE: String =
        "https://4pda.to/software/tag/programs-for-android/"

    /*REVIEWS*/
    const val NEWS_SUBCATEGORY_SMARTPHONES_REVIEWS: String = "s_r_news"
    const val NEWS_URL_SMARTPHONES_REVIEWS: String = "https://4pda.to/reviews/tag/smartphones/"
    const val NEWS_SUBCATEGORY_TABLETS_REVIEWS: String = "t_r_news"
    const val NEWS_URL_TABLETS_REVIEWS: String = "https://4pda.to/reviews/tag/tablets/"
    const val NEWS_SUBCATEGORY_SMART_WATCH_REVIEWS: String = "sw_r_news"
    const val NEWS_URL_SMART_WATCH_REVIEWS: String = "https://4pda.to/reviews/tag/smart-watches/"
    const val NEWS_SUBCATEGORY_ACCESSORIES_REVIEWS: String = "a_r_news"
    const val NEWS_URL_ACCESSORIES_REVIEWS: String = "https://4pda.to/reviews/tag/accessories/"
    const val NEWS_SUBCATEGORY_NOTEBOOKS_REVIEWS: String = "n_r_news"
    const val NEWS_URL_NOTEBOOKS_REVIEWS: String = "https://4pda.to/reviews/tag/notebooks/"
    const val NEWS_SUBCATEGORY_ACOUSTICS_REVIEWS: String = "ac_r_news"
    const val NEWS_URL_ACOUSTICS_REVIEWS: String = "https://4pda.to/reviews/tag/acoustics/"

    /*How to*/
    const val NEWS_SUBCATEGORY_HOW_TO_ANDROID: String = "h_t_a_news"
    const val NEWS_URL_HOW_TO_ANDROID: String =
        "https://4pda.to/tag/how-to-android/?utm_source=slider1"
    const val NEWS_SUBCATEGORY_HOW_TO_IOS: String = "h_t_i_news"
    const val NEWS_URL_HOW_TO_IOS: String = "https://4pda.to/tag/how-to-ios/?utm_source=slider1"
    const val NEWS_SUBCATEGORY_HOW_TO_WP: String = "h_t_w_news"
    const val NEWS_URL_HOW_TO_WP: String = "https://4pda.to/tag/how-to-wp/?utm_source=slider1"
    const val NEWS_SUBCATEGORY_HOW_TO_INTERVIEW: String = "interview_news"
    const val NEWS_URL_HOW_TO_INTERVIEW: String = "https://4pda.to/articles/tag/interview/"

    // news tabs
    const val TAB_OFFLINE: String = "offline"
    const val TAB_ALL: String = "news"
    const val TAB_ARTICLE: String = "article"
    const val TAB_REVIEWS: String = "reviews"
    const val TAB_SOFTWARE: String = "software"
    const val TAB_GAMES: String = "games"


    const val NEWS_LOAD_DATA_TASK: String = "news.load.data"
    const val NEWS_UPDATE_BACKGROUND_TASK: String = "update.background"
    const val COUNT_NEW_NEWS_ITEMS: String = "count.items"
    const val DETAILS_COVER: String = "count.items"
    const val NEWS_ERROR_LOAD_OR_UPDATE_TASK: String = "news.load.update.errro"


    // to details fragment args
    const val D_TITLE: String = "d.news.title"
    const val D_DATE: String = "d.news.date"
    const val D_USERNAME: String = "d.news.username"
    const val D_URL: String = "d.news.url"
    const val D_IMG: String = "d.news.img"
    const val D_ID: String = "d.news.id" //
}
