package forpdateam.ru.forpda.presentation.topics

import forpdateam.ru.forpda.common.Utils
import forpdateam.ru.forpda.common.mvp.BasePresenter
import forpdateam.ru.forpda.entity.remote.favorites.FavoriteAction
import forpdateam.ru.forpda.entity.remote.topics.TopicItem
import forpdateam.ru.forpda.entity.remote.topics.TopicsData
import forpdateam.ru.forpda.extensions.coRunCatching
import forpdateam.ru.forpda.extensions.replace
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
import ru.radiationx.coretypes.ForumId
import ru.radiationx.coretypes.PageOffset
import ru.radiationx.coretypes.TopicId
import ru.radiationx.links.Link
import ru.radiationx.quill.QuillExtra

/**
 * Created by radiationx on 03.01.18.
 */
data class TopicsExtra(
    val link: Link.Board.Forum
) : QuillExtra

@InjectViewState
class TopicsPresenter(
    private val argExtra: TopicsExtra,
    private val topicsRepository: TopicsRepository,
    private val forumRepository: ForumRepository,
    private val favoritesRepository: FavoritesRepository,
    private val crossScreenInteractor: CrossScreenInteractor,
    private val router: TabRouter,
    private val linkHandler: LinkHandler,
    private val errorHandler: ErrorHandler,
    private val utils: Utils
) : BasePresenter<TopicsView>() {

    private var pageOffset = argExtra.link.offset
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
                topicsRepository.getTopics(argExtra.link.forumId, pageOffset)
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
        pageOffset = PageOffset(st)
        loadTopics()
    }

    fun addForumToFavorite(forumId: ForumId, subType: String) {
        viewModelScope.launch {
            coRunCatching {
                favoritesRepository.editFavorites(FavoriteAction.AddForum(forumId, subType))
            }.onSuccess {
                viewState.onAddToFavorite(it)
            }.onFailure {
                errorHandler.handle(it)
            }
        }
    }

    fun addTopicToFavorite(topicId: TopicId, subType: String) {
        viewModelScope.launch {
            coRunCatching {
                favoritesRepository.editFavorites(FavoriteAction.AddTopic(topicId, subType))
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
                forumRepository.markRead(argExtra.link.forumId)
            }.onSuccess {
                viewState.onMarkRead()
            }.onFailure {
                errorHandler.handle(it)
            }
        }
    }

    private fun markRead(id: TopicId) {
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
        router.navigateTo(Screen.Forum(forumId = argExtra.link.forumId))
    }

    fun openSearch() {
        val link = Link.Board.Search.default.copy(forums = setOf(Link.Board.Search.Forum.Id(argExtra.link.forumId)))
        router.navigateTo(Screen.Search.Forum(link))
    }

    fun openTopicForum() {
        currentData?.let {
            linkHandler.handle("https://4pda.to/forum/index.php?showforum=${it.id.id}")
        }
    }

    fun onItemClick(item: TopicItem) {
        when (item) {
            is TopicItem.Announce -> {
                linkHandler.handle(getItemLink(item), mapOf(Screen.ARG_TITLE to item.title))
            }

            is TopicItem.Forum -> {
                linkHandler.handle(getItemLink(item))
            }

            is TopicItem.Topic -> {
                linkHandler.handle(getItemLink(item), mapOf(Screen.ARG_TITLE to item.title))
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
            is TopicItem.Forum -> "https://4pda.to/forum/index.php?showforum=${item.id.id}"
            is TopicItem.Topic -> "https://4pda.to/forum/index.php?showtopic=${item.id.id}"
        }
    }
}
