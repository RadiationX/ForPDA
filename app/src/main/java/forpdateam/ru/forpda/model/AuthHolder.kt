package forpdateam.ru.forpda.model

import forpdateam.ru.forpda.client.CookieStorage
import forpdateam.ru.forpda.entity.common.AuthState
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import ru.radiationx.coretypes.UserId
import ru.radiationx.flowpreferences.FlowPreferences
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

class AuthHolder @Inject constructor(
    private val preferences: FlowPreferences,
    private val cookieStorage: CookieStorage
) {

    private companion object {
        const val NO_ID = 0
    }

    private val skipPreference by lazy {
        preferences.getBoolean("auth_skip_flag", false)
    }

    private val authKeyPreference by lazy {
        preferences.getString("auth_key")
    }

    val dataFlow by lazy {
        combine(
            flow = cookieStorage.observe(),
            flow2 = skipPreference,
            transform = { cookies, skipFlag ->
                createAuthState(cookies, skipFlag)
            }
        ).stateIn(GlobalScope, SharingStarted.WhileSubscribed(1.seconds), getAuthState())
    }

    fun observe(): StateFlow<AuthState> = dataFlow

    fun get(): AuthState = dataFlow.value

    fun clearData() {
        authKeyPreference.remove()
        cookieStorage.removeByCookieName(CookieStorage.AUTH_COOKIES)
    }

    fun setSkip() {
        skipPreference.set(true)
    }

    fun getAuthKey(): String? {
        return authKeyPreference.get()
    }

    fun setAuthKey(authKey: String) {
        authKeyPreference.set(authKey)
    }

    private fun getAuthState(): AuthState {
        return createAuthState(cookieStorage.getAll(), skipPreference.get())
    }

    private fun createAuthState(cookies: List<CookieStorage.AppCookie>, skipFlag: Boolean): AuthState {
        val memberIdCookie = cookies.find { it.cookie.name == CookieStorage.MEMBER_ID }
        val passHashCookie = cookies.find { it.cookie.name == CookieStorage.PASS_HASH }
        if (memberIdCookie == null || passHashCookie == null) {
            return createUnauthState(skipFlag)
        }
        val memberId = memberIdCookie.cookie.value.toIntOrNull() ?: NO_ID
        if (memberId == NO_ID) {
            return createUnauthState(skipFlag)
        }
        return AuthState.Auth(UserId(memberId))
    }

    private fun createUnauthState(skipFlag: Boolean): AuthState {
        return if (skipFlag) {
            AuthState.Skip
        } else {
            AuthState.NoAuth
        }
    }
}