package forpdateam.ru.forpda.model.system

import android.content.Context
import android.net.ConnectivityManager
import forpdateam.ru.forpda.model.NetworkStateProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Created by radiationx on 10.02.18.
 */

class AppNetworkState(
    private val context: Context
) : NetworkStateProvider {

    private val stateFlow = MutableStateFlow(getLocalState())

    private fun getLocalState(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.activeNetworkInfo?.isConnected ?: false
    }

    override fun observeState(): Flow<Boolean> {
        return stateFlow
    }

    override fun getState(): Boolean {
        stateFlow.value = getLocalState()
        return stateFlow.value
    }

    override fun setState(state: Boolean) {
        stateFlow.value = state
    }
}
