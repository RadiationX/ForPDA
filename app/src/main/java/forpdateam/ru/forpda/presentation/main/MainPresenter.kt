package forpdateam.ru.forpda.presentation.main

import com.arellomobile.mvp.InjectViewState
import forpdateam.ru.forpda.App
import forpdateam.ru.forpda.common.mvp.BasePresenter
import forpdateam.ru.forpda.entity.common.AuthState
import forpdateam.ru.forpda.model.AuthHolder
import forpdateam.ru.forpda.model.interactors.other.MenuRepository
import forpdateam.ru.forpda.model.interactors.qms.QmsInteractor
import forpdateam.ru.forpda.model.preferences.MainPreferencesHolder
import forpdateam.ru.forpda.model.preferences.OtherPreferencesHolder
import forpdateam.ru.forpda.presentation.IErrorHandler
import forpdateam.ru.forpda.presentation.ILinkHandler
import forpdateam.ru.forpda.presentation.Screen
import forpdateam.ru.forpda.presentation.TabRouter

@InjectViewState
class MainPresenter(
        private val router: TabRouter,
        private val authHolder: AuthHolder,
        private val linkHandler: ILinkHandler,
        private val menuRepository: MenuRepository,
        private val qmsInteractor: QmsInteractor,
        private val otherPreferencesHolder: OtherPreferencesHolder,
        private val mainPreferencesHolder: MainPreferencesHolder,
        private val errorHandler: IErrorHandler
) : BasePresenter<MainView>() {

    private var isRestored: Boolean = false
    private var startLink: String = ""


    init {
        mainPreferencesHolder
                .observeThemeIsDark()
                .subscribe { isDark ->
                    viewState.changeTheme(isDark)
                }
                .untilDestroy()
    }

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        qmsInteractor.subscribeEvents()



        val firstAppStart = otherPreferencesHolder.getAppFirstStart()
        if (firstAppStart) {
            viewState.showFirstStartAnimation()
            otherPreferencesHolder.setAppFirstStart(false)
        }

        val linkHandled = linkHandler.handle(startLink, router)

        if (!isRestored && !linkHandled) {
            val authState = authHolder.get().state
            if (firstAppStart && authState == AuthState.NO_AUTH) {
                router.navigateTo(Screen.Auth())
            } else {
                val lastMenuId = menuRepository.getLastOpened()
                val screen: Screen = if (menuRepository.menuItemContains(lastMenuId)) {
                    menuRepository.getMenuItem(lastMenuId).screen ?: Screen.ArticleList()
                } else {
                    Screen.ArticleList()
                }
                router.navigateTo(screen)
            }
        }
    }

    fun setIsRestored(restored: Boolean) {
        isRestored = restored
    }

    fun setStartLink(link: String) {
        startLink = link
    }


    fun openLink(url: String) {
        linkHandler.handle(url, router)
    }
}