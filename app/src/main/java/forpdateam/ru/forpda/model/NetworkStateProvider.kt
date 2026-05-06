package forpdateam.ru.forpda.model


import kotlinx.coroutines.flow.Flow

/**
 * Created by radiationx on 10.02.18.
 */

interface NetworkStateProvider {
    fun observeState(): Flow<Boolean>
    fun setState(state: Boolean)
    fun getState(): Boolean
}
