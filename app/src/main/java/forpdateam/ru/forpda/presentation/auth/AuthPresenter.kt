package forpdateam.ru.forpda.presentation.auth

import moxy.InjectViewState
import forpdateam.ru.forpda.common.mvp.BasePresenter
import forpdateam.ru.forpda.entity.common.AuthData
import forpdateam.ru.forpda.entity.common.AuthState
import forpdateam.ru.forpda.entity.remote.auth.AuthForm
import forpdateam.ru.forpda.entity.remote.profile.ProfileModel
import forpdateam.ru.forpda.model.AuthHolder
import forpdateam.ru.forpda.model.SchedulersProvider
import forpdateam.ru.forpda.model.repository.auth.AuthRepository
import forpdateam.ru.forpda.model.repository.profile.ProfileRepository
import forpdateam.ru.forpda.presentation.IErrorHandler
import forpdateam.ru.forpda.presentation.ISystemLinkHandler
import forpdateam.ru.forpda.presentation.Screen
import forpdateam.ru.forpda.presentation.TabRouter
import io.reactivex.Observable
import java.util.concurrent.TimeUnit

/**
 * Created by radiationx on 02.01.18.
 */

@InjectViewState
class AuthPresenter(
        private val authRepository: AuthRepository,
        private val profileRepository: ProfileRepository,
        private val router: TabRouter,
        private val schedulers: SchedulersProvider,
        private val authHolder: AuthHolder,
        private val errorHandler: IErrorHandler,
        private val systemLinkHandler: ISystemLinkHandler
) : BasePresenter<AuthView>() {

    private var fieldsFilled = false
    private var authForm: AuthForm? = null

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        loadForm()
    }

    fun setFieldsFilled(isFilled: Boolean) {
        fieldsFilled = isFilled
        viewState.setSendEnabled(fieldsFilled)
    }

    fun signIn(
            nick: String,
            password: String,
            captcha: String,
            isHidden: Boolean
    ) {
        authForm?.also { authForm ->
            authForm.nick = nick
            authForm.password = password
            authForm.captcha = captcha
            authForm.isHidden = isHidden
            authRepository
                    .signIn(authForm)
                    .doOnSubscribe { viewState.setSendRefreshing(true) }
                    .doAfterTerminate { viewState.setSendRefreshing(false) }
                    .subscribe({
                        viewState.onSuccessAuth()
                        loadProfile("https://4pda.ru/forum/index.php?showuser=${authHolder.get().userId}")
                    }, {
                        authForm.captcha = null
                        viewState.onFormLoaded(authForm)
                        loadForm()
                        errorHandler.handle(it)
                    })
                    .untilDestroy()
        }
    }

    fun onClickSkip() {
        authHolder.set(authHolder.get().apply {
            userId = AuthData.NO_ID
            if (authHolder.get().state != AuthState.AUTH) {
                state = AuthState.SKIP
            }
        })
        router.exit()
    }

    fun onRegistrationClick() {
        systemLinkHandler.handle("https://4pda.ru/forum/index.php?act=auth#reg")
    }

    private fun loadForm() {
        authRepository
                .loadForm()
                .doOnSubscribe { viewState.setSendEnabled(false) }
                .doAfterTerminate { viewState.setSendEnabled(fieldsFilled) }
                .subscribe({
                    it.apply {
                        nick = authForm?.nick
                        password = authForm?.password
                    }
                    authForm = it
                    viewState.onFormLoaded(it)
                }, {
                    errorHandler.handle(it)
                })
                .untilDestroy()
    }

    private fun loadProfile(url: String) {
        profileRepository
                .loadProfile(url)
                /*.doOnTerminate { viewState.setRefreshing(true) }
                .doAfterTerminate { viewState.setRefreshing(false) }*/
                .subscribe({
                    viewState.showProfile(it)
                    delayedExit(it)
                }, {
                    errorHandler.handle(it)
                })
                .untilDestroy()
    }

    private fun delayedExit(profile: ProfileModel) {
        Observable
                .just(false)
                .delay(2000L, TimeUnit.MILLISECONDS)
                .subscribeOn(schedulers.io())
                .observeOn(schedulers.ui())
                .subscribe {
                    router.exit()
                }
                .untilDestroy()
    }
}
