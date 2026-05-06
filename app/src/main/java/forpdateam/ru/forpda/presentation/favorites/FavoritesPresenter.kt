package forpdateam.ru.forpda.presentation.favorites

import android.util.Log
import forpdateam.ru.forpda.common.Utils
import forpdateam.ru.forpda.common.mvp.BasePresenter
import forpdateam.ru.forpda.entity.app.TabNotification
import forpdateam.ru.forpda.entity.remote.favorites.FavItem
import forpdateam.ru.forpda.extensions.coRunCatching
import forpdateam.ru.forpda.model.CountersHolder
import forpdateam.ru.forpda.model.data.remote.api.favorites.Sorting
import forpdateam.ru.forpda.model.interactors.CrossScreenInteractor
import forpdateam.ru.forpda.model.preferences.ListsPreferencesHolder
import forpdateam.ru.forpda.model.preferences.NotificationPreferencesHolder
import forpdateam.ru.forpda.model.repository.events.EventsRepository
import forpdateam.ru.forpda.model.repository.faviorites.FavoritesRepository
import forpdateam.ru.forpda.model.repository.forum.ForumRepository
import forpdateam.ru.forpda.presentation.IErrorHandler
import forpdateam.ru.forpda.presentation.ILinkHandler
import forpdateam.ru.forpda.presentation.Screen
import forpdateam.ru.forpda.presentation.TabRouter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import moxy.InjectViewState

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
        listsPreferencesHolder.getSortingKey().orEmpty(),
        listsPreferencesHolder.getSortingOrder().orEmpty()
    )

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()

        viewState.initSorting(sorting)

        listsPreferencesHolder
            .observeFavLoadAll()
            .onEach { loadAll = it }
            .launchIn(viewModelScope)

        listsPreferencesHolder
            .observeShowDot()
            .onEach {
                viewState.setShowDot(it)
            }
            .launchIn(viewModelScope)

        listsPreferencesHolder
            .observeUnreadTop()
            .onEach {
                viewState.setUnreadTop(it)
            }
            .launchIn(viewModelScope)

        eventsRepository
            .observeEventsTab()
            .onEach {
                Log.e("testtabnotify", "fav observeEventsTab $it")
                handleEvent(it)
            }
            .launchIn(viewModelScope)

        favoritesRepository
            .observeItems()
            .onEach {
                Log.d(
                    "kokos",
                    "observeContacts ${it.size} ${it.joinToString("; ") { "${it.topicId}:${it.isNew}" }}"
                )
                viewState.onShowFavorite(it)
            }
            .launchIn(viewModelScope)

        crossScreenInteractor
            .observeTopic()
            .onEach {
                markRead(it)
            }
            .launchIn(viewModelScope)
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
        viewModelScope.launch {
            viewState.setRefreshing(true)
            coRunCatching {
                favoritesRepository.loadFavorites(currentSt, loadAll, sorting)
            }.onSuccess {
                viewState.onLoadFavorites(it)
            }.onFailure {
                errorHandler.handle(it)
            }
            viewState.setRefreshing(false)
        }
    }

    private fun markRead(topicId: Int) {
        viewModelScope.launch {
            coRunCatching {
                favoritesRepository.markRead(topicId)
            }.onFailure {
                errorHandler.handle(it)
            }
        }
    }

    private fun handleEvent(event: TabNotification) {
        viewModelScope.launch {
            coRunCatching {
                favoritesRepository.handleEvent(event)
            }.onSuccess {
                Log.e("testtabnotify", "fav handleEvent $it")
            }.onFailure {
                errorHandler.handle(it)
            }
        }
    }

    fun onItemClick(item: FavItem) {
        val args = mapOf<String, String>(
            Screen.ARG_TITLE to item.topicTitle.orEmpty()
        )
        if (item.isForum) {
            linkHandler.handle(
                "https://4pda.to/forum/index.php?showforum=" + item.forumId,
                router,
                args
            )
        } else {
            linkHandler.handle(
                "https://4pda.to/forum/index.php?showtopic=" + item.topicId + "&view=getnewpost",
                router,
                args
            )
        }
    }

    fun onItemLongClick(item: FavItem) {
        viewState.showItemDialogMenu(item)
    }

    fun copyLink(item: FavItem) {
        if (item.isForum) {
            Utils.copyToClipBoard(
                "https://4pda.to/forum/index.php?showforum=" + Integer.toString(
                    item.forumId
                )
            )
        } else {
            Utils.copyToClipBoard(
                "https://4pda.to/forum/index.php?showtopic=" + Integer.toString(
                    item.topicId
                )
            )
        }
    }

    fun openAttachments(item: FavItem) {
        linkHandler.handle(
            "https://4pda.to/forum/index.php?act=attach&code=showtopic&tid=" + item.topicId,
            router
        )
    }

    fun openForum(item: FavItem) {
        linkHandler.handle("https://4pda.to/forum/index.php?showforum=" + item.forumId, router)
    }

    fun changeFav(action: Int, type: String?, favId: Int) {
        viewModelScope.launch {
            coRunCatching {
                favoritesRepository.editFavorites(action, favId, favId, type)
            }.onSuccess {
                viewState.onChangeFav(it)
                loadFavorites(currentSt)
            }.onFailure {
                errorHandler.handle(it)
            }
        }
    }

    fun showSubscribeDialog(item: FavItem) {
        viewState.showSubscribeDialog(item)
    }
}
