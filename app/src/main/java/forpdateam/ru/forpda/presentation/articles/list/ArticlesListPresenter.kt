package forpdateam.ru.forpda.presentation.articles.list

import android.util.Log
import forpdateam.ru.forpda.common.Utils
import forpdateam.ru.forpda.common.mvp.BasePresenter
import forpdateam.ru.forpda.entity.asDeferredData
import forpdateam.ru.forpda.entity.remote.news.NewsItem
import forpdateam.ru.forpda.extensions.coRunCatching
import forpdateam.ru.forpda.extensions.replace
import forpdateam.ru.forpda.model.AuthHolder
import forpdateam.ru.forpda.model.repository.avatar.AvatarRepository
import forpdateam.ru.forpda.model.repository.news.NewsRepository
import forpdateam.ru.forpda.presentation.ErrorHandler
import forpdateam.ru.forpda.presentation.LinkHandler
import forpdateam.ru.forpda.presentation.Screen
import forpdateam.ru.forpda.presentation.TabRouter
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import moxy.InjectViewState
import ru.radiationx.coretypes.ArticleId
import ru.radiationx.links.Link

/**
 * Created by radiationx on 11.11.17.
 */

@InjectViewState
class ArticlesListPresenter(
    private val newsRepository: NewsRepository,
    private val avatarRepository: AvatarRepository,
    private val authHolder: AuthHolder,
    private val router: TabRouter,
    private val linkHandler: LinkHandler,
    private val errorHandler: ErrorHandler,
    private val utils: Utils
) : BasePresenter<ArticlesListView>() {
    private var currentPage = 1

    private val currentItems = mutableListOf<NewsItem>()
    private val avatarsData = mutableListOf<NewsUser>()

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        refreshArticles()
    }

    private fun loadArticles(page: Int, withClear: Boolean) {
        currentPage = page
        viewModelScope.launch {
            viewState.setRefreshing(true)
            coRunCatching {
                newsRepository.getNews(currentPage)
            }.onSuccess {
                if (withClear) {
                    currentItems.clear()
                }
                currentItems.addAll(it)
                viewState.showNews(it, withClear)
                loadAvatars(it)
            }.onFailure {
                errorHandler.handle(it)
            }
            viewState.setRefreshing(false)
        }
    }

    private fun loadAvatars(items: List<NewsItem>) {
        if (!authHolder.get().isAuth()) {
            return
        }
        val newsUsers = mutableListOf<NewsUser>()
        items.forEach { item ->
            if (avatarsData.firstOrNull { it.id == item.authorId } == null) {
                NewsUser(item.authorId, item.author, null).also {
                    avatarsData.add(it)
                    newsUsers.add(it)
                }
            }
        }
        newsUsers.forEach {
            Log.e("kekosina", "newAvatarsData ${it.id} ${it.nick}")
        }
        viewModelScope.launch {
            val loadedAvatars = newsUsers.map { newsUser ->
                async {
                    val avatarUrl = coRunCatching {
                        avatarRepository.getAvatar(newsUser.id, newsUser.nick)
                    }.getOrNull()
                    newsUser.copy(avatarUrl = avatarUrl)
                }
            }.awaitAll()

            val updItems = currentItems.toMutableList()
            loadedAvatars.forEach { loaded ->
                updItems.replace(
                    condition = { it.authorId == loaded.id && it.avatar?.value != loaded.avatarUrl },
                    map = { it.copy(avatar = loaded.avatarUrl?.asDeferredData()) }
                )
            }

            currentItems.clear()
            currentItems.addAll(updItems)
            viewState.updateItems(currentItems)
        }
    }

    fun refreshArticles() {
        loadArticles(1, true)
    }

    fun loadMore() {
        loadArticles(currentPage + 1, false)
    }

    fun onItemClick(item: NewsItem) {
        router.navigateTo(
            Screen.ArticleDetail.FromList(
                articleId = ArticleId(item.id),
                title = item.title,
                authorNick = item.author,
                date = item.date,
                imageUrl = item.imgUrl,
                commentsCount = item.commentsCount,
            )
        )
    }

    fun onItemLongClick(item: NewsItem) {
        viewState.showItemDialogMenu(item)
    }

    fun copyLink(item: NewsItem) {
        utils.copyToClipBoard("https://4pda.to/index.php?p=${item.id}")
    }

    fun shareLink(item: NewsItem) {
        utils.shareText("https://4pda.to/index.php?p=${item.id}")
    }

    fun openProfile(item: NewsItem) {
        linkHandler.handle("https://4pda.to/forum/index.php?showuser=${item.authorId}")
    }

    fun createNote(item: NewsItem) {
        val url = "https://4pda.to/index.php?p=${item.id}"
        viewState.showCreateNote(item.title.orEmpty(), url)
    }

    fun openSearch() {
        router.navigateTo(
            Screen.Search.Site(link = Link.Site.Search.default)
        )
    }

    private data class NewsUser(
        val id: Int,
        val nick: String,
        val avatarUrl: String?
    )
}
