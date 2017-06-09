package forpdateam.ru.forpda.fragments.theme;

import android.support.annotation.NonNull;

import forpdateam.ru.forpda.rxapi.RxApi;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by radiationx on 27.04.17.
 */

public class ThemeHelper {

    public static void reportPost(@NonNull Consumer<String> onNext, int themeId, int postId, String message) {
        RxApi.Theme().reportPost(themeId, postId, message).onErrorReturn(throwable -> "")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onNext);
    }

    public static void deletePost(@NonNull Consumer<Boolean> onNext, int postId) {
        RxApi.Theme().deletePost(postId).onErrorReturn(throwable -> false)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onNext);
    }

    public static void votePost(@NonNull Consumer<String> onNext, int postId, boolean type) {
        RxApi.Theme().votePost(postId, type).onErrorReturn(throwable -> "")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onNext);
    }

    public static void changeReputation(@NonNull Consumer<String> onNext, int postId, int userId, boolean type, String message) {
        RxApi.Reputation().editReputation(postId, userId, type, message).onErrorReturn(throwable -> "")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onNext);
    }
}
