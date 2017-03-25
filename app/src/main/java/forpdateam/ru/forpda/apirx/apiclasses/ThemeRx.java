package forpdateam.ru.forpda.apirx.apiclasses;

import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.api.theme.models.ThemePage;
import io.reactivex.Observable;

/**
 * Created by radiationx on 25.03.17.
 */

public class ThemeRx {
    public Observable<ThemePage> getTheme(final String url) {
        return getTheme(url, false);
    }

    public Observable<ThemePage> getTheme(final String url, boolean generateHtml) {
        return Observable.fromCallable(() -> Api.Theme().getTheme(url, generateHtml));
    }

    public Observable<String> reportPost(int themeId, int postId, String message) {
        return Observable.fromCallable(() -> Api.Theme().reportPost(themeId, postId, message));
    }

    public Observable<String> deletePost(int postId) {
        return Observable.fromCallable(() -> Api.Theme().deletePost(postId));
    }

    public Observable<String> votePost(int postId, boolean type) {
        return Observable.fromCallable(() -> Api.Theme().votePost(postId, type));
    }
}
