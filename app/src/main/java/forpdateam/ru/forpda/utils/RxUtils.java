package forpdateam.ru.forpda.utils;

import io.reactivex.disposables.Disposable;

/**
 * Created by isanechek on 05.08.16.
 */

public class RxUtils {
    public static void unsubscribe(Disposable subscription) {
        if (subscription != null && !subscription.isDisposed()) {
            subscription.dispose();
        }
    }
}
