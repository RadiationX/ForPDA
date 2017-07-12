package forpdateam.ru.forpda.fragments.forum;

import android.support.annotation.NonNull;

import forpdateam.ru.forpda.rxapi.RxApi;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by radiationx on 07.07.17.
 */

public class ForumHelper {

    public static void markAllRead(@NonNull Consumer<Object> onNext) {
        RxApi.Forum().markAllRead().onErrorReturn(throwable -> false)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onNext);
    }
}
