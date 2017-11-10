package forpdateam.ru.forpda.common.rx;

import android.support.annotation.NonNull;
import android.view.View;

import forpdateam.ru.forpda.common.ErrorHandler;
import forpdateam.ru.forpda.ui.fragments.TabFragment;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by radiationx on 16.03.17.
 */
public class Subscriber<T> extends RxSchedulers{
    private TabFragment fragment;

    public Subscriber() {
    }

    public Subscriber(TabFragment tabFragment) {
        fragment = tabFragment;
    }

    private void handleErrorRx(Throwable throwable) {
        handleErrorRx(throwable, null);
    }

    private void handleErrorRx(Throwable throwable, View.OnClickListener listener) {
        ErrorHandler.handle(fragment, throwable, listener);
    }

    public Disposable subscribe(@NonNull Observable<T> observable, @NonNull Consumer<T> onNext, @NonNull T onErrorReturn) {
        return subscribe(observable, onNext, onErrorReturn, null);
    }

    public Disposable subscribe(@NonNull Observable<T> observable, @NonNull Consumer<T> onNext, @NonNull T onErrorReturn, View.OnClickListener onErrorAction) {
        Disposable disposable = observable.onErrorReturn(throwable -> {
            handleErrorRx(throwable, onErrorAction);
            return onErrorReturn;
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onNext, throwable -> handleErrorRx(throwable, onErrorAction));
        if (fragment != null) {
            fragment.getDisposable().add(disposable);
        }

        return disposable;
    }
}
