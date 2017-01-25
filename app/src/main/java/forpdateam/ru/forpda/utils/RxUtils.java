package forpdateam.ru.forpda.utils;

import io.reactivex.ObservableTransformer;
import io.reactivex.SingleTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by isanechek on 05.08.16.
 */

public class RxUtils {
    public static void unsubscribe(Disposable subscription) {
        if (subscription != null && !subscription.isDisposed()) {
            subscription.dispose();
        }
    }

    public static final ObservableTransformer IO_TRANSFORMER = upstream -> upstream
            .subscribeOn(Schedulers.io())
            .unsubscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());

    public static <T> ObservableTransformer<T, T> applySchedulers(ObservableTransformer transformer) {
        return transformer;
    }

    public static final SingleTransformer IO_SINGLE_TRANSFORMER = upstream -> upstream
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());

    public static <T>SingleTransformer<T, T> applySingleSchedulers(SingleTransformer transformer) {
        return transformer;
    }


}
