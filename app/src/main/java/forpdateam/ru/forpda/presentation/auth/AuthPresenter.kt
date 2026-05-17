package forpdateam.ru.forpda.presentation.auth

import forpdateam.ru.forpda.common.mvp.BasePresenter
import forpdateam.ru.forpda.entity.remote.auth.AuthCaptcha
import forpdateam.ru.forpda.entity.remote.auth.AuthForm
import forpdateam.ru.forpda.extensions.coRunCatching
import forpdateam.ru.forpda.model.AuthHolder
import forpdateam.ru.forpda.model.repository.auth.AuthRepository
import forpdateam.ru.forpda.model.repository.profile.ProfileRepository
import forpdateam.ru.forpda.presentation.ErrorHandler
import forpdateam.ru.forpda.presentation.SystemLinkHandler
import forpdateam.ru.forpda.presentation.TabRouter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import moxy.InjectViewState

/**
 * Created by radiationx on 02.01.18.
 */

@InjectViewState
class AuthPresenter(
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository,
    private val router: TabRouter,
    private val authHolder: AuthHolder,
    private val errorHandler: ErrorHandler,
    private val systemLinkHandler: SystemLinkHandler
) : BasePresenter<AuthView>() {

    private var currentCaptcha: AuthCaptcha? = null
    private var form = AuthForm("", "", "", false)

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        loadForm()
    }

    fun updateForm(form: AuthForm) {
        this.form = form
        viewState.setSendEnabled(form.isFilled())
    }

    fun signIn() {
        val captcha = currentCaptcha ?: return
        viewModelScope.launch {
            viewState.setSendRefreshing(true)
            coRunCatching {
                authRepository.signIn(captcha, form)
            }.onSuccess {
                viewState.onSuccessAuth()
                loadProfile("https://4pda.to/forum/index.php?showuser=${authHolder.get().userId}")
            }.onFailure {
                form = form.copy(captcha = "")
                viewState.onFormChanged(form)
                loadForm()
                errorHandler.handle(it)
            }
            viewState.setSendRefreshing(false)
        }
    }

    fun onClickSkip() {
        authHolder.setSkip()
        router.exit()
    }

    fun onRegistrationClick() {
        systemLinkHandler.handle("https://4pda.to/forum/index.php?act=auth#reg")
    }

    private fun loadForm() {
        viewModelScope.launch {
            viewState.setSendEnabled(false)
            coRunCatching {
                authRepository.loadCaptcha()
            }.onSuccess {
                currentCaptcha = it
                viewState.onCaptchaLoaded(it)
            }.onFailure {
                errorHandler.handle(it)
            }
            viewState.setSendEnabled(form.isFilled())
        }
    }

    private fun loadProfile(url: String) {
        viewModelScope.launch {
            coRunCatching {
                profileRepository.loadProfile(url)
            }.onSuccess {
                viewState.showProfile(it)
                delay(2000)
                router.exit()
            }.onFailure {
                errorHandler.handle(it)
            }
        }
    }
}
