package forpdateam.ru.forpda.client;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;
import android.webkit.WebSettings;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
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
import forpdateam.ru.forpda.api.NetworkRequest;
import forpdateam.ru.forpda.api.NetworkResponse;
import forpdateam.ru.forpda.api.Utils;
import forpdateam.ru.forpda.utils.Html;
import forpdateam.ru.forpda.utils.SimpleObservable;
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
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;
import okio.Sink;

public class Client implements IWebClient {
    private final static String userAgent = WebSettings.getDefaultUserAgent(App.getContext());
    private final static Pattern countsPattern = Pattern.compile("<a href=\"(?:https?)?\\/\\/4pda\\.ru\\/forum\\/index\\.php\\?act=mentions[^>]*?><i[^>]*?>(\\d+)<\\/i>[\\s\\S]*?act=fav[^>]*?><i[^>]*?>(\\d+)<\\/i>[\\s\\S]*?act=qms[^>]*?data-count=\"(\\d+)\">");
    private final static Pattern errorPattern = Pattern.compile("^[\\s\\S]*?wr va-m text\">([\\s\\S]*?)</div></div></div></div><div class=\"footer\">");
    private static Client INSTANCE = null;
    private static Map<String, Cookie> cookies;
    private static List<Cookie> listCookies;
    private SimpleObservable networkObservables = new SimpleObservable();
    private Handler observerHandler = new Handler(Looper.getMainLooper());
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

    public Map<String, Cookie> getCookies() {
        return cookies;
    }

    public List<Cookie> getListCookies() {
        return listCookies;
    }

    private final CookieJar cookieJar = new CookieJar() {
        Pattern authPattern = Pattern.compile("4pda\\.ru\\/forum\\/[\\s\\S]*?(?:act=(?:auth|logout)|#afterauth)");
        Matcher matcher;

        @Override
        public void saveFromResponse(@NonNull HttpUrl url, @NonNull List<Cookie> cookies) {
            if (matcher == null)
                matcher = authPattern.matcher(url.toString());
            else
                matcher = matcher.reset(url.toString());

            if (matcher.find()) {
                for (Cookie cookie : cookies) {
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
            } else {
                for (Cookie cookie : cookies) {
                    if (cookie.value().equals("deleted")) {
                        Client.cookies.remove(cookie.name());
                    } else {
                        if (!Client.cookies.containsKey(cookie.name())) {
                            Client.cookies.remove(cookie.name());
                        }
                        Client.cookies.put(cookie.name(), cookie);
                    }
                }
            }
        }

        @Override
        public List<Cookie> loadForRequest(@NonNull HttpUrl url) {
            return new ArrayList<>(Client.cookies.values());
        }
    };

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            //.addInterceptor(new ChuckInterceptor(App.getContext()))
            .cookieJar(cookieJar)
            .build();

    private final OkHttpClient webSocketClient = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            //.addInterceptor(new ChuckInterceptor(App.getContext()))
            .cookieJar(cookieJar)
            .build();

    //Network
    @Override
    public NetworkResponse get(String url) throws Exception {
        return request(new NetworkRequest.Builder().url(url).build());
    }

    public class ProgressRequestBody extends RequestBody {

        protected RequestBody mDelegate;
        protected ProgressListener mListener;
        protected CountingSink mCountingSink;

        public ProgressRequestBody(RequestBody delegate, ProgressListener listener) {
            mDelegate = delegate;
            mListener = listener;
        }

        @Override
        public MediaType contentType() {
            return mDelegate.contentType();
        }

        @Override
        public long contentLength() {
            try {
                return mDelegate.contentLength();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return -1;
        }

        @Override
        public void writeTo(@NonNull BufferedSink sink) throws IOException {
            mCountingSink = new CountingSink(sink);
            BufferedSink bufferedSink = Okio.buffer(mCountingSink);
            mDelegate.writeTo(bufferedSink);
            bufferedSink.flush();
        }

        protected final class CountingSink extends ForwardingSink {
            private long bytesWritten = 0;

            public CountingSink(Sink delegate) {
                super(delegate);
            }

            @Override
            public void write(@NonNull Buffer source, long byteCount) throws IOException {
                super.write(source, byteCount);
                bytesWritten += byteCount;
                mListener.onProgress((int) (100F * bytesWritten / contentLength()));
            }
        }
    }

    private Request.Builder prepareRequest(NetworkRequest request, ProgressListener uploadProgressListener) {
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
                    FormBody formBody = formBuilder.build();
                    Log.d("FORPDA_LOG", "FORM BODY CONTENTTYPE " + formBody.contentType());
                    requestBuilder.post(formBody);
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
                    multipartBuilder.addFormDataPart(request.getFile().getRequestName(), request.getFile().getFileName(), RequestBodyUtil.create(MediaType.parse(request.getFile().getMimeType()), request.getFile().getFileStream()));
                }
                MultipartBody multipartBody = multipartBuilder.build();
                Log.d("FORPDA_LOG", "MULTIPART CONTENTTYPE " + multipartBody.contentType());
                for (MultipartBody.Part part : multipartBody.parts()) {
                    Log.e("FORPDA_LOG", "PART" + part.headers().toString());
                }
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
            if (!okHttpResponse.isSuccessful())
                throw new OkHttpResponseException(okHttpResponse.code(), okHttpResponse.message(), request.getUrl());

            response.setCode(okHttpResponse.code());
            response.setMessage(okHttpResponse.message());
            response.setRedirect(okHttpResponse.request().url().toString());

            if (!request.isWithoutBody()) {
                response.setBody(okHttpResponse.body().string());
                getCounts(response.getBody());
                checkForumErrors(response.getBody());
            }

            Log.d("SUKA", "" + request.isWithoutBody() + " : " + response.toString());
        } finally {
            if (okHttpResponse != null)
                okHttpResponse.close();
        }
        return response;
    }

    public InputStream loadImage(String url) throws Exception {
        Request.Builder requestBuilder = prepareRequest(new NetworkRequest.Builder().url(url).build(), null);
        Response okHttpResponse = null;
        InputStream is = null;
        okHttpResponse = client.newCall(requestBuilder.build()).execute();
        if (!okHttpResponse.isSuccessful())
            throw new OkHttpResponseException(okHttpResponse.code(), okHttpResponse.message(), url);
        is = okHttpResponse.body().byteStream();
        return is;
    }

    @Override
    public NetworkResponse request(NetworkRequest request) throws Exception {
        return request(request, this.client, null);
    }

    @Override
    public NetworkResponse request(NetworkRequest request, ProgressListener uploadProgressListener) throws Exception {
        return request(request, this.client, uploadProgressListener);
    }

    public WebSocket createWebSocketConnection(WebSocketListener webSocketListener) {

        Request request = new Request.Builder()
                .url("ws://app.4pda.ru/ws/")
                .build();
        Log.d("WS_SUKA", "HEADER " + request.headers().toString());

        return webSocketClient.newWebSocket(request, webSocketListener);

        // Trigger shutdown of the dispatcher's executor so this process can exit cleanly.
        //client.dispatcher().executorService().shutdown();
    }

    private void checkForumErrors(String res) throws Exception {
        Matcher errorMatcher = errorPattern.matcher(res);
        if (errorMatcher.find()) {
            throw new OnlyShowException(Utils.fromHtml(errorMatcher.group(1)));
        }
    }


    private void getCounts(String res) {
        Matcher countsMatcher = countsPattern.matcher(res);

        if (countsMatcher.find()) {
            try {
                tempGroup = countsMatcher.group(1);
                ClientHelper.setMentionsCount(tempGroup == null ? 0 : Integer.parseInt(tempGroup));

                tempGroup = countsMatcher.group(2);
                ClientHelper.setFavoritesCount(tempGroup == null ? 0 : Integer.parseInt(tempGroup));

                tempGroup = countsMatcher.group(3);
                ClientHelper.setQmsCount(tempGroup == null ? 0 : Integer.parseInt(tempGroup));
            } catch (Exception exception) {
                Log.d("WATAFUCK", res);
            }
            observerHandler.post(() -> ClientHelper.getInstance().notifyCountsChanged());
        }
    }

    public void clearCookies() {
        cookies.clear();
        listCookies.clear();
    }

    public void removeNetworkObserver(Observer observer) {
        networkObservables.deleteObserver(observer);
    }

    public void addNetworkObserver(Observer observer) {
        networkObservables.addObserver(observer);
    }

    public void notifyNetworkObservers(Boolean b) {
        networkObservables.notifyObservers(b);
    }

    public boolean getNetworkState() {
        ConnectivityManager cm =
                (ConnectivityManager) App.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }
}
