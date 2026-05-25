package forpdateam.ru.forpda.presentation.other

import forpdateam.ru.forpda.common.mvp.BasePresenter
import forpdateam.ru.forpda.entity.app.CloseableInfo
import forpdateam.ru.forpda.entity.app.other.AppMenuItem
import forpdateam.ru.forpda.entity.remote.others.user.ForumUser
import forpdateam.ru.forpda.extensions.coRunCatching
import forpdateam.ru.forpda.model.AuthHolder
import forpdateam.ru.forpda.model.CloseableInfoHolder
import forpdateam.ru.forpda.model.interactors.other.MenuRepository
import forpdateam.ru.forpda.model.repository.auth.AuthRepository
import forpdateam.ru.forpda.model.repository.profile.ProfileRepository
import forpdateam.ru.forpda.presentation.ErrorHandler
import forpdateam.ru.forpda.presentation.LinkHandler
import forpdateam.ru.forpda.presentation.Screen
import forpdateam.ru.forpda.presentation.SystemLinkHandler
import forpdateam.ru.forpda.presentation.TabRouter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import moxy.InjectViewState
import ru.radiationx.coretypes.UserId

@InjectViewState
class OtherPresenter(
    private val router: TabRouter,
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository,
    private val authHolder: AuthHolder,
    private val errorHandler: ErrorHandler,
    private val menuRepository: MenuRepository,
    private val closeableInfoHolder: CloseableInfoHolder,
    private val linkHandler: LinkHandler,
    private val systemLinkHandler: SystemLinkHandler
) : BasePresenter<OtherView>() {

    private val closeableInfoIds = arrayOf(
        CloseableInfoHolder.item_other_menu_drag
    )

    private var localMenu = mapOf<Int, List<AppMenuItem>>()
    private val localCloseableInfo = mutableListOf<CloseableInfo>()

    private var user: ForumUser? = null

    private var isMenuDragMode = false

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        subscribeUser()
        authHolder
            .observe()
            .onEach {
                if (!authHolder.get().isAuth()) {
                    user = null
                }
                updateMenuItems()
            }
            .launchIn(viewModelScope)

        menuRepository
            .observerMenu()
            .onEach {
                localMenu = it
                updateMenuItems()
            }
            .launchIn(viewModelScope)

        closeableInfoHolder
            .observe()
            .onEach { info ->
                localCloseableInfo.clear()
                localCloseableInfo.addAll(info.filter { closeableInfoIds.contains(it.id) && !it.isClosed })
                updateMenuItems()
            }
            .launchIn(viewModelScope)
    }

    fun onMenuDragModeChange(isDragMode: Boolean) {
        isMenuDragMode = isDragMode
        updateMenuItems()
    }

    private fun updateMenuItems() {
        if (!isMenuDragMode) {
            viewState.showItems(user, localCloseableInfo, localMenu.map { it.value })
        }
    }

    private fun subscribeUser() {
        viewModelScope.launch {
            coRunCatching {
                profileRepository.loadSelf()
            }
        }

        profileRepository
            .observeCurrentUser()
            .onEach {
                user = it
                updateMenuItems()
            }
            .launchIn(viewModelScope)
    }

    fun signOut() {
        viewModelScope.launch {
            coRunCatching {
                authRepository.signOut()
            }.onSuccess {
                router.showSystemMessage("Данные авторизации удалены")
            }.onFailure {
                errorHandler.handle(it)
            }
        }
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
                    linkHandler.handle("https://4pda.to/forum/index.php?showuser=2556269")
                }

                MenuRepository.item_link_forum_topic -> {
                    linkHandler.handle("https://4pda.to/forum/index.php?showtopic=820313")
                }

                MenuRepository.item_link_forum_faq -> {
                    linkHandler.handle(
                        "http://4pda.to/forum/index.php?s=&showtopic=820313&view=findpost&p=64077514"
                    )
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
        val authData = authHolder.get()
        if (authData.isAuth()) {
            router.navigateTo(Screen.Profile(UserId(authData.userId)))
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
