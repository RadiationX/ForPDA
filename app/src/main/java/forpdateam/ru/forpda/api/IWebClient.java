package forpdateam.ru.forpda.api;

/**
 * Created by radiationx on 26.03.17.
 */

public interface IWebClient {
    String MINIMAL_PAGE = "https://4pda.ru/forum/index.php?showforum=200#afterauth";

    NetworkResponse get(String url) throws Exception;

    NetworkResponse request(NetworkRequest request) throws Exception;

    NetworkResponse request(NetworkRequest request, ProgressListener progressListener) throws Exception;

    String getAuthKey();

    void clearCookies();

    interface ProgressListener {
        void onProgress(int percent);
    }
}
