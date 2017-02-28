package forpdateam.ru.forpda.client;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.webkit.WebSettings;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.utils.ourparser.Html;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Client {
    public final static String minimalPage = "http://4pda.ru/forum/index.php?showforum=200";
    private final static String userAgent = WebSettings.getDefaultUserAgent(App.getContext());
    private final static Pattern countsPattern = Pattern.compile("act=mentions[^>]*?><i[^>]*?>([^<]*?)<\\/i>[\\s\\S]*?act=fav[^>]*?><i[^>]*?>([^<]*?)<\\/i>[\\s\\S]*?act=qms[^>]*?data-count=\"([^\">]*?)\">");
    private final static Pattern errorPattern = Pattern.compile("^[\\s\\S]*?wr va-m text\">([\\s\\S]*?)</div></div></div></div><div class=\"footer\">");
    private static Client INSTANCE = new Client();
    private static Map<String, Cookie> cookies;
    private static List<Cookie> listCookies;
    private Map<String, String> redirects = new HashMap<>();
    private NetworkObservable networkObserver = new NetworkObservable();
    private Handler observerHandler = new Handler(Looper.getMainLooper());
    private Matcher countsMatcher, errorMatcher;
    private String tempGroup;

    //Class
    public Client() {
        INSTANCE = this;
        cookies = new HashMap<>();
        listCookies = new ArrayList<>();
        String member_id = App.getInstance().getPreferences().getString("cookie_member_id", null);
        String pass_hash = App.getInstance().getPreferences().getString("cookie_pass_hash", null);
        Api.Auth().setUserId(App.getInstance().getPreferences().getString("member_id", null));
        if (member_id != null && pass_hash != null) {
            Api.Auth().setState(true);
            //Первичная загрузка кукисов
            cookies.put("member_id", parseCookie(member_id));
            cookies.put("pass_hash", parseCookie(pass_hash));
        }
    }

    public static String getAuthKey() {
        return App.getInstance().getPreferences().getString("auth_key", "0");
    }

    public static Client getInstance() {
        return INSTANCE;
    }

    private Cookie parseCookie(String cookieFields) {
        /*Хранение: Url|:|Cookie*/
        String[] fields = cookieFields.split("\\|:\\|");
        return Cookie.parse(HttpUrl.parse(fields[0]), fields[1]);
    }

    private final OkHttpClient client = new OkHttpClient.Builder()
            .cookieJar(new CookieJar() {
                @Override
                public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                    Log.d("kek", "response cookies size " + cookies.size());
                    try {
                        for (Cookie cookie : cookies) {
                            if (cookie.name().matches("member_id|pass_hash")) {
                                //Сохранение кукисов cookie_member_id и cookie_pass_hash
                                App.getInstance().getPreferences().edit().putString("cookie_".concat(cookie.name()), url.toString().concat("|:|").concat(cookie.toString())).apply();
                                if (cookie.name().equals("member_id")) {
                                    //Сохранение и обновление member_id
                                    App.getInstance().getPreferences().edit().putString("member_id", cookie.value()).apply();
                                    Api.Auth().setUserId(cookie.value());
                                }
                            }
                            Client.cookies.put(cookie.name(), cookie);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public List<Cookie> loadForRequest(HttpUrl url) {
                    listCookies.clear();
                    listCookies.addAll(cookies.values());
                    Log.d("kek", "cookies size " + listCookies.size());
                    return listCookies;
                }
            })
            .build();

    //Network
    public String get(String url) throws Exception {
        return request(url, null, null);
    }

    public String post(String url, Map<String, String> headers) throws Exception {
        return request(url, headers, null);
    }

    public String post(String url, Map<String, String> headers, RequestFile file) throws Exception {
        return request(url, headers, file);
    }

    private String request(String url, Map<String, String> headers, RequestFile file) throws Exception {
        Log.d("kek", "request url " + url);
        if (url.substring(0, 2).equals("//")) {
            url = "http:".concat(url);
            Log.d("kek", "fixed request url " + url);
        }

        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .header("User-Agent", userAgent);
        if (headers != null || file != null) {
            //FormBody нужен, т.к не все формы корректно работают с MultipartBody
            if (file == null) {
                FormBody.Builder formBuilder = new FormBody.Builder();
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    formBuilder.addEncoded(entry.getKey(), entry.getValue());
                }
                requestBuilder.post(formBuilder.build());
            } else {
                MultipartBody.Builder multipartBuilder = new MultipartBody.Builder();
                multipartBuilder.setType(MultipartBody.FORM);
                if (headers != null) {
                    for (Map.Entry<String, String> entry : headers.entrySet()) {
                        multipartBuilder.addFormDataPart(entry.getKey(), entry.getValue());
                    }
                }
                multipartBuilder.addFormDataPart(file.getRequestName(), URLEncoder.encode(file.getFileName(), "CP1251"), RequestBodyUtil.create(MediaType.parse(file.getMimeType()), file.getFileStream()));
                requestBuilder.post(multipartBuilder.build());
            }
        }

        String res;
        Response response = null;
        try {
            response = client.newCall(requestBuilder.build()).execute();
            if (!response.isSuccessful())
                throw new OkHttpResponseException(response.code(), response.message(), url);
            res = response.body().string();
            getCounts(res);
            checkForumErrors(res);
            redirects.put(url, response.request().url().toString());
        } finally {
            if (response != null)
                response.close();
        }
        return res;
    }

    private void checkForumErrors(String res) throws Exception {
        if (errorMatcher == null)
            errorMatcher = errorPattern.matcher(res);
        else
            errorMatcher.reset(res);

        if (errorMatcher.find()) {
            throw new OnlyShowException(Html.fromHtml(errorMatcher.group(1)).toString());
        }
    }


    private void getCounts(String res) {
        if (countsMatcher == null)
            countsMatcher = countsPattern.matcher(res);
        else
            countsMatcher.reset(res);

        if (countsMatcher.find()) {
            tempGroup = countsMatcher.group(1);
            Api.get().setMentionsCount(tempGroup == null ? 0 : Integer.parseInt(tempGroup));

            tempGroup = countsMatcher.group(2);
            Api.get().setFavoritesCount(tempGroup == null ? 0 : Integer.parseInt(tempGroup));

            tempGroup = countsMatcher.group(3);
            Api.get().setQmsCount(tempGroup == null ? 0 : Integer.parseInt(tempGroup));

            observerHandler.post(() -> Api.get().notifyObservers());
        }
    }


    public String getRedirect(String url) {
        return redirects.get(url);
    }

    public static void clearCookies() {
        cookies.clear();
        listCookies.clear();
    }

    public void addNetworkObserver(Observer observer) {
        networkObserver.addObserver(observer);
    }

    public void notifyNetworkObservers(Boolean b) {
        networkObserver.notifyObservers(b);
    }

    private class NetworkObservable extends java.util.Observable {
        @Override
        public synchronized boolean hasChanged() {
            return true;
        }
    }

    public boolean getNetworkState() {
        ConnectivityManager cm =
                (ConnectivityManager) App.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }
}
