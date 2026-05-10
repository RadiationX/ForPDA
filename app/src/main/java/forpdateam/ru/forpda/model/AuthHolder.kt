package forpdateam.ru.forpda.model

import forpdateam.ru.forpda.client.CookieStorage
import forpdateam.ru.forpda.common.flowpreferences.FlowPreferences
import forpdateam.ru.forpda.entity.common.AuthData
import forpdateam.ru.forpda.entity.common.AuthState
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlin.time.Duration.Companion.seconds

class AuthHolder(
    private val preferences: FlowPreferences,
    private val cookieStorage: CookieStorage
) {

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
                createAuthData(cookies, skipFlag)
            }
        ).stateIn(GlobalScope, SharingStarted.WhileSubscribed(1.seconds), getAuthData())
    }

    fun observe(): StateFlow<AuthData> = dataFlow

    fun get(): AuthData = dataFlow.value

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

    private fun getAuthData(): AuthData {
        return createAuthData(cookieStorage.getAll(), skipPreference.get())
    }

    private fun createAuthData(cookies: List<CookieStorage.AppCookie>, skipFlag: Boolean): AuthData {
        val memberIdCookie = cookies.find { it.cookie.name == CookieStorage.MEMBER_ID }
        val passHashCookie = cookies.find { it.cookie.name == CookieStorage.PASS_HASH }
        if (memberIdCookie == null || passHashCookie == null) {
            return createUnauthData(skipFlag)
        }
        val memberId = memberIdCookie.cookie.value.toIntOrNull() ?: AuthData.NO_ID
        if (memberId == AuthData.NO_ID) {
            return createUnauthData(skipFlag)
        }
        return AuthData(
            userId = memberId,
            state = skipFlag.toAuthState()
        )
    }

    private fun createUnauthData(skipFlag: Boolean): AuthData {
        return AuthData(
            userId = AuthData.NO_ID,
            state = skipFlag.toAuthState()
        )
    }

    private fun Boolean.toAuthState(): AuthState {
        return if (this) {
            AuthState.SKIP
        } else {
            AuthState.NO_AUTH
        }
    }
}