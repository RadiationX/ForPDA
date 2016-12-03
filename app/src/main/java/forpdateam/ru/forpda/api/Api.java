package forpdateam.ru.forpda.api;

import java.util.Observer;

import forpdateam.ru.forpda.api.auth.Auth;
import forpdateam.ru.forpda.api.favorites.Favorites;
import forpdateam.ru.forpda.api.newslist.NewsList;
import forpdateam.ru.forpda.api.profile.Profile;
import forpdateam.ru.forpda.api.qms.Qms;
import forpdateam.ru.forpda.api.theme.Theme;

/**
 * Created by radiationx on 29.07.16.
 */
public class Api {
    private static Api INSTANCE = null;
    private static Qms qmsApi = null;
    private static Auth authApi = null;
    private static NewsList newsListApi = null;
    private static Profile profileApi = null;
    private static Theme themeApi = null;
    private static Favorites favoritesApi = null;
    private CountsObservable observable = new CountsObservable();
    private int qmsCount = 0, mentionsCount = 0;

    public static Qms Qms() {
        if (qmsApi == null) qmsApi = new Qms();
        return qmsApi;
    }


    /*Позже*/
    public static Auth Auth() {
        if (authApi == null) authApi = new Auth();
        return authApi;
    }

    public static NewsList NewsList() {
        if (newsListApi == null) newsListApi = new NewsList();
        return newsListApi;
    }

    public static Profile Profile() {
        if (profileApi == null) profileApi = new Profile();
        return profileApi;
    }

    public static Theme Theme() {
        if (themeApi == null) themeApi = new Theme();
        return themeApi;
    }

    public static Favorites Favorites() {
        if (favoritesApi == null) favoritesApi = new Favorites();
        return favoritesApi;
    }

    public static Api get() {
        if (INSTANCE == null) INSTANCE = new Api();
        return INSTANCE;
    }


    public int getQmsCount() {
        return qmsCount;
    }

    public void setQmsCount(int qmsCount) {
        this.qmsCount = qmsCount;
    }

    public int getMentionsCount() {
        return mentionsCount;
    }

    public void setMentionsCount(int mentionsCount) {
        this.mentionsCount = mentionsCount;
    }

    public void addObserver(Observer observer) {
        observable.addObserver(observer);
    }

    public void notifyObservers() {
        observable.notifyObservers();
    }


    private class CountsObservable extends java.util.Observable {
        @Override
        public synchronized boolean hasChanged() {
            return true;
        }
    }
}
