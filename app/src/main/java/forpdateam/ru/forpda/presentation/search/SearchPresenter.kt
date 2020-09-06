package forpdateam.ru.forpda.presentation.search

import android.util.Log
import com.arellomobile.mvp.InjectViewState
import forpdateam.ru.forpda.App
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.common.Utils
import forpdateam.ru.forpda.common.mvp.BasePresenter
import forpdateam.ru.forpda.entity.remote.IBaseForumPost
import forpdateam.ru.forpda.entity.remote.search.SearchItem
import forpdateam.ru.forpda.entity.remote.search.SearchResult
import forpdateam.ru.forpda.entity.remote.search.SearchSettings
import forpdateam.ru.forpda.model.data.remote.api.favorites.FavoritesApi
import forpdateam.ru.forpda.model.repository.faviorites.FavoritesRepository
import forpdateam.ru.forpda.model.preferences.MainPreferencesHolder
import forpdateam.ru.forpda.model.preferences.OtherPreferencesHolder
import forpdateam.ru.forpda.model.preferences.TopicPreferencesHolder
import forpdateam.ru.forpda.model.repository.reputation.ReputationRepository
import forpdateam.ru.forpda.model.repository.search.SearchRepository
import forpdateam.ru.forpda.model.repository.theme.ThemeRepository
import forpdateam.ru.forpda.presentation.IErrorHandler
import forpdateam.ru.forpda.presentation.ILinkHandler
import forpdateam.ru.forpda.presentation.Screen
import forpdateam.ru.forpda.presentation.TabRouter
import forpdateam.ru.forpda.presentation.theme.IThemePresenter
import forpdateam.ru.forpda.ui.TemplateManager

@InjectViewState
class SearchPresenter(
        private val searchRepository: SearchRepository,
        private val favoritesRepository: FavoritesRepository,
        private val themeRepository: ThemeRepository,
        private val reputationRepository: ReputationRepository,
        private val topicPreferencesHolder: TopicPreferencesHolder,
        private val mainPreferencesHolder: MainPreferencesHolder,
        private val otherPreferencesHolder: OtherPreferencesHolder,
        private val searchTemplate: SearchTemplate,
        private val templateManager: TemplateManager,
        private val router: TabRouter,
        private val linkHandler: ILinkHandler,
        private val errorHandler: IErrorHandler
) : BasePresenter<SearchSiteView>(), IThemePresenter {

    companion object {
        const val FIELD_RESOURCE = "resource"
        const val FIELD_RESULT = "result"
        const val FIELD_SORT = "sort"
        const val FIELD_SOURCE = "source"
    }

    private val resourceItems = listOf<String>(SearchSettings.RESOURCE_FORUM.second, SearchSettings.RESOURCE_NEWS.second)
    private val resultItems = listOf<String>(SearchSettings.RESULT_TOPICS.second, SearchSettings.RESULT_POSTS.second)
    private val sortItems = listOf<String>(SearchSettings.SORT_DA.second, SearchSettings.SORT_DD.second, SearchSettings.SORT_REL.second)
    private val sourceItems = listOf<String>(SearchSettings.SOURCE_ALL.second, SearchSettings.SOURCE_TITLES.second, SearchSettings.SOURCE_CONTENT.second)

    private val fields = mapOf(
            FIELD_RESOURCE to resourceItems,
            FIELD_RESULT to resultItems,
            FIELD_SORT to sortItems,
            FIELD_SOURCE to sourceItems
    )

    private var settings = SearchSettings()

    private var currentData: SearchResult? = null

    init {
        initSearchSettings(otherPreferencesHolder.getSearchSettings())
    }

    fun initSearchSettings(url: String?) {
        url?.let {
            settings = SearchSettings.parseSettings(settings, it)
        }
    }

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()

        topicPreferencesHolder
                .observeShowAvatars()
                .subscribe {
                    viewState.updateShowAvatarState(it)
                }
                .untilDestroy()

        topicPreferencesHolder
                .observeCircleAvatars()
                .subscribe {
                    viewState.updateTypeAvatarState(it)
                }
                .untilDestroy()

        mainPreferencesHolder
                .observeScrollButtonEnabled()
                .subscribe {
                    viewState.updateScrollButtonState(it)
                }
                .untilDestroy()

        mainPreferencesHolder
                .observeWebViewFontSize()
                .subscribe {
                    viewState.setFontSize(it)
                }
                .untilDestroy()

        templateManager
                .observeThemeType()
                .subscribe {
                    viewState.setStyleType(it)
                }
                .untilDestroy()
        viewState.fillSettingsData(settings, fields)
        refreshData()
    }

    fun refreshData() {
        if (settings.query.isEmpty() && settings.nick.isEmpty()) {
            return
        }
        val withHtml = settings.result == SearchSettings.RESULT_POSTS.first && settings.resourceType.equals(SearchSettings.RESOURCE_FORUM.first)
        searchRepository
                .getSearch(settings)
                .map {
                    if (withHtml) searchTemplate.mapEntity(it) else it
                }
                .doOnSubscribe {
                    viewState.setRefreshing(true)
                    viewState.onStartSearch(settings)
                }
                .doAfterTerminate { viewState.setRefreshing(false) }
                .subscribe({
                    currentData = it
                    viewState.showData(it)
                }, {
                    errorHandler.handle(it)
                })
                .untilDestroy()
    }

    fun search(query: String, nick: String) {
        settings.st = 0
        settings.query = query
        settings.nick = nick
        refreshData()
    }

    fun search(pageNumber: Int) {
        settings.st = pageNumber
        refreshData()
    }

    fun updateSettings(field: String, position: Int) {
        when (field) {
            FIELD_RESOURCE -> {
                val name = resourceItems[position]
                when {
                    checkName(name, SearchSettings.RESOURCE_NEWS) -> {
                        settings.resourceType = SearchSettings.RESOURCE_NEWS.first
                        viewState.setNewsMode()
                    }
                    checkName(name, SearchSettings.RESOURCE_FORUM) -> {
                        settings.resourceType = SearchSettings.RESOURCE_FORUM.first
                        viewState.setForumMode()
                    }
                }
            }
            FIELD_RESULT -> {
                val name = resultItems[position]
                when {
                    checkName(name, SearchSettings.RESULT_TOPICS) -> settings.result = SearchSettings.RESULT_TOPICS.first
                    checkName(name, SearchSettings.RESULT_POSTS) -> settings.result = SearchSettings.RESULT_POSTS.first
                }
            }
            FIELD_SORT -> {
                val name = sortItems[position]
                when {
                    checkName(name, SearchSettings.SORT_DA) -> settings.sort = SearchSettings.SORT_DA.first
                    checkName(name, SearchSettings.SORT_DD) -> settings.sort = SearchSettings.SORT_DD.first
                    checkName(name, SearchSettings.SORT_REL) -> settings.sort = SearchSettings.SORT_REL.first
                }
            }
            FIELD_SOURCE -> {
                val name = sourceItems[position]
                when {
                    checkName(name, SearchSettings.SOURCE_ALL) -> settings.source = SearchSettings.SOURCE_ALL.first
                    checkName(name, SearchSettings.SOURCE_TITLES) -> settings.source = SearchSettings.SOURCE_TITLES.first
                    checkName(name, SearchSettings.SOURCE_CONTENT) -> settings.source = SearchSettings.SOURCE_CONTENT.first
                }
            }
        }
    }

    private fun checkName(arg: String, pair: Pair<String, String>): Boolean {
        return arg == pair.second
    }


    fun saveSettings() {
        val saveSettings = SearchSettings()
        saveSettings.resourceType = settings.resourceType
        saveSettings.result = settings.result
        saveSettings.sort = settings.sort
        saveSettings.source = settings.source
        val saveUrl = saveSettings.toUrl()
        otherPreferencesHolder.setSearchSettings(saveUrl)
    }

    fun onItemClick(item: SearchItem) {
        var url = ""
        if (settings.resourceType.equals(SearchSettings.RESOURCE_NEWS.first)) {
            url = "https://4pda.ru/index.php?p=${item.id}"
        } else {
            url = "https://4pda.ru/forum/index.php?showtopic=${item.topicId}"
            if (item.id != 0) {
                url += "&view=findpost&p=${item.id}"
            }
        }
        linkHandler.handle(url, router)
    }

    fun onItemLongClick(item: SearchItem) {
        viewState.showItemDialogMenu(item, settings)
    }

    fun copyLink() {
        Utils.copyToClipBoard(settings.toUrl())
    }

    fun copyLink(item: IBaseForumPost) {
        var url = ""
        if (settings.resourceType.equals(SearchSettings.RESOURCE_NEWS.first)) {
            url = "https://4pda.ru/index.php?p=${item.id}"
        } else {
            url = "https://4pda.ru/forum/index.php?showtopic=${item.topicId}"
            if (item.id != 0) {
                url += "&view=findpost&p=${item.id}"
            }
        }
        Utils.copyToClipBoard(url)
    }

    fun openTopicBegin(item: IBaseForumPost) {
        linkHandler.handle("https://4pda.ru/forum/index.php?showtopic=${item.topicId}", router)
    }

    fun openTopicNew(item: IBaseForumPost) {
        linkHandler.handle("https://4pda.ru/forum/index.php?showtopic=${item.topicId}&view=getnewpost", router)
    }

    fun openTopicLast(item: IBaseForumPost) {
        linkHandler.handle("https://4pda.ru/forum/index.php?showtopic=${item.topicId}&view=getlastpost", router)
    }

    fun openForum(item: IBaseForumPost) {
        linkHandler.handle("https://4pda.ru/forum/index.php?showforum=${item.forumId}", router)
    }

    fun onClickAddInFav(item: IBaseForumPost) {
        viewState.showAddInFavDialog(item)
    }

    fun addTopicToFavorite(topicId: Int, subType: String) {
        favoritesRepository
                .editFavorites(FavoritesApi.ACTION_ADD, -1, topicId, subType)
                .subscribe({
                    viewState.onAddToFavorite(it)
                }, {
                    errorHandler.handle(it)
                })
                .untilDestroy()
    }

    /* ITHEME PReSNETER*/

    private fun unavailableFunction() {
        router.showSystemMessage("Действие невозможно")
    }

    override fun onPollResultsClick() = unavailableFunction()

    override fun onPollClick() = unavailableFunction()

    override fun onReplyPostClick(postId: Int) = unavailableFunction()

    override fun onQuotePostClick(postId: Int, text: String) = unavailableFunction()

    override fun quoteFromBuffer(postId: Int) = unavailableFunction()

    override fun onPollHeaderClick(bValue: Boolean) = unavailableFunction()

    override fun onHatHeaderClick(bValue: Boolean) = unavailableFunction()

    override fun setHistoryBody(index: Int, body: String) = unavailableFunction()

    override fun shareText(text: String) {
        Utils.shareText(text)
    }

    private fun getPostById(postId: Int): IBaseForumPost? = currentData
            ?.items
            ?.firstOrNull {
                it.id == postId
            }

    override fun onFirstPageClick() = viewState.firstPage()

    override fun onPrevPageClick() = viewState.prevPage()

    override fun onNextPageClick() = viewState.nextPage()

    override fun onLastPageClick() = viewState.lastPage()

    override fun onSelectPageClick() = viewState.selectPage()

    override fun onUserMenuClick(postId: Int) {
        getPostById(postId)?.let { viewState.showUserMenu(it) }
    }

    override fun onReputationMenuClick(postId: Int) {
        getPostById(postId)?.let { viewState.showReputationMenu(it) }
    }

    override fun onPostMenuClick(postId: Int) {
        getPostById(postId)?.let { viewState.showPostMenu(it) }
    }

    override fun onReportPostClick(postId: Int) {
        getPostById(postId)?.let { viewState.reportPost(it) }
    }


    override fun onDeletePostClick(postId: Int) {
        getPostById(postId)?.let { viewState.deletePost(it) }
    }

    override fun onEditPostClick(postId: Int) {
        getPostById(postId)?.let { viewState.editPost(it) }
    }

    override fun onVotePostClick(postId: Int, type: Boolean) {
        getPostById(postId)?.let { viewState.votePost(it, type) }
    }

    override fun onSpoilerCopyLinkClick(postId: Int, spoilNumber: String) {
        getPostById(postId)?.let { viewState.openSpoilerLinkDialog(it, spoilNumber) }
    }

    override fun onAnchorClick(postId: Int, name: String) {
        getPostById(postId)?.let { viewState.openAnchorDialog(it, name) }
    }

    override fun copyText(text: String) {
        Utils.copyToClipBoard(text)
    }

    override fun toast(text: String) {
        //viewState.toast(text)
        router.showSystemMessage(text)
    }

    override fun log(text: String) {
        viewState.log(text)
    }

    override fun openProfile(postId: Int) {
        getPostById(postId)?.let {
            linkHandler.handle("https://4pda.ru/forum/index.php?showuser=${it.userId}", router)
        }
    }

    override fun openQms(postId: Int) {
        getPostById(postId)?.let {
            linkHandler.handle("https://4pda.ru/forum/index.php?act=qms&amp;mid=${it.userId}", router)
        }
    }

    override fun openSearchUserTopic(postId: Int) {
        getPostById(postId)?.let {
            linkHandler.handle(SearchSettings().apply {
                source = SearchSettings.SOURCE_ALL.first
                nick = it.nick
                result = SearchSettings.RESULT_TOPICS.first
            }.toUrl(), router)
        }
    }

    override fun openSearchInTopic(postId: Int) {
        getPostById(postId)?.let {
            linkHandler.handle(SearchSettings().apply {
                addForum(Integer.toString(it.forumId))
                addTopic(Integer.toString(it.topicId))
                source = SearchSettings.SOURCE_CONTENT.first
                nick = it.nick
                result = SearchSettings.RESULT_POSTS.first
                subforums = SearchSettings.SUB_FORUMS_FALSE
            }.toUrl(), router)
        }
    }

    override fun openSearchUserMessages(postId: Int) {
        getPostById(postId)?.let {
            linkHandler.handle(SearchSettings().apply {
                source = SearchSettings.SOURCE_CONTENT.first
                nick = it.nick
                result = SearchSettings.RESULT_POSTS.first
                subforums = SearchSettings.SUB_FORUMS_FALSE
            }.toUrl(), router)
        }
    }

    override fun onChangeReputationClick(postId: Int, type: Boolean) {
        getPostById(postId)?.let { viewState.showChangeReputation(it, type) }
    }

    override fun changeReputation(postId: Int, type: Boolean, message: String) {
        getPostById(postId)?.let {
            reputationRepository
                    .changeReputation(it.id, it.userId, type, message)
                    .subscribe({
                        router.showSystemMessage(App.get().getString(R.string.reputation_changed))
                    }, {
                        errorHandler.handle(it)
                    })
                    .untilDestroy()
        }
    }

    override fun votePost(postId: Int, type: Boolean) {
        getPostById(postId)?.let {
            themeRepository
                    .votePost(it.id, type)
                    .subscribe({
                        router.showSystemMessage(it)
                    }, {
                        errorHandler.handle(it)
                    })
                    .untilDestroy()
        }
    }

    override fun openReputationHistory(postId: Int) {
        getPostById(postId)?.let {
            linkHandler.handle("https://4pda.ru/forum/index.php?act=rep&view=history&amp;mid=${it.userId}", router)
        }
    }


    override fun reportPost(postId: Int, message: String) {
        getPostById(postId)?.let { post ->
            currentData?.let {
                themeRepository
                        .reportPost(post.topicId, post.id, message)
                        .subscribe({
                            router.showSystemMessage("Жалоба отправлена")
                        }, {
                            errorHandler.handle(it)
                        })
                        .untilDestroy()
            }
        }
    }

    override fun deletePost(postId: Int) {
        getPostById(postId)?.let { post ->
            themeRepository
                    .deletePost(post.id)
                    .subscribe({
                        if (it) {
                            viewState.deletePostUi(post)
                        }
                        router.showSystemMessage(App.get().getString(R.string.message_deleted))
                    }, {
                        errorHandler.handle(it)
                    })
                    .untilDestroy()
        }
    }

    override fun createNote(postId: Int) {
        getPostById(postId)?.let {
            val topicTitle: String = if (it is SearchItem) {
                it.title.orEmpty()
            } else {
                "пост из поиска_"
            }
            val title = String.format(App.get().getString(R.string.post_Topic_Nick_Number), topicTitle, it.nick, it.id)
            val url = "https://4pda.ru/forum/index.php?s=&showtopic=${it.topicId}&view=findpost&p=${it.id}"
            viewState.showNoteCreate(title, url)
        }
    }

    fun openEditPostForm(postId: Int) {
        getPostById(postId)?.let {
            val title: String = if (it is SearchItem) {
                it.title.orEmpty();
            } else {
                "пост из поиска_";
            }
            router.navigateTo(Screen.EditPost().apply {
                this.postId = postId
                topicId = it.topicId
                forumId = it.forumId
                st = settings.st
                themeName = title
            })
        }
    }

    override fun copyPostLink(postId: Int) {
        getPostById(postId)?.let {
            val url = "https://4pda.ru/forum/index.php?s=&showtopic=${it.topicId}&view=findpost&p=${it.id}"
            copyText(url)
        }
    }

    override fun sharePostLink(postId: Int) {
        getPostById(postId)?.let {
            val url = "https://4pda.ru/forum/index.php?s=&showtopic=${it.topicId}&view=findpost&p=${it.id}"
            shareText(url)
        }
    }

    override fun copyAnchorLink(postId: Int, name: String) {
        getPostById(postId)?.let {
            val url = "https://4pda.ru/forum/index.php?act=findpost&pid=${it.id}&anchor=$name"
            copyText(url)
        }
    }

    override fun copySpoilerLink(postId: Int, spoilNumber: String) {
        getPostById(postId)?.let {
            val url = "https://4pda.ru/forum/index.php?act=findpost&pid=${it.id}&anchor=Spoil-${it.id}-$spoilNumber"
            copyText(url)
        }
    }
}