package forpdateam.ru.forpda.presentation.forum

import forpdateam.ru.forpda.common.Utils
import forpdateam.ru.forpda.common.mvp.BasePresenter
import forpdateam.ru.forpda.entity.remote.forum.ForumItemFlat
import forpdateam.ru.forpda.extensions.coRunCatching
import forpdateam.ru.forpda.model.data.remote.api.favorites.FavoritesApi
import forpdateam.ru.forpda.model.repository.faviorites.FavoritesRepository
import forpdateam.ru.forpda.model.repository.forum.ForumRepository
import forpdateam.ru.forpda.presentation.ErrorHandler
import forpdateam.ru.forpda.presentation.Screen
import forpdateam.ru.forpda.presentation.TabRouter
import kotlinx.coroutines.launch
import moxy.InjectViewState
import ru.radiationx.coretypes.ForumId
import ru.radiationx.links.Link
import ru.radiationx.quill.QuillExtra

/**
 * Created by radiationx on 03.01.18.
 */

data class ForumExtra(
    val forumId: ForumId?
): QuillExtra

@InjectViewState
class ForumPresenter(
    private val argExtra: ForumExtra,
    private val forumRepository: ForumRepository,
    private val favoritesRepository: FavoritesRepository,
    private val router: TabRouter,
    private val errorHandler: ErrorHandler,
    private val utils: Utils
) : BasePresenter<ForumView>() {

    private var targetForumId: ForumId? = argExtra.forumId

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        getCacheForums()
    }

    fun loadForums() {
        viewModelScope.launch {
            viewState.setRefreshing(true)
            coRunCatching {
                forumRepository.getForums()
            }.onSuccess {
                viewState.showForums(it)
                scrollToTarget()
            }.onFailure {
                errorHandler.handle(it)
            }
            viewState.setRefreshing(false)
        }
    }

    private fun getCacheForums() {
        viewModelScope.launch {
            viewState.setRefreshing(true)
            coRunCatching {
                forumRepository.getCache()
            }.onSuccess {
                if (it.isNotEmpty()) {
                    viewState.showForums(it)
                    scrollToTarget()
                }
            }.onFailure {
                errorHandler.handle(it)
            }
            viewState.setRefreshing(false)
            loadForums()
        }
    }

    private fun scrollToTarget() {
        val forumId = targetForumId ?: return
        viewState.scrollToForum(forumId.id)
        targetForumId = null
    }

    fun markRead(id: Int) {
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

    fun markAllRead() {
        viewModelScope.launch {
            coRunCatching {
                forumRepository.markAllRead()
            }.onSuccess {
                viewState.onMarkAllRead()
            }.onFailure {
                errorHandler.handle(it)
            }
        }
    }

    fun addToFavorite(forumId: Int, subType: String) {
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

    fun copyLink(item: ForumItemFlat) {
        utils.copyToClipBoard("https://4pda.to/forum/index.php?showforum=${item.id}")
    }

    fun navigateToForum(item: ForumItemFlat) {
        router.navigateTo(Screen.Topics(link = Link.Board.Forum.default(ForumId(item.id))))
    }

    fun navigateToSearch(item: ForumItemFlat) {
        val link = Link.Board.Search.default.copy(forums = setOf(Link.Board.Search.Forum.Id(forumId = ForumId(item.id))))
        router.navigateTo(Screen.Search.Forum(link = link))
    }
}
