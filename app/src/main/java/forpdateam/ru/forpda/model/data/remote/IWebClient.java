package forpdateam.ru.forpda.model.data.remote;

import java.util.Map;
import java.util.regex.Pattern;

import forpdateam.ru.forpda.model.data.remote.api.NetworkRequest;
import forpdateam.ru.forpda.model.data.remote.api.NetworkResponse;
import okhttp3.Cookie;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

/**
 * Created by radiationx on 26.03.17.
 */

public interface IWebClient {
    Pattern countsPattern = Pattern.compile("<a href=\"(?:https?)?\\/\\/4pda\\.(?:ru|to)\\/forum\\/index\\.php\\?act=mentions\" (?:data-count=\"(\\d+)\")?[^>]*?[\\s\\S]*?act=fav&amp;code=no\" (?:data-count=\"(\\d+)\")?[^>]*?[\\s\\S]*?span id=\"events-count\"[\\s\\S]*?(?:data-count=\"(\\d+)\")");
    Pattern errorPattern = Pattern.compile("^[\\s\\S]*?wr va-m text\">([\\s\\S]*?)</div></div></div></div><div class=\"footer\">");
    String MINIMAL_PAGE = "https://4pda.to/forum/index.php?showforum=200#afterauth";

    NetworkResponse get(String url) throws Exception;

    NetworkResponse request(NetworkRequest request) throws Exception;

    NetworkResponse request(NetworkRequest request, ProgressListener progressListener) throws Exception;

    String getAuthKey();

    Map<String, Cookie> getClientCookies();

    void clearCookies();

    WebSocket createWebSocketConnection(WebSocketListener webSocketListener);

    interface ProgressListener {
        void onProgress(int percent);
    }
}
