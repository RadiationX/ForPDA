package forpdateam.ru.forpda.presentation.favorites

import android.util.Log
import moxy.InjectViewState
import forpdateam.ru.forpda.common.Utils
import forpdateam.ru.forpda.common.mvp.BasePresenter
import forpdateam.ru.forpda.entity.app.TabNotification
import forpdateam.ru.forpda.entity.remote.favorites.FavItem
import forpdateam.ru.forpda.model.CountersHolder
import forpdateam.ru.forpda.model.data.remote.api.favorites.Sorting
import forpdateam.ru.forpda.model.interactors.CrossScreenInteractor
import forpdateam.ru.forpda.model.repository.events.EventsRepository
import forpdateam.ru.forpda.model.repository.faviorites.FavoritesRepository
import forpdateam.ru.forpda.model.repository.forum.ForumRepository
import forpdateam.ru.forpda.model.preferences.ListsPreferencesHolder
import forpdateam.ru.forpda.model.preferences.NotificationPreferencesHolder
import forpdateam.ru.forpda.presentation.IErrorHandler
import forpdateam.ru.forpda.presentation.ILinkHandler
import forpdateam.ru.forpda.presentation.Screen
import forpdateam.ru.forpda.presentation.TabRouter

/**
 * Created by radiationx on 11.11.17.
 */

@InjectViewState
class FavoritesPresenter(
        private val favoritesRepository: FavoritesRepository,
        private val forumRepository: ForumRepository,
        private val eventsRepository: EventsRepository,
        private val listsPreferencesHolder: ListsPreferencesHolder,
        private val notificationPreferencesHolder: NotificationPreferencesHolder,
        private val crossScreenInteractor: CrossScreenInteractor,
        private val router: TabRouter,
        private val linkHandler: ILinkHandler,
        private val countersHolder: CountersHolder,
        private val errorHandler: IErrorHandler
) : BasePresenter<FavoritesView>() {


    private var currentSt = 0
    private var loadAll = listsPreferencesHolder.getFavLoadAll()
    private var sorting: Sorting = Sorting(
            listsPreferencesHolder.getSortingKey(),
            listsPreferencesHolder.getSortingOrder()
    )

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()

        viewState.initSorting(sorting)

        listsPreferencesHolder
                .observeFavLoadAll()
                .subscribe { loadAll = it }
                .untilDestroy()

        listsPreferencesHolder
                .observeShowDot()
                .subscribe {
                    viewState.setShowDot(it)
                }
                .untilDestroy()

        listsPreferencesHolder
                .observeUnreadTop()
                .subscribe {
                    viewState.setUnreadTop(it)
                }
                .untilDestroy()

        eventsRepository
                .observeEventsTab()
                .subscribe {
                    Log.e("testtabnotify", "fav observeEventsTab $it")
                    handleEvent(it)
                }
                .untilDestroy()

        favoritesRepository
                .observeItems()
                .subscribe({
                    Log.d("kokos", "observeContacts ${it.size} ${it.joinToString("; "){"${it.topicId}:${it.isNew}"}}")
                    viewState.onShowFavorite(it)
                }, {
                    errorHandler.handle(it)
                })
                .untilDestroy()

        favoritesRepository
                .loadCache()
                .subscribe({
                    viewState.onShowFavorite(it)
                }, {
                    errorHandler.handle(it)
                })
                .untilDestroy()

        crossScreenInteractor
                .observeTopic()
                .subscribe {
                    markRead(it)
                }
                .untilDestroy()
    }

    fun updateSorting(key: String, order: String) {
        sorting.also {
            it.key = key
            it.order = order
        }
        listsPreferencesHolder.setSortingKey(key)
        listsPreferencesHolder.setSortingOrder(order)
        loadFavorites(currentSt)
    }

    fun refresh() {
        loadFavorites(0)
    }

    fun loadFavorites(pageNum: Int) {
        currentSt = pageNum
        favoritesRepository
                .loadFavorites(currentSt, loadAll, sorting)
                .doOnSubscribe { viewState.setRefreshing(true) }
                .doAfterTerminate { viewState.setRefreshing(false) }
                .subscribe({
                    viewState.onLoadFavorites(it)
                }, {
                    errorHandler.handle(it)
                })
                .untilDestroy()
    }

    private fun markRead(topicId: Int) {
        favoritesRepository
                .markRead(topicId)
                .subscribe({
                }, {
                    errorHandler.handle(it)
                })
                .untilDestroy()
    }

    private fun handleEvent(event: TabNotification) {
        favoritesRepository
                .handleEvent(event)
                .subscribe({
                    Log.e("testtabnotify", "fav handleEvent $it")
                }, {
                    errorHandler.handle(it)
                })
                .untilDestroy()
    }

    fun markAllRead() {
        forumRepository
                .markAllRead()
                .subscribe({
                    viewState.onMarkAllRead()
                }, {
                    errorHandler.handle(it)
                })
                .untilDestroy()
    }

    fun onItemClick(item: FavItem) {
        val args = mapOf<String, String>(
                Screen.ARG_TITLE to item.topicTitle.orEmpty()
        )
        if (item.isForum) {
            linkHandler.handle("https://4pda.to/forum/index.php?showforum=" + item.forumId, router, args)
        } else {
            linkHandler.handle("https://4pda.to/forum/index.php?showtopic=" + item.topicId + "&view=getnewpost", router, args)
        }
    }

    fun onItemLongClick(item: FavItem) {
        viewState.showItemDialogMenu(item)
    }

    fun copyLink(item: FavItem) {
        if (item.isForum) {
            Utils.copyToClipBoard("https://4pda.to/forum/index.php?showforum=" + Integer.toString(item.forumId))
        } else {
            Utils.copyToClipBoard("https://4pda.to/forum/index.php?showtopic=" + Integer.toString(item.topicId))
        }
    }

    fun openAttachments(item: FavItem) {
        linkHandler.handle("https://4pda.to/forum/index.php?act=attach&code=showtopic&tid=" + item.topicId, router)
    }

    fun openForum(item: FavItem) {
        linkHandler.handle("https://4pda.to/forum/index.php?showforum=" + item.forumId, router)
    }

    fun changeFav(action: Int, type: String?, favId: Int) {
        favoritesRepository
                .editFavorites(action, favId, favId, type)
                .subscribe({
                    viewState.onChangeFav(it)
                    loadFavorites(currentSt)
                }, {
                    errorHandler.handle(it)
                })
                .untilDestroy()
    }

    fun showSubscribeDialog(item: FavItem) {
        viewState.showSubscribeDialog(item)
    }
}
