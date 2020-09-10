package forpdateam.ru.forpda.client;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import android.util.Log;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;

import javax.net.ssl.SSLContext;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.entity.common.AuthData;
import forpdateam.ru.forpda.entity.common.AuthState;
import forpdateam.ru.forpda.entity.common.MessageCounters;
import forpdateam.ru.forpda.model.AuthHolder;
import forpdateam.ru.forpda.model.CountersHolder;
import forpdateam.ru.forpda.model.data.remote.IWebClient;
import forpdateam.ru.forpda.model.data.remote.api.ApiUtils;
import forpdateam.ru.forpda.model.data.remote.api.NetworkRequest;
import forpdateam.ru.forpda.model.data.remote.api.NetworkResponse;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class Client implements IWebClient {
    private final static String LOG_TAG = Client.class.getSimpleName();
    private static final String USER_AGENT = "Mozilla/5.0 (Linux; Android 4.4; Nexus 5 Build/_BuildID_) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/30.0.0.0 Mobile Safari/537.36";
    private Map<String, Cookie> clientCookies = new HashMap<>();
    private Handler observerHandler = new Handler(Looper.getMainLooper());
    private List<String> privateHeaders = new ArrayList<>(Arrays.asList("pass_hash", "session_id", "auth_key", "password"));
    private final Cookie mobileCookie = Cookie.parse(HttpUrl.parse("https://4pda.ru/"), "ngx_mb=1;");
    private AuthHolder authHolder;
    private CountersHolder countersHolder;

    //Контекст нужен, для чтения настроек
    //Не необходимо, но вдруг случится шо у App не будет контекста
    public Client(Context context, AuthHolder authHolder, CountersHolder countersHolder) {
        this.authHolder = authHolder;
        this.countersHolder = countersHolder;
        AuthData authData = authHolder.get();
        SharedPreferences preferences = App.getPreferences(context);
        String member_id = preferences.getString("cookie_member_id", null);
        String pass_hash = preferences.getString("cookie_pass_hash", null);
        String session_id = preferences.getString("cookie_session_id", null);
        String anonymous = preferences.getString("cookie_anonymous", null);
        String clearance = preferences.getString("cookie_cf_clearance", null);

        clientCookies.put("ngx_mb", mobileCookie);
        if (clearance != null) {
            clientCookies.put("cf_clearance", parseCookie(clearance));
        }

        if (member_id != null && pass_hash != null) {
            int userId = Integer.parseInt(preferences.getString("member_id", "0"));
            authData.setState(AuthState.AUTH);
            authData.setUserId(userId);

            //Первичная загрузка кукисов
            clientCookies.put("member_id", parseCookie(member_id));
            clientCookies.put("pass_hash", parseCookie(pass_hash));
            if (session_id != null)
                clientCookies.put("session_id", parseCookie(session_id));
            if (anonymous != null) {
                clientCookies.put("anonymous", parseCookie(anonymous));
            }
        } else {
            authData.setState(AuthState.SKIP);
            authData.setUserId(0);
        }
        authHolder.set(authData);
    }

    public String getAuthKey() {
        return App.get().getPreferences().getString("auth_key", "0");
    }

    private Cookie parseCookie(String cookieFields) {
        /*Хранение: Url|:|Cookie*/
        String[] fields = cookieFields.split("\\|:\\|");
        return Cookie.parse(HttpUrl.parse(fields[0]), fields[1]);
    }

    private String cookieToPref(String url, Cookie cookie) {
        return url.concat("|:|").concat(cookie.toString());
    }

    public Map<String, Cookie> getClientCookies() {
        return clientCookies;
    }

    private final CookieJar cookieJar = new CookieJar() {

        @Override
        public void saveFromResponse(@NonNull HttpUrl url, @NonNull List<Cookie> cookies) {
            SharedPreferences.Editor editor = App.get().getPreferences().edit();
            /*for (Cookie cookie : cookies) {
                Log.e("SUKA", "save COOK " + cookie.name() + " : " + cookie.value());
            }*/
            for (Cookie cookie : cookies) {
                if (cookie.value().equals("deleted")) {
                    editor.remove("cookie_".concat(cookie.name()));
                    clientCookies.remove(cookie.name());
                } else {
                    editor.putString("cookie_".concat(cookie.name()), cookieToPref(url.toString(), cookie));
                    if (cookie.name().equals("member_id")) {
                        editor.putString("member_id", cookie.value());
                        int userId = Integer.parseInt(cookie.value());
                        AuthData authData = authHolder.get();
                        authData.setUserId(userId);
                        authData.setState(userId == AuthData.NO_ID ? AuthState.NO_AUTH : AuthState.AUTH);
                        authHolder.set(authData);
                    }
                    if (!clientCookies.containsKey(cookie.name())) {
                        clientCookies.remove(cookie.name());
                    }
                    clientCookies.put(cookie.name(), cookie);
                }
            }
            editor.apply();
        }

        @Override
        public List<Cookie> loadForRequest(@NonNull HttpUrl url) {
            boolean external = !url.host().toLowerCase().contains("4pda");
            if (!external) {
                clientCookies.put("ngx_mb", mobileCookie);
            }

            List<Cookie> cookies = new ArrayList<>(clientCookies.values());
            if (external) {
                for (String privateName : privateHeaders) {
                    for (int i = 0; i < cookies.size(); i++) {
                        if (cookies.get(i).name().equals(privateName)) {
                            cookies.remove(i);
                            break;
                        }
                    }
                }
            }
            /*for (Cookie cookie : cookies) {
                Log.e("SUKA", "load COOK " + cookie.name() + " : " + cookie.value());
            }*/
            return cookies;
        }
    };

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(45, TimeUnit.SECONDS)
            .writeTimeout(45, TimeUnit.SECONDS)
            .readTimeout(45, TimeUnit.SECONDS)
            .sslSocketFactory(getNewSslContext().getSocketFactory())
            .cookieJar(cookieJar)
            .build();

    private final OkHttpClient webSocketClient = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .sslSocketFactory(getNewSslContext().getSocketFactory())
            .retryOnConnectionFailure(true)
            .cookieJar(cookieJar)
            .build();


    private SSLContext getNewSslContext() {
        SSLContext sslContext;
        try {
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, null, null);
        } catch (GeneralSecurityException e) {
            throw new AssertionError(); // The system has no TLS. Just give up.
        }
        return sslContext;
    }

    //Network
    @Override
    public NetworkResponse get(String url) throws Exception {
        return request(new NetworkRequest.Builder().url(url).build());
    }

    @Override
    public NetworkResponse request(NetworkRequest request) throws Exception {
        return request(request, this.client, null);
    }

    @Override
    public NetworkResponse request(NetworkRequest request, ProgressListener uploadProgressListener) throws Exception {
        return request(request, this.client, uploadProgressListener);
    }

    private Request.Builder prepareRequest(NetworkRequest request, ProgressListener uploadProgressListener) {
        String url = request.getUrl();
        if (request.getUrl().substring(0, 2).equals("//")) {
            url = "https:".concat(request.getUrl());
        }
        Log.d(LOG_TAG, "Request url " + request.getUrl());
        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .header("Accept-Language", "ru-RU,ru;q=0.8,en-US;q=0.6,en;q=0.4")
                .header("User-Agent", USER_AGENT);
        if (request.getHeaders() != null) {
            for (Map.Entry<String, String> entry : request.getHeaders().entrySet()) {
                Log.d(LOG_TAG, "Header " + entry.getKey() + " : " + (privateHeaders.contains(entry.getKey()) ? "private" : entry.getValue()));
                requestBuilder.header(entry.getKey(), entry.getValue());
            }
        }
        if (request.getFormHeaders() != null || request.getFile() != null) {
            Log.d(LOG_TAG, "Multipart " + request.isMultipartForm());
            if (request.getFormHeaders() != null) {
                for (Map.Entry<String, String> entry : request.getFormHeaders().entrySet()) {
                    Log.d(LOG_TAG, "Form header " + entry.getKey() + " : " + (privateHeaders.contains(entry.getKey()) ? "private" : entry.getValue()));
                }
            }
            if (request.getFile() != null) {
                Log.d(LOG_TAG, "Form file " + request.getFile().toString());
            }
            if (!request.isMultipartForm()) {
                if (request.getFormHeaders() != null) {
                    FormBody.Builder formBuilder = new FormBody.Builder();
                    for (Map.Entry<String, String> entry : request.getFormHeaders().entrySet()) {
                        formBuilder.add(entry.getKey(), entry.getValue());
                        if (request.getEncodedFormHeaders() != null && request.getEncodedFormHeaders().contains(entry.getKey())) {
                            formBuilder.addEncoded(entry.getKey(), entry.getValue());
                        } else {
                            formBuilder.add(entry.getKey(), entry.getValue());
                        }
                    }
                    FormBody formBody = formBuilder.build();
                    requestBuilder.post(formBody);
                }
            } else {
                MultipartBody.Builder multipartBuilder = new MultipartBody.Builder();
                multipartBuilder.setType(MultipartBody.FORM);
                if (request.getFormHeaders() != null) {
                    for (Map.Entry<String, String> entry : request.getFormHeaders().entrySet()) {
                        multipartBuilder.addFormDataPart(entry.getKey(), entry.getValue());
                    }
                }
                if (request.getFile() != null) {
                    MediaType type = MediaType.parse(request.getFile().getMimeType());
                    RequestBody requestBody = RequestBodyUtil
                            .create(type, request.getFile().getFileStream());
                    multipartBuilder.addFormDataPart(
                            request.getFile().getRequestName(),
                            request.getFile().getFileName(),
                            requestBody);
                }
                MultipartBody multipartBody = multipartBuilder.build();
                if (uploadProgressListener == null) {
                    requestBuilder.post(multipartBody);
                } else {
                    requestBuilder.post(new ProgressRequestBody(multipartBody, uploadProgressListener));
                }
            }
        }
        return requestBuilder;
    }

    public NetworkResponse request(NetworkRequest request, OkHttpClient client, ProgressListener uploadProgressListener) throws Exception {
        Request.Builder requestBuilder = prepareRequest(request, uploadProgressListener);
        NetworkResponse response = new NetworkResponse(request.getUrl());
        Response okHttpResponse = null;
        try {
            okHttpResponse = client.newCall(requestBuilder.build()).execute();
            if (!okHttpResponse.isSuccessful()) {
                if (okHttpResponse.code() == 403) {
                    String content = okHttpResponse.body().string();
                    //todo catch this is errorhandler
                    throw new GoogleCaptchaException(content);
                }
                throw new OkHttpResponseException(okHttpResponse.code(), okHttpResponse.message(), request.getUrl());
            }

            response.setCode(okHttpResponse.code());
            response.setMessage(okHttpResponse.message());
            response.setRedirect(okHttpResponse.request().url().toString());

            if (!request.isWithoutBody()) {
                response.setBody(okHttpResponse.body().string());
                getCounts(response.getBody());
                checkForumErrors(response.getBody());
            }

            Log.d(LOG_TAG, "Response: " + response.toString());
        } finally {
            if (okHttpResponse != null)
                okHttpResponse.close();
        }
        return response;
    }

    public WebSocket createWebSocketConnection(WebSocketListener webSocketListener) {
        Request request = new Request.Builder()
                .url("ws://app.4pda.ru/ws/")
                .build();
        return webSocketClient.newWebSocket(request, webSocketListener);
    }

    private void checkForumErrors(String res) throws Exception {
        Matcher errorMatcher = errorPattern.matcher(res);
        if (errorMatcher.find()) {
            throw new OnlyShowException(ApiUtils.fromHtml(errorMatcher.group(1)));
        }
    }

    private void getCounts(String res) {
        Matcher countsMatcher = countsPattern.matcher(res);

        if (countsMatcher.find()) {
            MessageCounters counters = countersHolder.get();
            try {
                String tempGroup;
                tempGroup = countsMatcher.group(1);
                counters.setMentions(tempGroup == null ? 0 : Integer.parseInt(tempGroup));

                tempGroup = countsMatcher.group(2);
                counters.setFavorites(tempGroup == null ? 0 : Integer.parseInt(tempGroup));

                tempGroup = countsMatcher.group(3);
                counters.setQms(tempGroup == null ? 0 : Integer.parseInt(tempGroup));
            } catch (Exception exception) {
                Log.d("WATAFUCK", res);
            }
            countersHolder.set(counters);
        }
    }

    public void clearCookies() {
        clientCookies.clear();
    }

}
