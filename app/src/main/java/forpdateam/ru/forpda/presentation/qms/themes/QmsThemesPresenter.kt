package forpdateam.ru.forpda.presentation.qms.themes

import android.util.Log
import com.arellomobile.mvp.InjectViewState
import forpdateam.ru.forpda.common.mvp.BasePresenter
import forpdateam.ru.forpda.entity.remote.qms.QmsTheme
import forpdateam.ru.forpda.entity.remote.qms.QmsThemes
import forpdateam.ru.forpda.model.interactors.qms.QmsInteractor
import forpdateam.ru.forpda.presentation.IErrorHandler
import forpdateam.ru.forpda.presentation.ILinkHandler
import forpdateam.ru.forpda.presentation.Screen
import forpdateam.ru.forpda.presentation.TabRouter

/**
 * Created by radiationx on 11.11.17.
 */

@InjectViewState
class QmsThemesPresenter(
        private val qmsInteractor: QmsInteractor,
        private val router: TabRouter,
        private val linkHandler: ILinkHandler,
        private val errorHandler: IErrorHandler
) : BasePresenter<QmsThemesView>() {

    var themesId: Int = 0
    var avatarUrl: String? = null
    var currentData: QmsThemes? = null

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        qmsInteractor
                .observeThemes(themesId)
                .subscribe {
                    currentData = it
                    viewState.showThemes(it)
                }
                .untilDestroy()
        avatarUrl?.let { viewState.showAvatar(it) }
    }

    fun loadThemes() {
        qmsInteractor
                .getThemesList(themesId)
                .doOnSubscribe { viewState.setRefreshing(true) }
                .doAfterTerminate { viewState.setRefreshing(false) }
                .subscribe({
                    currentData = it
                    if (it.themes.isEmpty() && it.nick != null) {
                        openChat()
                    }
                    //viewState.showThemes(it)
                }, {
                    errorHandler.handle(it)
                })
                .untilDestroy()
    }

    fun blockUser() {
        currentData?.nick?.let { nick ->
            qmsInteractor
                    .blockUser(nick)
                    .map { it.firstOrNull { it.nick == nick } != null }
                    .subscribe({
                        viewState.onBlockUser(it)
                    }, {
                        errorHandler.handle(it)
                    })
                    .untilDestroy()
        }
    }

    fun deleteTheme(themeId: Int) {
        currentData?.let {
            qmsInteractor
                    .deleteTheme(it.userId, themeId)
                    .subscribe({
                        //viewState.showThemes(it)
                    }, {
                        errorHandler.handle(it)
                    })
                    .untilDestroy()
        }
    }

    fun openProfile(userId: Int) {
        linkHandler.handle("https://4pda.ru/forum/index.php?showuser=$userId", router)
    }

    fun openChat() {
        currentData?.let {
            Log.e("kokosina", "openChat")
            router.replaceScreen(Screen.QmsChat().apply {
                userId = it.userId
                userNick = it.nick
                avatarUrl = this@QmsThemesPresenter.avatarUrl
            })
        }
    }

    fun createNote() {
        currentData?.let {
            val url = "https://4pda.ru/forum/index.php?act=qms&mid=${it.userId}"
            viewState.showCreateNote(it.nick.orEmpty(), url)
        }
    }

    fun createThemeNote(item: QmsTheme) {
        currentData?.let {
            val url = "https://4pda.ru/forum/index.php?act=qms&mid=${it.userId}&t=${item.userId}"
            viewState.showCreateNote(item.name.orEmpty(), it.nick.orEmpty(), url)
        }
    }

    fun onItemClick(item: QmsTheme) {
        currentData?.let {
            router.navigateTo(Screen.QmsChat().apply {
                screenTitle = item.name
                screenSubTitle = it.nick
                userId = it.userId
                avatarUrl = this@QmsThemesPresenter.avatarUrl
                themeId = item.id
                themeTitle = item.name
            })
        }
    }

    fun onItemLongClick(item: QmsTheme) {
        viewState.showItemDialogMenu(item)
    }

}
