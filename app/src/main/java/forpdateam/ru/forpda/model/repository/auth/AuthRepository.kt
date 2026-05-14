package forpdateam.ru.forpda.model.repository.auth

import forpdateam.ru.forpda.entity.common.MessageCounters
import forpdateam.ru.forpda.entity.remote.auth.AuthCaptcha
import forpdateam.ru.forpda.entity.remote.auth.AuthForm
import forpdateam.ru.forpda.model.CountersHolder
import forpdateam.ru.forpda.model.data.remote.api.auth.AuthApi

/**
 * Created by radiationx on 02.01.18.
 */

class AuthRepository(
    private val authApi: AuthApi,
    private val countersHolder: CountersHolder,
) {

    suspend fun loadCaptcha(): AuthCaptcha {
        return authApi.getCaptcha()
    }

    suspend fun signIn(captcha: AuthCaptcha, form: AuthForm) {
        authApi.login(captcha, form)
    }

    suspend fun signOut() {
        authApi.logout()
        countersHolder.set(
            MessageCounters(
                mentions = 0,
                favorites = 0,
                qms = 0
            )
        )
    }

}
