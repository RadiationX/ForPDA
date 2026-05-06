package forpdateam.ru.forpda.presentation.forum

import forpdateam.ru.forpda.common.Utils
import forpdateam.ru.forpda.common.mvp.BasePresenter
import forpdateam.ru.forpda.entity.remote.forum.ForumItemTree
import forpdateam.ru.forpda.extensions.coRunCatching
import forpdateam.ru.forpda.model.data.remote.api.favorites.FavoritesApi
import forpdateam.ru.forpda.model.repository.faviorites.FavoritesRepository
import forpdateam.ru.forpda.model.repository.forum.ForumRepository
import forpdateam.ru.forpda.presentation.IErrorHandler
import forpdateam.ru.forpda.presentation.Screen
import forpdateam.ru.forpda.presentation.TabRouter
import kotlinx.coroutines.launch
import moxy.InjectViewState

/**
 * Created by radiationx on 03.01.18.
 */

@InjectViewState
class ForumPresenter(
    private val forumRepository: ForumRepository,
    private val favoritesRepository: FavoritesRepository,
    private val router: TabRouter,
    private val errorHandler: IErrorHandler
) : BasePresenter<ForumView>() {

    var targetForumId = -1

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        getCacheForums()
        loadForums()
    }

    fun loadForums() {
        viewModelScope.launch {
            viewState.setRefreshing(true)
            coRunCatching {
                forumRepository.getForums()
            }.onSuccess {
                viewState.showForums(it)
                scrollToTarget()
                saveCacheForums(it)
            }.onFailure {
                errorHandler.handle(it)
            }
            viewState.setRefreshing(false)
        }
    }

    private fun getCacheForums() {
        viewModelScope.launch {
            coRunCatching {
                forumRepository.getCache()
            }.onSuccess {
                if (it.forums.isEmpty()) {
                    loadForums()
                } else {
                    viewState.showForums(it)
                    scrollToTarget()
                }
            }.onFailure {
                errorHandler.handle(it)
            }
        }
    }

    private fun scrollToTarget() {
        if (targetForumId != -1) {
            viewState.scrollToForum(targetForumId)
            targetForumId = -1
        }
    }

    private fun saveCacheForums(rootForum: ForumItemTree) {
        viewModelScope.launch {
            coRunCatching {
                forumRepository.saveCache(rootForum)
            }.onFailure {
                errorHandler.handle(it)
            }
        }
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

    fun copyLink(item: ForumItemTree) {
        Utils.copyToClipBoard("https://4pda.to/forum/index.php?showforum=${item.item.id}")
    }

    fun navigateToForum(item: ForumItemTree) {
        router.navigateTo(Screen.Topics().apply {
            forumId = item.item.id
        })
    }

    fun navigateToSearch(item: ForumItemTree) {
        router.navigateTo(Screen.Search().apply {
            searchUrl =
                "https://4pda.to/forum/index.php?act=search&source=all&forums%5B%5D=${item.item.id}"
        })
    }
}
