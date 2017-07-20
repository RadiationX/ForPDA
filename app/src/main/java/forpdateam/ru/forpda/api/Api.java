package forpdateam.ru.forpda.api;

import forpdateam.ru.forpda.api.auth.Auth;
import forpdateam.ru.forpda.api.events.Events;
import forpdateam.ru.forpda.api.favorites.Favorites;
import forpdateam.ru.forpda.api.forum.Forum;
import forpdateam.ru.forpda.api.mentions.Mentions;
import forpdateam.ru.forpda.api.news.NewsApi;
import forpdateam.ru.forpda.api.profile.Profile;
import forpdateam.ru.forpda.api.qms.Qms;
import forpdateam.ru.forpda.api.reputation.Reputation;
import forpdateam.ru.forpda.api.search.Search;
import forpdateam.ru.forpda.api.theme.Theme;
import forpdateam.ru.forpda.api.theme.editpost.EditPost;
import forpdateam.ru.forpda.api.topcis.Topics;

/**
 * Created by radiationx on 29.07.16.
 */
public class Api {
    private static Api INSTANCE = null;
    private static Qms qmsApi = null;
    private static Auth authApi = null;
    private static NewsApi newsListApi = null;
    private static Profile profileRxApi = null;
    private static Theme themeApi = null;
    private static EditPost editPost = null;
    private static Favorites favoritesApi = null;
    private static Mentions mentions = null;
    private static Search search = null;
    private static Forum forum = null;
    private static Topics topics = null;
    private static Reputation reputation = null;
    private static Events events = null;

    private static IWebClient webClient = null;

    public static void setWebClient(IWebClient webClient) {
        Api.webClient = webClient;
    }

    public static IWebClient getWebClient() {
        return webClient;
    }

    public static Qms Qms() {
        if (qmsApi == null) qmsApi = new Qms();
        return qmsApi;
    }

    /*Позже*/
    public static Auth Auth() {
        if (authApi == null) authApi = new Auth();
        return authApi;
    }

    public static NewsApi NewsList() {
        if (newsListApi == null) newsListApi = new NewsApi();
        return newsListApi;
    }

    public static Profile Profile() {
        if (profileRxApi == null) profileRxApi = new Profile();
        return profileRxApi;
    }

    public static Theme Theme() {
        if (themeApi == null) themeApi = new Theme();
        return themeApi;
    }

    public static EditPost EditPost() {
        if (editPost == null) editPost = new EditPost();
        return editPost;
    }

    public static Favorites Favorites() {
        if (favoritesApi == null) favoritesApi = new Favorites();
        return favoritesApi;
    }

    public static Mentions Mentions() {
        if (mentions == null) mentions = new Mentions();
        return mentions;
    }

    public static Search Search() {
        if (search == null) search = new Search();
        return search;
    }

    public static Forum Forum() {
        if (forum == null) forum = new Forum();
        return forum;
    }

    public static Topics Topics() {
        if (topics == null) topics = new Topics();
        return topics;
    }

    public static Reputation Reputation() {
        if (reputation == null) reputation = new Reputation();
        return reputation;
    }

    public static Events Events() {
        if (events == null) events = new Events();
        return events;
    }

    public static Api get() {
        if (INSTANCE == null) INSTANCE = new Api();
        return INSTANCE;
    }
}
