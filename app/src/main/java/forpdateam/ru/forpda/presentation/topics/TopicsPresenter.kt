package forpdateam.ru.forpda.presentation.topics

import forpdateam.ru.forpda.common.Utils
import forpdateam.ru.forpda.common.mvp.BasePresenter
import forpdateam.ru.forpda.entity.remote.topics.TopicItem
import forpdateam.ru.forpda.entity.remote.topics.TopicsData
import forpdateam.ru.forpda.extensions.coRunCatching
import forpdateam.ru.forpda.extensions.replace
import forpdateam.ru.forpda.model.data.remote.api.favorites.FavoritesApi
import forpdateam.ru.forpda.model.interactors.CrossScreenInteractor
import forpdateam.ru.forpda.model.repository.faviorites.FavoritesRepository
import forpdateam.ru.forpda.model.repository.forum.ForumRepository
import forpdateam.ru.forpda.model.repository.topics.TopicsRepository
import forpdateam.ru.forpda.presentation.ErrorHandler
import forpdateam.ru.forpda.presentation.LinkHandler
import forpdateam.ru.forpda.presentation.Screen
import forpdateam.ru.forpda.presentation.TabRouter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import moxy.InjectViewState

/**
 * Created by radiationx on 03.01.18.
 */

@InjectViewState
class TopicsPresenter(
    private val topicsRepository: TopicsRepository,
    private val forumRepository: ForumRepository,
    private val favoritesRepository: FavoritesRepository,
    private val crossScreenInteractor: CrossScreenInteractor,
    private val router: TabRouter,
    private val linkHandler: LinkHandler,
    private val errorHandler: ErrorHandler,
    private val utils: Utils
) : BasePresenter<TopicsView>() {

    var id = 0
    private var currentSt = 0
    var currentData: TopicsData? = null

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        crossScreenInteractor
            .observeTopic()
            .onEach {
                markRead(it)
            }
            .launchIn(viewModelScope)
        loadTopics()
    }

    fun loadTopics() {
        viewModelScope.launch {
            viewState.setRefreshing(true)
            coRunCatching {
                topicsRepository.getTopics(id, currentSt)
            }.onSuccess {
                currentData = it
                viewState.showTopics(it)
            }.onFailure {
                errorHandler.handle(it)
            }
            viewState.setRefreshing(false)
        }
    }

    fun loadPage(st: Int) {
        currentSt = st
        loadTopics()
    }

    fun addForumToFavorite(forumId: Int, subType: String) {
        viewModelScope.launch {
            coRunCatching {
                favoritesRepository.editFavorites(
                    FavoritesApi.ACTION_ADD_FORUM,
                    -1,
                    forumId,
                    subType
                )
            }.onSuccess {
                viewState.onAddToFavorite(it)
            }.onFailure {
                errorHandler.handle(it)
            }
        }
    }

    fun addTopicToFavorite(topicId: Int, subType: String) {
        viewModelScope.launch {
            coRunCatching {
                favoritesRepository.editFavorites(FavoritesApi.ACTION_ADD, -1, topicId, subType)
            }.onSuccess {
                viewState.onAddToFavorite(it)
            }.onFailure {
                errorHandler.handle(it)
            }
        }
    }

    fun markRead() {
        viewModelScope.launch {
            coRunCatching {
                forumRepository.markRead(id)
            }.onSuccess {
                viewState.onMarkRead()
            }.onFailure {
                errorHandler.handle(it)
            }
        }
    }

    private fun markRead(id: Int) {
        currentData?.also { data ->
            val newItems = data.topicItems.replace(
                condition = { it.id == id },
                map = { it.copy(flags = it.flags.copy(isNew = false)) }
            )
            currentData = data.copy(topicItems = newItems)
            viewState.updateList()
        }
    }

    fun openForum() {
        router.navigateTo(Screen.Forum().apply {
            forumId = id
        })
    }

    fun openSearch() {
        router.navigateTo(Screen.Search().apply {
            searchUrl = "https://4pda.to/forum/index.php?act=search&source=all&forums%5B%5D=$id"
        })
    }

    fun openTopicForum() {
        currentData?.let {
            linkHandler.handle("https://4pda.to/forum/index.php?showforum=${it.id}", router)
        }
    }

    fun onItemClick(item: TopicItem) {
        when (item) {
            is TopicItem.Announce -> {
                linkHandler.handle(getItemLink(item), router, mapOf(Screen.ARG_TITLE to item.title))
            }

            is TopicItem.Forum -> {
                linkHandler.handle(getItemLink(item), router)
            }

            is TopicItem.Topic -> {
                linkHandler.handle(getItemLink(item), router, mapOf(Screen.ARG_TITLE to item.title))
            }
        }
    }

    fun onItemLongClick(item: TopicItem) {
        viewState.showItemDialogMenu(item)
    }

    fun copyLink(item: TopicItem) {
        val link = getItemLink(item)
        utils.copyToClipBoard(link)
    }

    private fun getItemLink(item: TopicItem): String {
        return when (item) {
            is TopicItem.Announce -> item.url
            is TopicItem.Forum -> "https://4pda.to/forum/index.php?showforum=${item.id}"
            is TopicItem.Topic -> "https://4pda.to/forum/index.php?showtopic=${item.id}"
        }
    }
}
