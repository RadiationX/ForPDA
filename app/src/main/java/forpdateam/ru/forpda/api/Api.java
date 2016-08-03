package forpdateam.ru.forpda.api;

import forpdateam.ru.forpda.api.login.Login;
import forpdateam.ru.forpda.api.newslist.NewsList;
import forpdateam.ru.forpda.api.profile.Profile;
import forpdateam.ru.forpda.api.qms.Qms;

/**
 * Created by radiationx on 29.07.16.
 */
public class Api {
    private static Api INSTANCE = null;
    private static Qms qmsApi = null;
    private static Login loginApi = null;
    private static NewsList newsListApi = null;
    private static Profile profileApi = null;

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

    public static Login Login() {
        if (loginApi == null) loginApi = new Login();
        return loginApi;
    }

    public static NewsList NewsList() {
        if (newsListApi == null) newsListApi = new NewsList();
        return newsListApi;
    }

    public static Profile Profile() {
        if (profileApi == null) profileApi = new Profile();
        return profileApi;
    }

    /*public static Api get() {
        if (INSTANCE == null) INSTANCE = new Api();
        return INSTANCE;
    }*/
}
