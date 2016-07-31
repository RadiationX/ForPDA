package forpdateam.ru.forpda.api;

import forpdateam.ru.forpda.api.login.Login;
import forpdateam.ru.forpda.api.newslist.NewsList;
import forpdateam.ru.forpda.api.qms.Qms;

/**
 * Created by radiationx on 29.07.16.
 */
public class Api {
    private static Api INSTANCE = null;
    private static Qms qmsApi = null;
    private static Login loginApi = null;
    private static NewsList newsListApi = null;

    public Api() {
        INSTANCE = this;
        qmsApi = new Qms();
        loginApi = new Login();
        newsListApi = new NewsList();
    }

    public static void Init() {
        INSTANCE = new Api();
    }

    public static Qms Qms() {
        return qmsApi;
    }

    public static Login Login() {
        return loginApi;
    }

    public static NewsList NewsList() {
        return newsListApi;
    }

    /*public static Api get() {
        if (INSTANCE == null) INSTANCE = new Api();
        return INSTANCE;
    }*/
}
