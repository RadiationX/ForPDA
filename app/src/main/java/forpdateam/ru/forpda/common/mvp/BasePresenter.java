package forpdateam.ru.forpda.common.mvp;

import android.os.Bundle;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

/**
 * Created by radiationx on 05.11.17.
 */

public class BasePresenter<T> implements IBasePresenter<T> {
    protected T view;
    private CompositeDisposable disposables = new CompositeDisposable();

    public BasePresenter(T view) {
        this.view = view;
    }

    protected void addDisposable(Disposable disposable) {
        disposables.add(disposable);
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
}
