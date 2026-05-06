package forpdateam.ru.forpda.common.mvp

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import moxy.MvpPresenter
import moxy.MvpView

/**
 * Created by radiationx on 05.11.17.
 */

open class BasePresenter<V : MvpView> : MvpPresenter<V>() {

    val viewModelScope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())

    override fun onDestroy() {
        viewModelScope.cancel()
    }
}
