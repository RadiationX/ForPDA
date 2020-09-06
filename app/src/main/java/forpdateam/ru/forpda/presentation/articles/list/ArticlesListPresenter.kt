package forpdateam.ru.forpda.presentation.articles.list

import android.util.Log
import com.arellomobile.mvp.InjectViewState
import forpdateam.ru.forpda.common.Utils
import forpdateam.ru.forpda.common.mvp.BasePresenter
import forpdateam.ru.forpda.entity.remote.news.NewsItem
import forpdateam.ru.forpda.model.AuthHolder
import forpdateam.ru.forpda.model.SchedulersProvider
import forpdateam.ru.forpda.model.data.remote.api.news.Constants
import forpdateam.ru.forpda.model.repository.avatar.AvatarRepository
import forpdateam.ru.forpda.model.repository.news.NewsRepository
import forpdateam.ru.forpda.presentation.IErrorHandler
import forpdateam.ru.forpda.presentation.ILinkHandler
import forpdateam.ru.forpda.presentation.Screen
import forpdateam.ru.forpda.presentation.TabRouter
import io.reactivex.Observable

/**
 * Created by radiationx on 11.11.17.
 */

@InjectViewState
class ArticlesListPresenter(
        private val newsRepository: NewsRepository,
        private val avatarRepository: AvatarRepository,
        private val authHolder: AuthHolder,
        private val router: TabRouter,
        private val linkHandler: ILinkHandler,
        private val errorHandler: IErrorHandler,
        private val schedulers: SchedulersProvider
) : BasePresenter<ArticlesListView>() {
    private val category = Constants.NEWS_CATEGORY_ROOT
    private var currentPage = 1

    private val currentItems = mutableListOf<NewsItem>()
    private val avatarsData = mutableListOf<Pair<Int, String>>()

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        refreshArticles()
    }

    private fun loadArticles(page: Int, withClear: Boolean) {
        currentPage = page
        newsRepository
                .getNews(category, currentPage)
                .doOnSubscribe { viewState.setRefreshing(true) }
                .doAfterTerminate { viewState.setRefreshing(false) }
                .subscribe({
                    if (withClear) {
                        currentItems.clear()
                    }
                    currentItems.addAll(it)
                    viewState.showNews(it, withClear)
                    loadAvatars(it)
                }, {
                    errorHandler.handle(it)
                })
                .untilDestroy()
    }

    private fun loadAvatars(items: List<NewsItem>) {
        if (!authHolder.get().isAuth()) {
            return
        }
        val newAvatarsData = mutableListOf<Pair<Int, String>>()
        items.forEach { item ->
            if (avatarsData.firstOrNull { it.first == item.authorId } == null) {
                Pair(item.authorId, item.author.orEmpty()).also {
                    avatarsData.add(it)
                    newAvatarsData.add(it)
                }
            }
        }
        newAvatarsData.forEach {
            Log.e("kekosina", "newAvatarsData ${it.first} ${it.second}")
        }
        Observable
                .fromIterable(newAvatarsData)
                .flatMapSingle { avatarData ->
                    avatarRepository
                            .getAvatar(avatarData.second)
                            .map { Pair(avatarData, it as String?) }
                            .onErrorReturnItem(Pair(avatarData, null as String?))
                }
                .subscribeOn(schedulers.io())
                .observeOn(schedulers.ui())
                .subscribe({ loaded ->
                    val updItems = currentItems
                            .filter { it.authorId == loaded.first.first && it.avatar != loaded.second }
                    updItems.forEach {
                        it.avatar = loaded.second
                    }
                    viewState.updateItems(updItems)
                }, {
                    errorHandler.handle(it)
                })
                .untilDestroy()

    }

    fun refreshArticles() {
        loadArticles(1, true)
    }

    fun loadMore() {
        loadArticles(currentPage + 1, false)
    }

    fun onItemClick(item: NewsItem) {
        router.navigateTo(Screen.ArticleDetail().apply {
            articleId = item.id
            articleTitle = item.title
            articleAuthorNick = item.author
            articleDate = item.date
            articleImageUrl = item.imgUrl
            articleCommentsCount = item.commentsCount
        })
    }

    fun onItemLongClick(item: NewsItem) {
        viewState.showItemDialogMenu(item)
    }

    fun copyLink(item: NewsItem) {
        Utils.copyToClipBoard("https://4pda.ru/index.php?p=${item.id}")
    }

    fun shareLink(item: NewsItem) {
        Utils.shareText("https://4pda.ru/index.php?p=${item.id}")
    }

    fun openProfile(item: NewsItem) {
        linkHandler.handle("https://4pda.ru/forum/index.php?showuser=${item.authorId}", router)
    }

    fun createNote(item: NewsItem) {
        val url = "https://4pda.ru/index.php?p=${item.id}"
        viewState.showCreateNote(item.title.orEmpty(), url)
    }

    fun openSearch() {
        router.navigateTo(Screen.Search().apply {
            searchUrl = "https://4pda.ru/?s="
        })
    }
}
