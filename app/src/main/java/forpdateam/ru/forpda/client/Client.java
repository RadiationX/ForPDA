package forpdateam.ru.forpda.client;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.webkit.WebSettings;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Observer;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.api.Api;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Client {
    private static final String userAgent = WebSettings.getDefaultUserAgent(App.getContext());
    private static final URI domain = URI.create("http://4pda.ru/");
    private static final CookieManager msCookieManager = new CookieManager();
    private static Client INSTANCE = new Client();
    public static final String minimalPage = "http://4pda.ru/forum/index.php?showforum=200";
    private static List<Cookie> cookies = new ArrayList<>();
    private NetworkObservable networkObserver = new NetworkObservable();

    public static String member_id;

    //Class
    public Client() {
        INSTANCE = this;
        msCookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        String member_id = App.getInstance().getPreferences().getString("cookie_member_id", null);
        String pass_hash = App.getInstance().getPreferences().getString("cookie_pass_hash", null);
        Client.member_id = App.getInstance().getPreferences().getString("member_id", null);
        if (member_id != null && pass_hash != null) {
            Api.Auth().setState(true);
            msCookieManager.getCookieStore().add(domain, parseCookie(member_id));
            msCookieManager.getCookieStore().add(domain, parseCookie(pass_hash));
        }
    }

    public static Client getInstance() {
        return INSTANCE;
    }

    private HttpCookie parseCookie(String cookie) {
        String[] fields = cookie.split("\\|:\\|");
        HttpCookie httpCookie = new HttpCookie(fields[0], fields[1]);
        httpCookie.setDomain(fields[2]);
        httpCookie.setPath(fields[3]);
        return httpCookie;
    }

    private static final OkHttpClient client = new OkHttpClient.Builder()
            .cookieJar(new CookieJar() {
                @Override
                public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                    Log.d("kek", "response cookies size " + cookies.size());
                    try {
                        msCookieManager.getCookieStore().removeAll();

                        for (Cookie cookie : cookies) {
                            if (cookie.name().matches("member_id|pass_hash")) {
                                String toSave = cookie.name() + "|:|" + cookie.value() + "|:|" + cookie.domain() + "|:|" + cookie.path() + "|:|";
                                App.getInstance().getPreferences().edit().putString("cookie_" + cookie.name(), toSave).apply();
                                if(cookie.name().equals("member_id")){
                                    App.getInstance().getPreferences().edit().putString("member_id", cookie.value()).apply();
                                    Client.member_id = cookie.value();
                                }
                                HttpCookie tempCookie = new HttpCookie(cookie.name(), cookie.value());
                                tempCookie.setDomain(cookie.domain());
                                tempCookie.setPath(cookie.path());
                                if (!msCookieManager.getCookieStore().getCookies().contains(tempCookie))
                                    msCookieManager.getCookieStore().add(domain, tempCookie);
                            }
                            Client.cookies.add(cookie);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public List<Cookie> loadForRequest(HttpUrl url) {
                    try {
                        for (HttpCookie cookie : msCookieManager.getCookieStore().getCookies()) {
                            Cookie tempCookie = new Cookie.Builder()
                                    .name(cookie.getName())
                                    .value(cookie.getValue())
                                    .domain(cookie.getDomain())
                                    .build();
                            if (!cookies.contains(tempCookie))
                                cookies.add(tempCookie);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Log.d("kek", "cookies size " + cookies.size());
                    return cookies;
                }
            })
            .build();


    //Network
    public String get(String url) throws Exception {
        return request(url, null);
    }

    public String post(String url, Map<String, String> headers) throws Exception {
        return request(url, headers);
    }

    private String request(String url, Map<String, String> headers) throws Exception {
        Log.d("kek", "request url " + url);

        Request.Builder builder = new Request.Builder()
                .url(url)
                .header("User-Agent", userAgent);
        if (headers != null) {
            FormBody.Builder formBodyBuilder = new FormBody.Builder();
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                formBodyBuilder.add(entry.getKey(), entry.getValue());
            }
            builder.post(formBodyBuilder.build());
        }

        String res;
        Response response = client.newCall(builder.build()).execute();
        try {
            if (!response.isSuccessful())
                throw new OkHttpResponseException(response.code(), response.message(), url);
            res = response.body().string();
        } finally {
            response.close();
        }

        return res;
    }

    public static List<Cookie> getCookies() {
        return cookies;
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
