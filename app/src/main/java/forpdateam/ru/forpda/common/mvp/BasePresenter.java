package forpdateam.ru.forpda.common.mvp;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.view.View;

import com.arellomobile.mvp.MvpPresenter;
import com.arellomobile.mvp.MvpView;

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

public class BasePresenter<V extends MvpView> extends MvpPresenter<V> {
    private CompositeDisposable disposables = new CompositeDisposable();

    @Override
    public void onDestroy() {
        if (!disposables.isDisposed())
            disposables.dispose();
    }

    protected void addToDisposable(Disposable disposable) {
        disposables.add(disposable);
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

    protected void handleErrorRx(Throwable throwable) {
        handleErrorRx(throwable, null);
    }

    protected void handleErrorRx(Throwable throwable, View.OnClickListener listener) {
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
