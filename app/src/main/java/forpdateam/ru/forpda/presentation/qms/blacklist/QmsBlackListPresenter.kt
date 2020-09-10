package forpdateam.ru.forpda.presentation.qms.blacklist

import moxy.InjectViewState
import forpdateam.ru.forpda.common.mvp.BasePresenter
import forpdateam.ru.forpda.entity.remote.qms.QmsContact
import forpdateam.ru.forpda.model.interactors.qms.QmsInteractor
import forpdateam.ru.forpda.presentation.IErrorHandler
import forpdateam.ru.forpda.presentation.ILinkHandler
import forpdateam.ru.forpda.presentation.Screen
import forpdateam.ru.forpda.presentation.TabRouter

/**
 * Created by radiationx on 11.11.17.
 */

@InjectViewState
class QmsBlackListPresenter(
        private val qmsInteractor: QmsInteractor,
        private val router: TabRouter,
        private val linkHandler: ILinkHandler,
        private val errorHandler: IErrorHandler
) : BasePresenter<QmsBlackListView>() {


    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        loadContacts()
    }


    fun loadContacts() {
        qmsInteractor
                .getBlackList()
                .doOnSubscribe { viewState.setRefreshing(true) }
                .doAfterTerminate { viewState.setRefreshing(false) }
                .subscribe({
                    viewState.showContacts(it)
                }, {
                    errorHandler.handle(it)
                })
                .untilDestroy()
    }

    fun blockUser(nick: String) {
        qmsInteractor
                .blockUser(nick)
                .subscribe({
                    viewState.showContacts(it)
                    viewState.clearNickField()
                }, {
                    errorHandler.handle(it)
                })
                .untilDestroy()
    }

    fun unBlockUser(id: Int) {
        qmsInteractor
                .unBlockUsers(id)
                .subscribe({
                    viewState.showContacts(it)
                    viewState.clearNickField()
                }, {
                    errorHandler.handle(it)
                })
                .untilDestroy()
    }

    fun searchUser(nick: String) {
        qmsInteractor
                .findUser(nick)
                .subscribe({
                    viewState.showFoundUsers(it)
                }, {
                    errorHandler.handle(it)
                })
                .untilDestroy()
    }

    fun onItemLongClick(item: QmsContact) {
        viewState.showItemDialogMenu(item)
    }

    fun openProfile(item: QmsContact) {
        linkHandler.handle("https://4pda.ru/forum/index.php?showuser=${item.id}", router)
    }

    fun openDialogs(item: QmsContact) {
        router.navigateTo(Screen.QmsThemes().apply {
            screenTitle = item.nick
            userId = item.id
            avatarUrl = item.avatar
        })
    }
}
