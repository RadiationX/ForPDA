package forpdateam.ru.forpda.presentation.other

import com.arellomobile.mvp.InjectViewState
import forpdateam.ru.forpda.common.mvp.BasePresenter
import forpdateam.ru.forpda.entity.app.CloseableInfo
import forpdateam.ru.forpda.entity.app.other.AppMenuItem
import forpdateam.ru.forpda.entity.remote.profile.ProfileModel
import forpdateam.ru.forpda.model.AuthHolder
import forpdateam.ru.forpda.model.CloseableInfoHolder
import forpdateam.ru.forpda.model.interactors.other.MenuRepository
import forpdateam.ru.forpda.model.repository.auth.AuthRepository
import forpdateam.ru.forpda.model.repository.profile.ProfileRepository
import forpdateam.ru.forpda.presentation.*

@InjectViewState
class OtherPresenter(
        private val router: TabRouter,
        private val authRepository: AuthRepository,
        private val profileRepository: ProfileRepository,
        private val authHolder: AuthHolder,
        private val errorHandler: IErrorHandler,
        private val menuRepository: MenuRepository,
        private val closeableInfoHolder: CloseableInfoHolder,
        private val linkHandler: ILinkHandler,
        private val systemLinkHandler: ISystemLinkHandler
) : BasePresenter<OtherView>() {

    private val closeableInfoIds = arrayOf(
            CloseableInfoHolder.item_other_menu_drag
    )

    private var localMenu = mapOf<Int, List<AppMenuItem>>()
    private val localCloseableInfo = mutableListOf<CloseableInfo>()

    private var profileItem: ProfileModel? = null

    private var isMenuDragMode = false

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        subscribeUser()
        authHolder
                .observe()
                .subscribe {
                    if (!authHolder.get().isAuth()) {
                        profileItem = null
                    }
                    updateMenuItems()
                }
                .untilDestroy()

        menuRepository
                .observerMenu()
                .subscribe {
                    localMenu = it
                    updateMenuItems()
                }
                .untilDestroy()

        closeableInfoHolder
                .observe()
                .subscribe { info ->
                    localCloseableInfo.clear()
                    localCloseableInfo.addAll(info.filter { closeableInfoIds.contains(it.id) && !it.isClosed })
                    updateMenuItems()
                }
                .untilDestroy()
    }

    fun onMenuDragModeChange(isDragMode: Boolean) {
        isMenuDragMode = isDragMode
        updateMenuItems()
    }

    private fun updateMenuItems() {
        if (!isMenuDragMode) {
            viewState.showItems(profileItem, localCloseableInfo, localMenu.map { it.value })
        }
    }

    private fun subscribeUser() {
        profileRepository
                .loadSelf()
                .subscribe({}, {})
                .untilDestroy()
        profileRepository
                .observeCurrentUser()
                .subscribe {
                    profileItem = it.value
                    updateMenuItems()
                }
                .untilDestroy()
    }

    fun signOut() {
        authRepository
                .signOut()
                .subscribe({
                    router.showSystemMessage("Данные авторизации удалены")
                }, {
                    errorHandler.handle(it)
                })
                .untilDestroy()
    }

    fun onMenuClick(item: AppMenuItem) {
        if (item.screen != null) {
            item.screen.also {
                router.navigateTo(it)
            }
            menuRepository.setLastOpened(item.id)
        } else {
            when (item.id) {
                MenuRepository.item_link_forum_author -> {
                    linkHandler.handle("https://4pda.ru/forum/index.php?showuser=2556269", router)
                }
                MenuRepository.item_link_forum_topic -> {
                    linkHandler.handle("https://4pda.ru/forum/index.php?showtopic=820313", router)
                }
                MenuRepository.item_link_forum_faq -> {
                    linkHandler.handle("http://4pda.ru/forum/index.php?s=&showtopic=820313&view=findpost&p=64077514", router)
                }
                MenuRepository.item_link_chat_telegram -> {
                    systemLinkHandler.handle("https://t.me/forpda_app")
                }
                MenuRepository.item_link_play_market -> {
                    systemLinkHandler.handle("https://play.google.com/store/apps/details?id=ru.forpdateam.forpda")
                }
                MenuRepository.item_link_github -> {
                    systemLinkHandler.handle("https://github.com/RadiationX/ForPDA")
                }
                MenuRepository.item_link_bitbucket -> {
                    systemLinkHandler.handle("https://bitbucket.org/RadiationX/forpda/")
                }
            }

        }
    }

    fun onProfileClick() {
        if (authHolder.get().isAuth()) {
            router.navigateTo(Screen.Profile())
        } else {
            router.navigateTo(Screen.Auth())
        }
    }

    fun onChangeMenuSequence(items: List<AppMenuItem>) {
        menuRepository.setMainMenuSequence(items)
    }

    fun onCloseInfo(item: CloseableInfo) {
        closeableInfoHolder.close(item)
    }
}
