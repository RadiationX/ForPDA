package forpdateam.ru.forpda.common.mvp

import com.arellomobile.mvp.MvpPresenter
import com.arellomobile.mvp.MvpView
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

/**
 * Created by radiationx on 05.11.17.
 */

open class BasePresenter<V : MvpView> : MvpPresenter<V>() {
    private var compositeDisposable = CompositeDisposable()

    override fun onDestroy() {
        compositeDisposable.dispose()
    }

    fun Disposable.untilDestroy() {
        compositeDisposable.add(this)
    }
}
