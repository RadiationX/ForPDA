package forpdateam.ru.forpda.presentation.search

import android.content.Context
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.common.Utils
import forpdateam.ru.forpda.common.mvp.BasePresenter
import forpdateam.ru.forpda.entity.remote.search.SearchItem
import forpdateam.ru.forpda.entity.remote.search.SearchResult
import forpdateam.ru.forpda.entity.remote.search.SearchSettings
import forpdateam.ru.forpda.extensions.coRunCatching
import forpdateam.ru.forpda.model.data.remote.api.favorites.FavoritesApi
import forpdateam.ru.forpda.model.preferences.MainPreferencesHolder
import forpdateam.ru.forpda.model.preferences.OtherPreferencesHolder
import forpdateam.ru.forpda.model.preferences.TopicPreferencesHolder
import forpdateam.ru.forpda.model.repository.faviorites.FavoritesRepository
import forpdateam.ru.forpda.model.repository.reputation.ReputationRepository
import forpdateam.ru.forpda.model.repository.search.SearchRepository
import forpdateam.ru.forpda.model.repository.theme.ThemeRepository
import forpdateam.ru.forpda.presentation.ErrorHandler
import forpdateam.ru.forpda.presentation.LinkHandler
import forpdateam.ru.forpda.presentation.Screen
import forpdateam.ru.forpda.presentation.TabRouter
import forpdateam.ru.forpda.presentation.theme.IThemePresenter
import forpdateam.ru.forpda.ui.TemplateManager
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import moxy.InjectViewState

@InjectViewState
class SearchPresenter(
    private val context: Context,
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
    private val linkHandler: LinkHandler,
    private val errorHandler: ErrorHandler,
    private val utils: Utils
) : BasePresenter<SearchSiteView>(), IThemePresenter {

    companion object {
        const val FIELD_RESOURCE = "resource"
        const val FIELD_RESULT = "result"
        const val FIELD_SORT = "sort"
        const val FIELD_SOURCE = "source"
    }

    private val resourceItems =
        listOf<String>(SearchSettings.RESOURCE_FORUM.second, SearchSettings.RESOURCE_NEWS.second)
    private val resultItems =
        listOf<String>(SearchSettings.RESULT_TOPICS.second, SearchSettings.RESULT_POSTS.second)
    private val sortItems = listOf<String>(
        SearchSettings.SORT_DA.second,
        SearchSettings.SORT_DD.second,
        SearchSettings.SORT_REL.second
    )
    private val sourceItems = listOf<String>(
        SearchSettings.SOURCE_ALL.second,
        SearchSettings.SOURCE_TITLES.second,
        SearchSettings.SOURCE_CONTENT.second
    )

    private val fields = mapOf(
        FIELD_RESOURCE to resourceItems,
        FIELD_RESULT to resultItems,
        FIELD_SORT to sortItems,
        FIELD_SOURCE to sourceItems
    )

    private var argSettings = SearchSettings.default()

    private var currentData: SearchResult? = null

    init {
        val settings = otherPreferencesHolder.searchSettings.get()?.let {
            SearchSettings.parseSettings(it)
        }
        initSearchSettings(settings)
    }

    fun initSearchSettings(settings: SearchSettings?) {
        if (settings == null) return
        argSettings = settings
    }

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()

        topicPreferencesHolder
            .showAvatars
            .onEach {
                viewState.updateShowAvatarState(it)
            }
            .launchIn(viewModelScope)

        topicPreferencesHolder
            .circleAvatars
            .onEach {
                viewState.updateTypeAvatarState(it)
            }
            .launchIn(viewModelScope)

        mainPreferencesHolder
            .scrollButtonEnabled
            .onEach {
                viewState.updateScrollButtonState(it)
            }
            .launchIn(viewModelScope)

        mainPreferencesHolder
            .webViewFontSize
            .onEach {
                viewState.setFontSize(it)
            }
            .launchIn(viewModelScope)

        templateManager
            .observeThemeType()
            .onEach {
                viewState.setStyleType(it)
            }
            .launchIn(viewModelScope)
        viewState.fillSettingsData(argSettings, fields)
        refreshData()
    }

    fun refreshData() {
        if (argSettings.query.isNullOrEmpty() && argSettings.nick.isNullOrEmpty()) {
            return
        }
        val withHtml =
            argSettings.result == SearchSettings.RESULT_POSTS.first && argSettings.resourceType == SearchSettings.RESOURCE_FORUM.first

        viewModelScope.launch {
            viewState.setRefreshing(true)
            viewState.onStartSearch(argSettings)
            coRunCatching {
                searchRepository.getSearch(argSettings)
            }.map {
                if (withHtml) searchTemplate.mapEntity(it) else it
            }.onSuccess {
                currentData = it
                viewState.showData(it)
            }.onFailure {
                errorHandler.handle(it)
            }
            viewState.setRefreshing(false)
        }
    }

    fun search(query: String, nick: String) {
        argSettings = argSettings.copy(
            st = 0,
            query = query,
            nick = nick
        )
        refreshData()
    }

    fun search(pageNumber: Int) {
        argSettings = argSettings.copy(
            st = pageNumber
        )
        refreshData()
    }

    fun updateSettings(field: String, position: Int) {
        when (field) {
            FIELD_RESOURCE -> {
                val name = resourceItems[position]
                when {
                    checkName(name, SearchSettings.RESOURCE_NEWS) -> {
                        argSettings = argSettings.copy(
                            resourceType = SearchSettings.RESOURCE_NEWS.first
                        )
                        viewState.setNewsMode()
                    }

                    checkName(name, SearchSettings.RESOURCE_FORUM) -> {
                        argSettings = argSettings.copy(
                            resourceType = SearchSettings.RESOURCE_FORUM.first
                        )
                        viewState.setForumMode()
                    }
                }
            }

            FIELD_RESULT -> {
                val name = resultItems[position]
                when {
                    checkName(name, SearchSettings.RESULT_TOPICS) -> {
                        argSettings = argSettings.copy(result = SearchSettings.RESULT_TOPICS.first)
                    }

                    checkName(name, SearchSettings.RESULT_POSTS) -> {
                        argSettings = argSettings.copy(result = SearchSettings.RESULT_POSTS.first)
                    }
                }
            }

            FIELD_SORT -> {
                val name = sortItems[position]
                when {
                    checkName(name, SearchSettings.SORT_DA) -> {
                        argSettings = argSettings.copy(sort = SearchSettings.SORT_DA.first)
                    }

                    checkName(name, SearchSettings.SORT_DD) -> {
                        argSettings = argSettings.copy(sort = SearchSettings.SORT_DD.first)
                    }

                    checkName(name, SearchSettings.SORT_REL) -> {
                        argSettings = argSettings.copy(sort = SearchSettings.SORT_REL.first)
                    }
                }
            }

            FIELD_SOURCE -> {
                val name = sourceItems[position]
                when {
                    checkName(name, SearchSettings.SOURCE_ALL) -> {
                        argSettings = argSettings.copy(source = SearchSettings.SOURCE_ALL.first)
                    }

                    checkName(name, SearchSettings.SOURCE_TITLES) -> {
                        argSettings = argSettings.copy(source = SearchSettings.SOURCE_TITLES.first)
                    }

                    checkName(name, SearchSettings.SOURCE_CONTENT) -> {
                        argSettings = argSettings.copy(source = SearchSettings.SOURCE_CONTENT.first)
                    }
                }
            }
        }
    }

    private fun checkName(arg: String, pair: Pair<String, String>): Boolean {
        return arg == pair.second
    }


    fun saveSettings() {
        val saveSettings = SearchSettings.default().copy(
            resourceType = argSettings.resourceType,
            result = argSettings.result,
            sort = argSettings.sort,
            source = argSettings.source
        )
        val saveUrl = saveSettings.toUrl()
        otherPreferencesHolder.searchSettings.set(saveUrl)
    }

    fun onItemClick(item: SearchItem) {
        val url = getItemUrl(item)
        linkHandler.handle(url)
    }

    fun onItemLongClick(item: SearchItem) {
        viewState.showItemDialogMenu(item, argSettings)
    }

    fun copyLink() {
        utils.copyToClipBoard(argSettings.toUrl())
    }

    fun copyLink(item: SearchItem) {
        val url = getItemUrl(item)
        utils.copyToClipBoard(url)
    }

    private fun getItemUrl(item: SearchItem): String {
        return when (item) {
            is SearchItem.News -> "https://4pda.to/index.php?p=${item.id}"
            is SearchItem.Topic -> "https://4pda.to/forum/index.php?showtopic=${item.topicId}"
            is SearchItem.Post -> "https://4pda.to/forum/index.php?showtopic=${item.post.topicId}&view=findpost&p=${item.post.id}"
        }
    }

    fun openTopicBegin(item: SearchItem) {
        val topicId = when (item) {
            is SearchItem.Post -> item.post.topicId
            is SearchItem.Topic -> item.topicId
            is SearchItem.News -> return
        }
        linkHandler.handle("https://4pda.to/forum/index.php?showtopic=${topicId}")
    }

    fun openTopicNew(item: SearchItem) {
        val topicId = when (item) {
            is SearchItem.Post -> item.post.topicId
            is SearchItem.Topic -> item.topicId
            is SearchItem.News -> return
        }
        linkHandler.handle(
            "https://4pda.to/forum/index.php?showtopic=${topicId}&view=getnewpost"
        )
    }

    fun openTopicLast(item: SearchItem) {
        val topicId = when (item) {
            is SearchItem.Post -> item.post.topicId
            is SearchItem.Topic -> item.topicId
            is SearchItem.News -> return
        }
        linkHandler.handle(
            "https://4pda.to/forum/index.php?showtopic=${topicId}&view=getlastpost"
        )
    }

    fun openForum(item: SearchItem) {
        val forumId = when (item) {
            is SearchItem.Topic -> item.forumId
            is SearchItem.Post -> return
            is SearchItem.News -> return
        }
        linkHandler.handle("https://4pda.to/forum/index.php?showforum=${forumId}")
    }

    fun onClickAddInFav(item: SearchItem) {
        viewState.showAddInFavDialog(item)
    }

    fun addTopicToFavorite(item: SearchItem, subType: String) {
        val topicId = when (item) {
            is SearchItem.Post -> item.post.topicId
            is SearchItem.Topic -> item.topicId
            is SearchItem.News -> return
        }
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

    override fun shareText(text: String) {
        utils.shareText(text)
    }

    private fun getPostById(postId: Int): SearchItem.Post? = currentData
        ?.items
        ?.filterIsInstance<SearchItem.Post>()
        ?.firstOrNull { it.post.id == postId }

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
        utils.copyToClipBoard(text)
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
            linkHandler.handle(
                "https://4pda.to/forum/index.php?showuser=${it.post.user.id}"
            )
        }
    }

    override fun openQms(postId: Int) {
        getPostById(postId)?.let {
            linkHandler.handle(
                "https://4pda.to/forum/index.php?act=qms&amp;mid=${it.post.user.id}"
            )
        }
    }

    override fun openSearchUserTopic(postId: Int) {
        getPostById(postId)?.let {
            linkHandler.handle(
                SearchSettings.default().copy(
                    source = SearchSettings.SOURCE_ALL.first,
                    nick = it.post.user.nick,
                    result = SearchSettings.RESULT_TOPICS.first
                ).toUrl()
            )
        }
    }

    override fun openSearchInTopic(postId: Int) {
        getPostById(postId)?.let {
            linkHandler.handle(
                SearchSettings.default().copy(
                    topics = listOf(it.post.topicId),
                    source = SearchSettings.SOURCE_CONTENT.first,
                    nick = it.post.user.nick,
                    result = SearchSettings.RESULT_POSTS.first,
                    subforums = SearchSettings.SUB_FORUMS_FALSE
                ).toUrl()
            )
        }
    }

    override fun openSearchUserMessages(postId: Int) {
        getPostById(postId)?.let {
            linkHandler.handle(
                SearchSettings.default().copy(
                    source = SearchSettings.SOURCE_CONTENT.first,
                    nick = it.post.user.nick,
                    result = SearchSettings.RESULT_POSTS.first,
                    subforums = SearchSettings.SUB_FORUMS_FALSE
                ).toUrl()
            )
        }
    }

    override fun onChangeReputationClick(postId: Int, type: Boolean) {
        getPostById(postId)?.let { viewState.showChangeReputation(it, type) }
    }

    override fun changeReputation(postId: Int, type: Boolean, message: String) {
        getPostById(postId)?.let {
            viewModelScope.launch {
                coRunCatching {
                    reputationRepository.changeReputation(
                        it.post.id,
                        it.post.user.id,
                        type,
                        message
                    )
                }.onSuccess {
                    router.showSystemMessage(R.string.reputation_changed)
                }.onFailure {
                    errorHandler.handle(it)
                }
            }
        }
    }

    override fun votePost(postId: Int, type: Boolean) {
        getPostById(postId)?.let {
            viewModelScope.launch {
                coRunCatching {
                    themeRepository.votePost(it.post.id, type)
                }.onSuccess {
                    router.showSystemMessage(it)
                }.onFailure {
                    errorHandler.handle(it)
                }
            }
        }
    }

    override fun openReputationHistory(postId: Int) {
        getPostById(postId)?.let {
            linkHandler.handle(
                "https://4pda.to/forum/index.php?act=rep&view=history&amp;mid=${it.post.user.nick}"
            )
        }
    }


    override fun reportPost(postId: Int, message: String) {
        getPostById(postId)?.let { post ->
            viewModelScope.launch {
                coRunCatching {
                    themeRepository.reportPost(post.post.topicId, post.post.id, message)
                }.onSuccess {
                    router.showSystemMessage("Жалоба отправлена")
                }.onFailure {
                    errorHandler.handle(it)
                }
            }
        }
    }

    override fun deletePost(postId: Int) {
        getPostById(postId)?.let { post ->
            viewModelScope.launch {
                coRunCatching {
                    themeRepository.deletePost(post.post.id)
                }.onSuccess {
                    viewState.deletePostUi(post)
                    router.showSystemMessage(R.string.message_deleted)
                }.onFailure {
                    errorHandler.handle(it)
                }
            }
        }
    }

    override fun createNote(postId: Int) {
        getPostById(postId)?.let {
            val topicTitle: String = it.title
            val title = context.getString(
                R.string.post_Topic_Nick_Number,
                topicTitle,
                it.post.user.nick,
                it.post.id
            )
            val url =
                "https://4pda.to/forum/index.php?s=&showtopic=${it.post.topicId}&view=findpost&p=${it.post.id}"
            viewState.showNoteCreate(title, url)
        }
    }

    fun openEditPostForm(postId: Int) {
        getPostById(postId)?.let {
            val title: String = it.title
            router.navigateTo(
                Screen.EditPost.Existed(
                    postId = it.post.id,
                    topicId = it.post.topicId,
                    forumId = -1,
                    st = argSettings.st,
                    themeName = title
                )
            )
        }
    }

    override fun copyPostLink(postId: Int) {
        getPostById(postId)?.let {
            val url =
                "https://4pda.to/forum/index.php?s=&showtopic=${it.post.topicId}&view=findpost&p=${it.post.id}"
            copyText(url)
        }
    }

    override fun sharePostLink(postId: Int) {
        getPostById(postId)?.let {
            val url =
                "https://4pda.to/forum/index.php?s=&showtopic=${it.post.topicId}&view=findpost&p=${it.post.id}"
            shareText(url)
        }
    }

    override fun copyAnchorLink(postId: Int, name: String) {
        getPostById(postId)?.let {
            val url = "https://4pda.to/forum/index.php?act=findpost&pid=${it.post.id}&anchor=$name"
            copyText(url)
        }
    }

    override fun copySpoilerLink(postId: Int, spoilNumber: String) {
        getPostById(postId)?.let {
            val url =
                "https://4pda.to/forum/index.php?act=findpost&pid=${it.post.id}&anchor=Spoil-${it.post.id}-$spoilNumber"
            copyText(url)
        }
    }
}