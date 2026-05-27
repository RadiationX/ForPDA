package forpdateam.ru.forpda.entity.common

import ru.radiationx.coretypes.UserId
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

sealed class AuthState {
    data class Auth(val userId: UserId) : AuthState()
    data object NoAuth : AuthState()
    data object Skip : AuthState()

    @OptIn(ExperimentalContracts::class)
    fun isAuth(): Boolean {
        contract {
            returns(true) implies (this@AuthState is Auth)
        }
        return this is Auth
    }

    fun asAuth(): Auth? {
        return this as? Auth
    }
}
