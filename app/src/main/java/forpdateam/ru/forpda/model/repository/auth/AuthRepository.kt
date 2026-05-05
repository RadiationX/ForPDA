package forpdateam.ru.forpda.model.repository.auth

import forpdateam.ru.forpda.entity.app.profile.IUserHolder
import forpdateam.ru.forpda.entity.common.AuthData
import forpdateam.ru.forpda.entity.common.AuthState
import forpdateam.ru.forpda.entity.common.MessageCounters
import forpdateam.ru.forpda.entity.remote.auth.AuthCaptcha
import forpdateam.ru.forpda.entity.remote.auth.AuthForm
import forpdateam.ru.forpda.model.AuthHolder
import forpdateam.ru.forpda.model.CountersHolder
import forpdateam.ru.forpda.model.SchedulersProvider
import forpdateam.ru.forpda.model.data.remote.api.auth.AuthApi
import forpdateam.ru.forpda.model.repository.BaseRepository

/**
 * Created by radiationx on 02.01.18.
 */

class AuthRepository(
    private val schedulers: SchedulersProvider,
    private val authApi: AuthApi,
    private val authHolder: AuthHolder,
    private val countersHolder: CountersHolder,
    private val userHolder: IUserHolder
) : BaseRepository(schedulers) {

    suspend fun loadCaptcha(): AuthCaptcha {
        return authApi.getCaptcha()
    }

    suspend fun signIn(captcha: AuthCaptcha, form: AuthForm) {
        authApi.login(captcha, form)
    }

    suspend fun signOut() {
        authApi.logout()
        authHolder.set(
            AuthData(
                userId = AuthData.NO_ID,
                state = AuthState.NO_AUTH
            )
        )
        countersHolder.set(
            MessageCounters(
                mentions = 0,
                favorites = 0,
                qms = 0
            )
        )
        userHolder.user = null
    }

}
