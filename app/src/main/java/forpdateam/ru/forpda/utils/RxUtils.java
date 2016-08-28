package forpdateam.ru.forpda.utils;

import rx.Subscription;

/**
 * Created by isanechek on 05.08.16.
 */

public class RxUtils {
    public static void unsubscribe(Subscription subscription) {
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
    }
}
