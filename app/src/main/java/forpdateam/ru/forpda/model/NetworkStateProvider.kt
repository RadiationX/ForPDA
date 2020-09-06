package forpdateam.ru.forpda.model


import io.reactivex.Observable

/**
 * Created by radiationx on 10.02.18.
 */

interface NetworkStateProvider {
    fun observeState(): Observable<Boolean>
    fun setState(state: Boolean)
    fun getState(): Boolean
}
