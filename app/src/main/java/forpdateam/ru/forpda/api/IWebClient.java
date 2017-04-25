package forpdateam.ru.forpda.api;

import java.util.Map;

/**
 * Created by radiationx on 26.03.17.
 */

public interface IWebClient {
    String MINIMAL_PAGE = "http://4pda.ru/forum/index.php?showforum=200#afterauth";

    String get(String url) throws Exception;

    String get(String url, Map<String, String> headers) throws Exception;

    String post(String url, Map<String, String> headers, Map<String, String> formHeaders) throws Exception;

    String post(String url, Map<String, String> headers, Map<String, String> formHeaders, boolean formBody) throws Exception;

    String post(String url, Map<String, String> headers, Map<String, String> formHeaders, RequestFile file) throws Exception;

    String post(String url, Map<String, String> formHeaders) throws Exception;

    String post(String url, Map<String, String> formHeaders, boolean formBody) throws Exception;

    String post(String url, Map<String, String> formHeaders, RequestFile file) throws Exception;

    String request(String url, Map<String, String> headers, Map<String, String> formHeaders, RequestFile file, boolean formBody) throws Exception;

    String getRedirect(String url);

    String getAuthKey();

    void clearCookies();
}
