package forpdateam.ru.forpda.mvp;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

/**
 * Чисто на всякий случай. Ну вдруг где активити захочется)
 */

public abstract class MvpActivity<T extends MvpPresenter> extends AppCompatActivity implements MvpView {
    public T presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        presenter = getPresenter();
        presenter.attachView(this);
    }

    @Override
    protected void onDestroy() {
        presenter.detachView(this);
        super.onDestroy();
    }

    @NonNull
    abstract protected T getPresenter();
}
