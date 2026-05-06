package forpdateam.ru.forpda.common.mvp

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import moxy.MvpPresenter
import moxy.MvpView

/**
 * Created by radiationx on 05.11.17.
 */

open class BasePresenter<V : MvpView> : MvpPresenter<V>() {

    val viewModelScope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())

    private var compositeDisposable = CompositeDisposable()

    override fun onDestroy() {
        viewModelScope.cancel()
        compositeDisposable.dispose()
    }

    fun Disposable.untilDestroy() {
        compositeDisposable.add(this)
    }
}
