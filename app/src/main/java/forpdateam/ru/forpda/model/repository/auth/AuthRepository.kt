package forpdateam.ru.forpda.model.repository.auth

import forpdateam.ru.forpda.entity.app.profile.IUserHolder
import forpdateam.ru.forpda.entity.app.profile.UserHolder
import forpdateam.ru.forpda.entity.common.AuthData
import forpdateam.ru.forpda.entity.common.AuthState
import forpdateam.ru.forpda.entity.remote.auth.AuthForm
import forpdateam.ru.forpda.model.AuthHolder
import forpdateam.ru.forpda.model.CountersHolder
import forpdateam.ru.forpda.model.SchedulersProvider
import forpdateam.ru.forpda.model.data.remote.api.auth.AuthApi
import forpdateam.ru.forpda.model.repository.BaseRepository
import io.reactivex.Observable
import io.reactivex.Single

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

    fun loadForm(): Single<AuthForm> = Single
            .fromCallable { authApi.getForm() }
            .runInIoToUi()

    fun signIn(authForm: AuthForm): Single<AuthForm> = Single
            .fromCallable { authApi.login(authForm) }
            .runInIoToUi()

    fun signOut(): Single<Boolean> = Single
            .fromCallable { authApi.logout() }
            .doOnSuccess {
                authHolder.set(authHolder.get().apply {
                    userId = AuthData.NO_ID
                    state = AuthState.NO_AUTH
                })
                countersHolder.set(countersHolder.get().apply {
                    mentions = 0
                    favorites = 0
                    qms = 0
                })
                userHolder.user = null
            }
            .runInIoToUi()

}
