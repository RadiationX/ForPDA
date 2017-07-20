package forpdateam.ru.forpda.rxapi;

import forpdateam.ru.forpda.api.news.NewsApi;
import forpdateam.ru.forpda.rxapi.apiclasses.AuthRx;
import forpdateam.ru.forpda.rxapi.apiclasses.EditPostRx;
import forpdateam.ru.forpda.rxapi.apiclasses.FavoritesRx;
import forpdateam.ru.forpda.rxapi.apiclasses.ForumRx;
import forpdateam.ru.forpda.rxapi.apiclasses.MentionsRx;
import forpdateam.ru.forpda.rxapi.apiclasses.ProfileRx;
import forpdateam.ru.forpda.rxapi.apiclasses.QmsRx;
import forpdateam.ru.forpda.rxapi.apiclasses.ReputationRx;
import forpdateam.ru.forpda.rxapi.apiclasses.SearchRx;
import forpdateam.ru.forpda.rxapi.apiclasses.ThemeRx;
import forpdateam.ru.forpda.rxapi.apiclasses.TopicsRx;

/**
 * Created by radiationx on 25.03.17.
 */

public class RxApi {
    private static RxApi INSTANCE = null;
    private static QmsRx qmsApi = null;
    private static AuthRx authApi = null;
    private static NewsApi newsListApi = null;
    private static ProfileRx profileApi = null;
    private static ThemeRx themeApi = null;
    private static EditPostRx editPost = null;
    private static FavoritesRx favoritesApi = null;
    private static MentionsRx mentions = null;
    private static SearchRx search = null;
    private static ForumRx forum = null;
    private static TopicsRx topics = null;
    private static ReputationRx reputation = null;

    public static QmsRx Qms() {
        if (qmsApi == null) qmsApi = new QmsRx();
        return qmsApi;
    }

    public static AuthRx Auth() {
        if (authApi == null) authApi = new AuthRx();
        return authApi;
    }

    public static NewsApi NewsList() {
        if (newsListApi == null) newsListApi = new NewsApi();
        return newsListApi;
    }

    public static ProfileRx Profile() {
        if (profileApi == null) profileApi = new ProfileRx();
        return profileApi;
    }

    public static ThemeRx Theme() {
        if (themeApi == null) themeApi = new ThemeRx();
        return themeApi;
    }

    public static EditPostRx EditPost() {
        if (editPost == null) editPost = new EditPostRx();
        return editPost;
    }

    public static FavoritesRx Favorites() {
        if (favoritesApi == null) favoritesApi = new FavoritesRx();
        return favoritesApi;
    }

    public static MentionsRx Mentions() {
        if (mentions == null) mentions = new MentionsRx();
        return mentions;
    }

    public static SearchRx Search() {
        if (search == null) search = new SearchRx();
        return search;
    }

    public static ForumRx Forum() {
        if (forum == null) forum = new ForumRx();
        return forum;
    }

    public static TopicsRx Topics() {
        if (topics == null) topics = new TopicsRx();
        return topics;
    }

    public static ReputationRx Reputation() {
        if (reputation == null) reputation = new ReputationRx();
        return reputation;
    }

    public static RxApi get() {
        if (INSTANCE == null) INSTANCE = new RxApi();
        return INSTANCE;
    }
}
