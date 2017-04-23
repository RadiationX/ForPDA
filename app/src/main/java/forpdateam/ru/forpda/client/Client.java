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
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.api.IWebClient;
import forpdateam.ru.forpda.api.RequestFile;
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

public class Client implements IWebClient {
    private final static String userAgent = WebSettings.getDefaultUserAgent(App.getContext());
    private final static Pattern countsPattern = Pattern.compile("act=mentions[^>]*?><i[^>]*?>([^<]*?)<\\/i>[\\s\\S]*?act=fav[^>]*?><i[^>]*?>([^<]*?)<\\/i>[\\s\\S]*?act=qms[^>]*?data-count=\"([^\">]*?)\">");
    private final static Pattern errorPattern = Pattern.compile("^[\\s\\S]*?wr va-m text\">([\\s\\S]*?)</div></div></div></div><div class=\"footer\">");
    private static Client INSTANCE = null;
    private static Map<String, Cookie> cookies;
    private static List<Cookie> listCookies;
    private Map<String, String> redirects = new HashMap<>();
    private NetworkObservable networkObserver = new NetworkObservable();
    private Handler observerHandler = new Handler(Looper.getMainLooper());
    private Matcher countsMatcher, errorMatcher;
    private String tempGroup;

    //Class
    public Client() {
        Api.setWebClient(this);
        cookies = new HashMap<>();
        listCookies = new ArrayList<>();
        String member_id = App.getInstance().getPreferences().getString("cookie_member_id", null);
        String pass_hash = App.getInstance().getPreferences().getString("cookie_pass_hash", null);
        ClientHelper.setUserId(App.getInstance().getPreferences().getString("member_id", null));
        Log.d("FORPDA_LOG", "INIT AUTH DATA " + member_id + " : " + pass_hash + " : " + App.getInstance().getPreferences().getString("member_id", null));
        if (member_id != null && pass_hash != null) {
            ClientHelper.setAuthState(ClientHelper.AUTH_STATE_LOGIN);
            //Первичная загрузка кукисов
            cookies.put("member_id", parseCookie(member_id));
            cookies.put("pass_hash", parseCookie(pass_hash));
        }
    }

    public String getAuthKey() {
        return App.getInstance().getPreferences().getString("auth_key", "0");
    }

    public static Client getInstance() {
        if (INSTANCE == null) INSTANCE = new Client();
        return INSTANCE;
    }

    private Cookie parseCookie(String cookieFields) {
        /*Хранение: Url|:|Cookie*/
        String[] fields = cookieFields.split("\\|:\\|");
        return Cookie.parse(HttpUrl.parse(fields[0]), fields[1]);
    }

    private String cookieToPref(String url, Cookie cookie) {
        return url.concat("|:|").concat(cookie.toString());
    }

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .cookieJar(new CookieJar() {
                Pattern authPattern = Pattern.compile("4pda\\.ru\\/forum\\/[\\s\\S]*?act=(?:auth|logout)");
                Matcher matcher;

                @Override
                public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                    Log.d("FORPDA_LOG", "response url " + url.toString());
                    Log.d("FORPDA_LOG", "response cookies size " + cookies.size());

                    if (matcher == null)
                        matcher = authPattern.matcher(url.toString());
                    else
                        matcher = matcher.reset(url.toString());

                    if (matcher.find()) {
                        for (Cookie cookie : cookies) {
                            Log.e("FORPDA_LOG", "response " + cookie.name() + " : " + cookie.value());
                            boolean isMemberId = cookie.name().equals("member_id");
                            boolean isPassHash = cookie.name().equals("pass_hash");
                            if (isMemberId || isPassHash) {
                                if (cookie.value().equals("deleted")) {
                                    App.getInstance().getPreferences().edit().remove("cookie_".concat(cookie.name())).apply();
                                    if (Client.cookies.containsKey(cookie.name())) {
                                        Client.cookies.remove(cookie.name());
                                    }
                                } else {
                                    //Сохранение кукисов cookie_member_id и cookie_pass_hash
                                    if (isMemberId) {
                                        App.getInstance().getPreferences().edit().putString("cookie_member_id", cookieToPref(url.toString(), cookie)).apply();
                                        App.getInstance().getPreferences().edit().putString("member_id", cookie.value()).apply();
                                        ClientHelper.setUserId(cookie.value());
                                    }
                                    if (isPassHash) {
                                        App.getInstance().getPreferences().edit().putString("cookie_pass_hash", cookieToPref(url.toString(), cookie)).apply();
                                        App.getInstance().getPreferences().edit().putString("auth_key", cookie.value()).apply();
                                    }
                                    if (!Client.cookies.containsKey(cookie.name())) {
                                        Client.cookies.put(cookie.name(), cookie);
                                    }
                                }
                            }
                        }
                    }
                }

                @Override
                public List<Cookie> loadForRequest(HttpUrl url) {
                    listCookies.clear();
                    listCookies.addAll(Client.cookies.values());
                    Log.d("FORPDA_LOG", "cookies size " + listCookies.size());
                    return listCookies;
                }
            })
            .build();

    //Network
    @Override
    public String get(String url) throws Exception {
        return request(url, null, null, false);
    }

    @Override
    public String post(String url, Map<String, String> headers) throws Exception {
        return request(url, headers, null, false);
    }

    @Override
    public String post(String url, Map<String, String> headers, boolean formBody) throws Exception {
        return request(url, headers, null, formBody);
    }

    @Override
    public String post(String url, Map<String, String> headers, RequestFile file) throws Exception {
        return request(url, headers, file, false);
    }

    //boolean formBody нужен для тех случаев, когда в хедерах есть строки с \n и т.д
    @Override
    public String request(String url, Map<String, String> headers, RequestFile file, boolean formBody) throws Exception {
        Log.d("FORPDA_LOG", "request url " + url);
        if (url.substring(0, 2).equals("//")) {
            url = "http:".concat(url);
            Log.d("FORPDA_LOG", "fixed request url " + url);
        }

        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .header("User-Agent", userAgent);
        if (headers != null || file != null) {
            if (formBody) {
                if (headers != null) {
                    //FormBody нужен, т.к не все формы корректно работают с MultipartBody (точнее только авторизация)
                    Log.d("FORPDA_LOG", "FORM BUILDER");
                    FormBody.Builder formBuilder = new FormBody.Builder();
                    for (Map.Entry<String, String> entry : headers.entrySet()) {
                        Log.d("FORPDA_LOG", "HEADER " + entry.getKey() + " : " + entry.getValue());
                        formBuilder.addEncoded(entry.getKey(), entry.getValue());
                    }
                    requestBuilder.post(formBuilder.build());
                }
            } else {
                MultipartBody.Builder multipartBuilder = new MultipartBody.Builder();
                multipartBuilder.setType(MultipartBody.FORM);
                Log.d("FORPDA_LOG", "MULTIPART FORM BUILDER");
                if (headers != null) {
                    for (Map.Entry<String, String> entry : headers.entrySet()) {
                        Log.d("FORPDA_LOG", "HEADER " + entry.getKey() + " : " + entry.getValue());
                        multipartBuilder.addFormDataPart(entry.getKey(), entry.getValue());
                    }
                }
                if (file != null) {
                    Log.e("FORPDA_LOG", "FILE " + file.getRequestName());
                    Log.e("FORPDA_LOG", "FILE " + file.getFileName());
                    Log.e("FORPDA_LOG", "FILE " + file.getMimeType());
                    Log.e("FORPDA_LOG", "FILE " + file.getFileStream());
                    multipartBuilder.addFormDataPart(file.getRequestName(), URLEncoder.encode(file.getFileName(), "CP1251"), RequestBodyUtil.create(MediaType.parse(file.getMimeType()), file.getFileStream()));
                }
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
            //Log.d("FORPDA_LOG", "redirected url " + response.request().url().toString());
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
            int lastCounts[] = {ClientHelper.getMentionsCount(), ClientHelper.getFavoritesCount(), ClientHelper.getQmsCount()};
            ClientHelper.setMentionsCount(tempGroup == null ? 0 : Integer.parseInt(tempGroup));

            tempGroup = countsMatcher.group(2);
            ClientHelper.setFavoritesCount(tempGroup == null ? 0 : Integer.parseInt(tempGroup));

            tempGroup = countsMatcher.group(3);
            ClientHelper.setQmsCount(tempGroup == null ? 0 : Integer.parseInt(tempGroup));

            if (lastCounts[0] != ClientHelper.getMentionsCount()||lastCounts[1] != ClientHelper.getFavoritesCount()||lastCounts[2] != ClientHelper.getQmsCount()) {
                observerHandler.post(() -> ClientHelper.getInstance().notifyCountsChanged());
            }
        }
    }


    public String getRedirect(String url) {
        return redirects.get(url);
    }

    public void clearCookies() {
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
