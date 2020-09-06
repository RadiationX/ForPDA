package forpdateam.ru.forpda.model.system

import android.content.Context
import android.net.ConnectivityManager
import com.jakewharton.rxrelay2.BehaviorRelay
import forpdateam.ru.forpda.model.NetworkStateProvider
import io.reactivex.Observable

/**
 * Created by radiationx on 10.02.18.
 */

class AppNetworkState(
        private val context: Context
) : NetworkStateProvider {
    private val stateRelay: BehaviorRelay<Boolean>

    private fun getLocalState(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.activeNetworkInfo?.isConnected ?: false
    }

    init {
        stateRelay = BehaviorRelay.createDefault(getLocalState())
    }

    override fun observeState(): Observable<Boolean> {
        return stateRelay
    }

    override fun getState(): Boolean {
        val result = getLocalState()
        if (result != stateRelay.value) {
            stateRelay.accept(result)
        }
        return stateRelay.value!!
    }

    override fun setState(state: Boolean) {
        stateRelay.accept(state)
    }
}
