package forpdateam.ru.forpda.common.mvp

import kotlinx.coroutines.CoroutineScope
import moxy.MvpPresenter
import moxy.MvpView
import moxy.presenterScope

/**
 * Created by radiationx on 05.11.17.
 */

open class BasePresenter<V : MvpView> : MvpPresenter<V>() {

    val viewModelScope: CoroutineScope
        get() = presenterScope

}
