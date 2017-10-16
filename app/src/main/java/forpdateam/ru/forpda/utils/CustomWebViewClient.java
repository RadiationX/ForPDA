package forpdateam.ru.forpda.utils;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.util.Base64;
import android.util.Log;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import forpdateam.ru.forpda.api.others.user.ForumUser;
import forpdateam.ru.forpda.rxapi.ForumUsersCache;

/**
 * Created by radiationx on 12.09.17.
 */

public class CustomWebViewClient extends WebViewClient {
    private final static String LOG_TAG = CustomWebViewClient.class.getSimpleName();
    private final static String TYPE_NICK = "nick";
    private final static String TYPE_URL = "url";

    private Pattern cachePattern = Pattern.compile("app_cache:avatars\\?(url|nick)=([\\s\\S]*)");

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
        return super.shouldInterceptRequest(view, request);
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
        Matcher matcher = cachePattern.matcher(url);
        if (matcher.find()) {
            try {
                Log.d(LOG_TAG, "intercepted " + url);
                WebResourceResponse resourceResponse = null;
                String type = matcher.group(1);
                String value = matcher.group(2);
                value = URLDecoder.decode(value, "UTF-8");

                String avatarUrl = null;
                switch (type) {
                    case TYPE_NICK:
                        ForumUser forumUser = ForumUsersCache.loadUserByNick(value);
                        Log.d(LOG_TAG, "Loaded user " + forumUser.getId() + " : " + forumUser.getNick() + " : " + forumUser.getAvatar());
                        avatarUrl = forumUser.getAvatar();
                        break;
                    case TYPE_URL:
                        avatarUrl = value;
                        break;
                }

                Bitmap bitmap = ImageLoader.getInstance().loadImageSync(avatarUrl);
                String base64Bitmap = convert(bitmap);
                base64Bitmap = "data:image/png;base64," + base64Bitmap;
                resourceResponse = new WebResourceResponse(
                        "text/text",
                        null,
                        new ByteArrayInputStream(base64Bitmap.getBytes()));
                return resourceResponse;
            } catch (Exception e) {
                e.printStackTrace();
                super.shouldInterceptRequest(view, url);
            }
        }
        return super.shouldInterceptRequest(view, url);
    }

    public Bitmap convert(String base64Str) throws IllegalArgumentException {
        byte[] decodedBytes = Base64.decode(
                base64Str.substring(base64Str.indexOf(",") + 1),
                Base64.DEFAULT
        );

        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }

    public String convert(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);

        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP);
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        return handleUri(Uri.parse(url));
    }

    @TargetApi(Build.VERSION_CODES.N)
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        return handleUri(request.getUrl());
    }

    private boolean handleUri(Uri uri) {
        IntentHandler.handle(uri.toString());
        return true;
    }
}
