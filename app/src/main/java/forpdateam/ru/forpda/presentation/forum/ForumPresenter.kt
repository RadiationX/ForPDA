package forpdateam.ru.forpda.presentation.forum

import moxy.InjectViewState
import forpdateam.ru.forpda.common.Utils
import forpdateam.ru.forpda.common.mvp.BasePresenter
import forpdateam.ru.forpda.entity.remote.forum.ForumItemTree
import forpdateam.ru.forpda.model.data.remote.api.favorites.FavoritesApi
import forpdateam.ru.forpda.model.repository.faviorites.FavoritesRepository
import forpdateam.ru.forpda.model.repository.forum.ForumRepository
import forpdateam.ru.forpda.presentation.IErrorHandler
import forpdateam.ru.forpda.presentation.Screen
import forpdateam.ru.forpda.presentation.TabRouter

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

    var targetForumId = -1;

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        getCacheForums()
        loadForums()
    }

    fun loadForums() {
        forumRepository
                .getForums()
                .doOnSubscribe { viewState.setRefreshing(true) }
                .doAfterTerminate { viewState.setRefreshing(false) }
                .subscribe({
                    viewState.showForums(it)
                    scrollToTarget()
                    saveCacheForums(it)
                }, {
                    errorHandler.handle(it)
                })
                .untilDestroy()
    }

    private fun getCacheForums() {
        forumRepository
                .getCache()
                .doOnSubscribe { viewState.setRefreshing(true) }
                .doAfterTerminate { viewState.setRefreshing(false) }
                .subscribe({ it ->
                    if (it.forums == null) {
                        loadForums()
                    } else {
                        viewState.showForums(it)
                        scrollToTarget()
                    }
                }, {
                    errorHandler.handle(it)
                })
                .untilDestroy()
    }

    private fun scrollToTarget() {
        if (targetForumId != -1) {
            viewState.scrollToForum(targetForumId)
            targetForumId = -1
        }
    }

    private fun saveCacheForums(rootForum: ForumItemTree) {
        forumRepository
                .saveCache(rootForum)
                .doOnTerminate { viewState.setRefreshing(true) }
                .doAfterTerminate { viewState.setRefreshing(false) }
                .subscribe({

                }, {
                    errorHandler.handle(it)
                })
                .untilDestroy()
    }

    fun markRead(id: Int) {
        forumRepository
                .markRead(id)
                .subscribe({
                    viewState.onMarkRead()
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

    fun addToFavorite(forumId: Int, subType: String) {
        favoritesRepository
                .editFavorites(FavoritesApi.ACTION_ADD_FORUM, -1, forumId, subType)
                .subscribe({
                    viewState.onAddToFavorite(it)
                }, {
                    errorHandler.handle(it)
                })
                .untilDestroy()
    }

    fun copyLink(item: ForumItemTree) {
        Utils.copyToClipBoard("https://4pda.ru/forum/index.php?showforum=${item.id}")
    }

    fun navigateToForum(item: ForumItemTree) {
        router.navigateTo(Screen.Topics().apply {
            forumId = item.id
        })
    }

    fun navigateToSearch(item: ForumItemTree) {
        router.navigateTo(Screen.Search().apply {
            searchUrl = "https://4pda.ru/forum/index.php?act=search&source=all&forums%5B%5D=${item.id}"
        })
    }
}
