package forpdateam.ru.forpda.common.mvp;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.view.View;

import org.acra.ACRA;

import forpdateam.ru.forpda.common.ErrorHandler;
import forpdateam.ru.forpda.ui.TabManager;
import forpdateam.ru.forpda.ui.fragments.TabFragment;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by radiationx on 05.11.17.
 */

public class BasePresenter<V> implements IBasePresenter<V> {
    protected V view;
    private CompositeDisposable disposables = new CompositeDisposable();

    public BasePresenter(V view) {
        this.view = view;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {

    }

    @Override
    public void onAttach() {

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

    }

    @Override
    public void onDetach() {

    }

    @Override
    public void onDestroyView() {

    }

    @Override
    public void onDestroy() {
        if (!disposables.isDisposed())
            disposables.dispose();
        this.view = null;
    }

    public <T> void subscribe(@NonNull Observable<T> observable, @NonNull Consumer<T> onNext, @NonNull T onErrorReturn) {
        subscribe(observable, onNext, onErrorReturn, null);
    }

    public <T> void subscribe(@NonNull Observable<T> observable, @NonNull Consumer<T> onNext, @NonNull T onErrorReturn, View.OnClickListener onErrorAction) {
        Disposable disposable = observable
                .onErrorReturn(throwable -> {
                    handleErrorRx(throwable, onErrorAction);
                    return onErrorReturn;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onNext, throwable -> handleErrorRx(throwable, onErrorAction));

        disposables.add(disposable);
    }

    private void handleErrorRx(Throwable throwable, View.OnClickListener listener) {
        new Handler(Looper.getMainLooper()).post(() -> {
            TabFragment tabFragment = TabManager.get().getActive();
            if (tabFragment == null) {
                throwable.printStackTrace();
                ACRA.getErrorReporter().handleException(throwable);
            } else {
                ErrorHandler.handle(tabFragment, throwable, listener);
            }
        });

    }
}
