package forpdateam.ru.forpda.apirx;

import forpdateam.ru.forpda.apirx.apiclasses.AuthRx;
import forpdateam.ru.forpda.apirx.apiclasses.DevDbRx;
import forpdateam.ru.forpda.apirx.apiclasses.EditPostRx;
import forpdateam.ru.forpda.apirx.apiclasses.FavoritesRx;
import forpdateam.ru.forpda.apirx.apiclasses.ForumRx;
import forpdateam.ru.forpda.apirx.apiclasses.MentionsRx;
import forpdateam.ru.forpda.apirx.apiclasses.NewsRx;
import forpdateam.ru.forpda.apirx.apiclasses.ProfileRx;
import forpdateam.ru.forpda.apirx.apiclasses.QmsRx;
import forpdateam.ru.forpda.apirx.apiclasses.ReputationRx;
import forpdateam.ru.forpda.apirx.apiclasses.SearchRx;
import forpdateam.ru.forpda.apirx.apiclasses.ThemeRx;
import forpdateam.ru.forpda.apirx.apiclasses.TopicsRx;

/**
 * Created by radiationx on 25.03.17.
 */

public class RxApi {
    private static RxApi INSTANCE = null;
    private static QmsRx qmsApi = null;
    private static AuthRx authApi = null;
    private static NewsRx newsApi = null;
    private static ProfileRx profileApi = null;
    private static ThemeRx themeApi = null;
    private static EditPostRx editPost = null;
    private static FavoritesRx favoritesApi = null;
    private static MentionsRx mentions = null;
    private static SearchRx search = null;
    private static ForumRx forum = null;
    private static TopicsRx topics = null;
    private static ReputationRx reputation = null;
    private static DevDbRx devDb = null;

    public static QmsRx Qms() {
        if (qmsApi == null) qmsApi = new QmsRx();
        return qmsApi;
    }

    public static AuthRx Auth() {
        if (authApi == null) authApi = new AuthRx();
        return authApi;
    }

    public static NewsRx NewsList() {
        if (newsApi == null) newsApi = new NewsRx();
        return newsApi;
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

    public static DevDbRx DevDb() {
        if (devDb == null) devDb = new DevDbRx();
        return devDb;
    }

    public static RxApi get() {
        if (INSTANCE == null) INSTANCE = new RxApi();
        return INSTANCE;
    }
}
