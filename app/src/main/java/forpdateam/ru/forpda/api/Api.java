package forpdateam.ru.forpda.api;

import forpdateam.ru.forpda.api.auth.Auth;
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

    public Api() {
        INSTANCE = this;
    }

    public static void Init() {
        INSTANCE = new Api();
    }

    public static Qms Qms() {
        if (qmsApi == null) qmsApi = new Qms();
        return qmsApi;
    }

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

    /*public static Api get() {
        if (INSTANCE == null) INSTANCE = new Api();
        return INSTANCE;
    }*/
}
