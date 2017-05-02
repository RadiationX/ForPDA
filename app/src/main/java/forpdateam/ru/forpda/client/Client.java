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
        String session_id = App.getInstance().getPreferences().getString("cookie_session_id", null);
        String anonymous = App.getInstance().getPreferences().getString("cookie_anonymous", null);
        ClientHelper.setUserId(App.getInstance().getPreferences().getString("member_id", null));
        Log.d("FORPDA_LOG", "INIT AUTH DATA " + member_id + " : " + pass_hash + " : " + session_id + " : " + App.getInstance().getPreferences().getString("member_id", null));
        if (member_id != null && pass_hash != null) {
            ClientHelper.setAuthState(ClientHelper.AUTH_STATE_LOGIN);
            //Первичная загрузка кукисов
            cookies.put("member_id", parseCookie(member_id));
            cookies.put("pass_hash", parseCookie(pass_hash));
            if (session_id != null)
                cookies.put("session_id", parseCookie(session_id));
            if (anonymous != null) {
                cookies.put("anonymous", parseCookie(anonymous));
            }
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
                Pattern authPattern = Pattern.compile("4pda\\.ru\\/forum\\/[\\s\\S]*?(?:act=(?:auth|logout)|#afterauth)");
                Matcher matcher;

                @Override
                public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                    Log.d("FORPDA_LOG", "response url " + url.toString());
                    Log.d("FORPDA_LOG", "response cookies size " + cookies.size());
                    for (Cookie cookie : cookies) {
                        Log.e("FORPDA_LOG", "Cookie: " + cookie.name() + " : " + cookie.value());
                    }

                    if (matcher == null)
                        matcher = authPattern.matcher(url.toString());
                    else
                        matcher = matcher.reset(url.toString());

                    if (matcher.find()) {
                        for (Cookie cookie : cookies) {
                            Log.e("FORPDA_LOG", "AUTH response " + cookie.name() + " : " + cookie.value());
                            boolean isMemberId = cookie.name().equals("member_id");
                            boolean isPassHash = cookie.name().equals("pass_hash");
                            boolean isSessionId = cookie.name().equals("session_id");
                            boolean isAnonymous = cookie.name().equals("anonymous");
                            if (isMemberId || isPassHash || isSessionId || isAnonymous) {
                                if (cookie.value().equals("deleted")) {
                                    App.getInstance().getPreferences().edit().remove("cookie_".concat(cookie.name())).apply();
                                    if (Client.cookies.containsKey(cookie.name())) {
                                        Client.cookies.remove(cookie.name());
                                    }
                                } else {
                                    //Сохранение кукисов cookie_member_id и cookie_pass_hash
                                    App.getInstance().getPreferences().edit().putString("cookie_".concat(cookie.name()), cookieToPref(url.toString(), cookie)).apply();
                                    if (isMemberId) {
                                        App.getInstance().getPreferences().edit().putString("member_id", cookie.value()).apply();
                                        ClientHelper.setUserId(cookie.value());
                                    }
                                    if (isPassHash) {
                                        //App.getInstance().getPreferences().edit().putString("cookie_pass_hash", cookieToPref(url.toString(), cookie)).apply();
                                        //App.getInstance().getPreferences().edit().putString("auth_key", cookie.value()).apply();
                                    }
                                    if (isSessionId) {
                                        //App.getInstance().getPreferences().edit().putString("cookie_session_id", cookieToPref(url.toString(), cookie)).apply();
                                    }
                                    if (isAnonymous) {
                                        //App.getInstance().getPreferences().edit().putString("cookie_anonymous", cookieToPref(url.toString(), cookie)).apply();
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
                    for (int i = 0; i < listCookies.size(); i++) {
                        Log.e("FORPDA_LOG", "cookie request: " + listCookies.get(i).name() + " : " + listCookies.get(i).value());
                    }
                    return listCookies;
                }
            })
            .build();


    //Network
    @Override
    public String get(String url) throws Exception {
        return request(new ForPdaRequest.Builder().url(url).build());
    }

    @Override
    public String getXhr(String url) throws Exception {
        return request(new ForPdaRequest.Builder().url(url).addHeader("X-Requested-With", "XMLHttpRequest").build());
    }

    @Override
    public String request(ForPdaRequest request) throws Exception {
        Log.d("FORPDA_LOG", "request url " + request.getUrl());
        String url = request.getUrl();
        if (request.getUrl().substring(0, 2).equals("//")) {
            url = "http:".concat(request.getUrl());
            Log.d("FORPDA_LOG", "fixed request url " + request.getUrl());
        }

        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .header("User-Agent", userAgent);
        if (request.getHeaders() != null) {
            for (Map.Entry<String, String> entry : request.getHeaders().entrySet()) {
                Log.d("FORPDA_LOG", "HEADER " + entry.getKey() + " : " + entry.getValue());
                requestBuilder.header(entry.getKey(), entry.getValue());
            }
        }
        if (request.getFormHeaders() != null || request.getFile() != null) {
            if (!request.isMultipartForm()) {
                if (request.getFormHeaders() != null) {
                    //FormBody нужен, т.к не все формы корректно работают с MultipartBody (точнее только авторизация)
                    Log.d("FORPDA_LOG", "FORM BUILDER");
                    FormBody.Builder formBuilder = new FormBody.Builder();
                    for (Map.Entry<String, String> entry : request.getFormHeaders().entrySet()) {
                        Log.d("FORPDA_LOG", "FORM HEADER " + entry.getKey() + " : " + entry.getValue());
                        //formBuilder.addEncoded(entry.getKey(), entry.getValue());
                        formBuilder.add(entry.getKey(), entry.getValue());
                        if (request.getEncodedFormHeaders() != null && request.getEncodedFormHeaders().contains(entry.getKey())) {
                            formBuilder.addEncoded(entry.getKey(), entry.getValue());
                        } else {
                            formBuilder.add(entry.getKey(), entry.getValue());
                        }
                    }
                    requestBuilder.post(formBuilder.build());
                }
            } else {
                MultipartBody.Builder multipartBuilder = new MultipartBody.Builder();
                multipartBuilder.setType(MultipartBody.FORM);
                Log.d("FORPDA_LOG", "MULTIPART FORM BUILDER");
                if (request.getFormHeaders() != null) {
                    for (Map.Entry<String, String> entry : request.getFormHeaders().entrySet()) {
                        Log.d("FORPDA_LOG", "FORM HEADER " + entry.getKey() + " : " + entry.getValue());
                        //multipartBuilder.addFormDataPart(entry.getKey(), URLEncoder.encode(entry.getValue(), "UTF-8"));
                        multipartBuilder.addFormDataPart(entry.getKey(), entry.getValue());
                    }
                }
                if (request.getFile() != null) {
                    Log.e("FORPDA_LOG", "FILE " + request.getFile().getRequestName());
                    Log.e("FORPDA_LOG", "FILE " + request.getFile().getFileName());
                    Log.e("FORPDA_LOG", "FILE " + request.getFile().getMimeType());
                    Log.e("FORPDA_LOG", "FILE " + request.getFile().getFileStream());
                    multipartBuilder.addFormDataPart(request.getFile().getRequestName(), URLEncoder.encode(request.getFile().getFileName(), "UTF-8"), RequestBodyUtil.create(MediaType.parse(request.getFile().getMimeType()), request.getFile().getFileStream()));
                }
                MultipartBody multipartBody = multipartBuilder.build();
                for (MultipartBody.Part part : multipartBody.parts()) {
                    Log.e("FORPDA_LOG", "PART" + part.headers().toString());
                }
                requestBuilder.post(multipartBody);
            }
        }

        String res;
        Response response = null;
        try {
            response = client.newCall(requestBuilder.build()).execute();
            if (!response.isSuccessful())
                throw new OkHttpResponseException(response.code(), response.message(), request.getUrl());
            res = response.body().string();
            getCounts(res);
            checkForumErrors(res);
            //Log.d("FORPDA_LOG", "redirected url " + response.request().url().toString());
            redirects.put(request.getUrl(), response.request().url().toString());
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
            //int lastCounts[] = {ClientHelper.getMentionsCount(), ClientHelper.getFavoritesCount(), ClientHelper.getQmsCount()};
            ClientHelper.setMentionsCount(tempGroup == null ? 0 : Integer.parseInt(tempGroup));

            tempGroup = countsMatcher.group(2);
            ClientHelper.setFavoritesCount(tempGroup == null ? 0 : Integer.parseInt(tempGroup));

            tempGroup = countsMatcher.group(3);
            ClientHelper.setQmsCount(tempGroup == null ? 0 : Integer.parseInt(tempGroup));

            /*if (lastCounts[0] != ClientHelper.getMentionsCount()||lastCounts[1] != ClientHelper.getFavoritesCount()||lastCounts[2] != ClientHelper.getQmsCount()) {
            }*/
            observerHandler.post(() -> ClientHelper.getInstance().notifyCountsChanged());
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
