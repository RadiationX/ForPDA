package forpdateam.ru.forpda.presentation.theme

import android.content.Context
import android.net.Uri
import android.util.Log
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.common.Utils
import forpdateam.ru.forpda.common.mvp.BasePresenter
import forpdateam.ru.forpda.entity.app.EditPostSyncData
import forpdateam.ru.forpda.entity.asDeferredData
import forpdateam.ru.forpda.entity.remote.ForumPost
import forpdateam.ru.forpda.entity.remote.editpost.AttachmentItem
import forpdateam.ru.forpda.entity.remote.editpost.EditPostForm
import forpdateam.ru.forpda.entity.remote.events.WebSocketEvent
import forpdateam.ru.forpda.entity.remote.favorites.FavoriteAction
import forpdateam.ru.forpda.entity.remote.search.SearchSettings
import forpdateam.ru.forpda.entity.remote.theme.ThemePage
import forpdateam.ru.forpda.entity.remote.theme.ThemePost
import forpdateam.ru.forpda.extensions.coRunCatching
import forpdateam.ru.forpda.model.data.remote.api.RequestFile
import forpdateam.ru.forpda.model.data.remote.api.theme.ThemeParser
import forpdateam.ru.forpda.model.interactors.CrossScreenInteractor
import forpdateam.ru.forpda.model.preferences.MainPreferencesHolder
import forpdateam.ru.forpda.model.preferences.TopicPreferencesHolder
import forpdateam.ru.forpda.model.repository.events.WebSocketEventsRepository
import forpdateam.ru.forpda.model.repository.faviorites.FavoritesRepository
import forpdateam.ru.forpda.model.repository.posteditor.PostEditorRepository
import forpdateam.ru.forpda.model.repository.profile.ProfileRepository
import forpdateam.ru.forpda.model.repository.reputation.ReputationRepository
import forpdateam.ru.forpda.model.repository.theme.ThemeRepository
import forpdateam.ru.forpda.presentation.ErrorHandler
import forpdateam.ru.forpda.presentation.LinkHandler
import forpdateam.ru.forpda.presentation.Screen
import forpdateam.ru.forpda.presentation.TabRouter
import forpdateam.ru.forpda.ui.TemplateManager
import forpdateam.ru.forpda.ui.fragments.theme.ThemeFragmentWeb
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import moxy.InjectViewState
import ru.radiationx.coretypes.FavoriteId
import ru.radiationx.coretypes.PageOffset
import ru.radiationx.coretypes.PostId
import ru.radiationx.coretypes.TopicId
import ru.radiationx.links.Link
import ru.radiationx.links.parser.LinkTransformer
import ru.radiationx.quill.QuillExtra
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.util.regex.Pattern
import kotlin.time.Duration.Companion.seconds

/**
 * Created by radiationx on 15.03.18.
 */
data class ThemeExtra(
    val link: Link.Board.Topic
) : QuillExtra

@InjectViewState
class ThemePresenter(
    private val argExtra: ThemeExtra,
    private val context: Context,
    private val themeRepository: ThemeRepository,
    private val reputationRepository: ReputationRepository,
    private val editorRepository: PostEditorRepository,
    private val favoritesRepository: FavoritesRepository,
    private val webSocketEventsRepository: WebSocketEventsRepository,
    private val profileRepository: ProfileRepository,
    private val topicPreferencesHolder: TopicPreferencesHolder,
    private val mainPreferencesHolder: MainPreferencesHolder,
    private val crossScreenInteractor: CrossScreenInteractor,
    private val themeTemplate: ThemeTemplate,
    private val templateManager: TemplateManager,
    private val router: TabRouter,
    private val linkHandler: LinkHandler,
    private val errorHandler: ErrorHandler,
    private val utils: Utils,
    private val themeParser: ThemeParser,
    private val linkTransformer: LinkTransformer
) : BasePresenter<ThemeView>(), IThemePresenter {

    var loadAction = ActionState.NORMAL

    val history = TopicHistory()

    val currentPage: ThemePage?
        get() = history.currentPage

    val currentPageUrl: Link.Board.Topic.ShowTopic.Page?
        get() = currentPage?.link

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
        webSocketEventsRepository
            .observeEvents()
            .filterIsInstance<WebSocketEvent.Topic>()
            .debounce(2.seconds)
            .onEach {
                handleEvent(it)
            }
            .launchIn(viewModelScope)
        loadUrl(argExtra.link)
    }

    fun exit() {
        router.exit()
    }

    private fun handleEvent(event: WebSocketEvent.Topic) {
        if (!isPageLoaded()) {
            return
        }
        if (event.topicId != getId()) {
            return
        }

        when (event.type) {
            is WebSocketEvent.Topic.Type.HatUpdate -> Unit
            is WebSocketEvent.Topic.Type.Mention -> Unit
            is WebSocketEvent.Topic.Type.New -> {
                viewState.onEventNew()
            }

            is WebSocketEvent.Topic.Type.Read -> {
                viewState.onEventRead()
            }
        }
    }

    fun getPageScrollY() = currentPage?.scrollY ?: 0

    fun canQuote() = currentPage?.canQuote ?: false

    fun isPageLoaded() = currentPage != null

    fun isInFavorites() = currentPage?.isInFavorite ?: false

    fun getId() = currentPage?.id ?: -1

    private fun loadData(link: Link.Board.Topic, action: ActionState) {
        var hatOpen = false
        var pollOpen = false
        currentPage?.let {
            hatOpen = it.isHatOpen
            pollOpen = it.isPollOpen
        }
        loadAction = action
        viewState.updateHistoryLastHtml()
        viewModelScope.launch {
            viewState.setRefreshing(true)
            coRunCatching {
                themeRepository.getTheme(link, hatOpen, pollOpen)
            }.map {
                themeTemplate.mapEntity(it)
            }.onSuccess {
                onLoadData(it)
            }.onFailure {
                errorHandler.handle(it)
            }
            viewState.setRefreshing(false)
        }
    }

    private fun onLoadData(page: ThemePage) {
        if (!page.pagination.hasNext()) {
            viewModelScope.launch {
                crossScreenInteractor.onLoadTopic(page.id)
            }
        }
        if (loadAction === ActionState.NORMAL) {
            history.add(page)
        }
        if (loadAction === ActionState.REFRESH) {
            history.updateOrAdd { current ->
                if (current == null) {
                    page
                } else {
                    page.copy(
                        scrollY = current.scrollY,
                        anchors = page.anchors + current.anchors
                    )
                }
            }
        }
        viewState.onLoadData(page)
    }

    fun addTopicToFavorite(topicId: TopicId, subType: String) {
        viewModelScope.launch {
            coRunCatching {
                favoritesRepository.editFavorites(FavoriteAction.AddTopic(topicId, subType))
            }.onSuccess {
                if (it) {
                    history.updateExist { it.copy(isInFavorite = true) }
                }
                viewState.onAddToFavorite(it)
            }.onFailure {
                errorHandler.handle(it)
            }
        }
    }

    fun deleteTopicFromFavorite(favId: FavoriteId) {
        viewModelScope.launch {
            coRunCatching {
                favoritesRepository.editFavorites(FavoriteAction.Delete(favId))
            }.onSuccess {
                if (it) {
                    history.updateExist { it.copy(isInFavorite = false) }
                }
                viewState.onDeleteFromFavorite(it)
            }.onFailure {
                errorHandler.handle(it)
            }
        }
    }

    private fun createEditPostForm(
        message: String,
        attachments: List<AttachmentItem>
    ): EditPostForm? = currentPage?.let {
        val form = EditPostForm()
        form.forumId = it.forumId
        form.topicId = it.id
        form.st = PageOffset(it.pagination.currentPage())
        form.message = message
        form.attachments.addAll(attachments)
        form
    }

    fun openEditPostForm(message: String, attachments: List<AttachmentItem>) {
        currentPage?.let { page ->
            createEditPostForm(message, attachments)?.let {
                router.navigateTo(
                    Screen.EditPost.Create(
                        editPostForm = it,
                        themeName = page.title
                    )
                )
                router.setResultListener(Screen.Theme.CODE_RESULT_SYNC, {
                    (it as? EditPostSyncData?)?.let {
                        if (it.topicId == page.id) {
                            viewState.syncEditPost(it)
                        }
                    }
                })
                router.setResultListener(Screen.Theme.CODE_RESULT_PAGE, {
                    (it as? ThemePage?)?.let {
                        viewState.onMessageSent()
                        onLoadData(it)
                    }
                })
            }
        }
    }

    fun openEditPostForm(postId: PostId) {
        currentPage?.let {
            router.navigateTo(
                Screen.EditPost.Edit(
                    postId = postId,
                    topicId = it.id,
                    forumId = it.forumId,
                    offset = PageOffset(it.st),
                    themeName = it.title
                )
            )
            router.setResultListener(Screen.Theme.CODE_RESULT_PAGE, {
                (it as? ThemePage?)?.let { onLoadData(it) }
            })
        }
    }


    fun sendMessage(message: String, attachments: List<AttachmentItem>) {
        createEditPostForm(message, attachments)?.let {
            viewState.setMessageRefreshing(true)
            viewModelScope.launch {
                viewState.setMessageRefreshing(true)
                coRunCatching {
                    editorRepository.sendPost(it)
                }.map {
                    themeTemplate.mapEntity(it)
                }.onSuccess {
                    onLoadData(it)
                    viewState.onMessageSent()
                }.onFailure {
                    errorHandler.handle(it)
                }
                viewState.setMessageRefreshing(false)
            }
        }
    }

    fun uploadFiles(files: List<RequestFile>, pending: List<AttachmentItem>) {
        viewModelScope.launch {
            coRunCatching {
                editorRepository.uploadFiles(null, files, pending)
            }.onSuccess {
                viewState.onUploadFiles(it)
            }.onFailure {
                errorHandler.handle(it)
            }
        }
    }

    fun deleteFiles(items: List<AttachmentItem>) {
        viewModelScope.launch {
            coRunCatching {
                editorRepository.deleteFiles(null, items)
            }.onSuccess {
                viewState.onDeleteFiles(it)
            }.onFailure {
                errorHandler.handle(it)
            }
        }
    }

    private fun loadUrl(link: Link.Board.Topic) {
        loadData(link, ActionState.NORMAL)
    }

    fun reload() {
        loadData(currentPageUrl ?: argExtra.link, ActionState.REFRESH)
    }

    fun loadNewPosts() {
        val url = currentPageUrl ?: return
        loadUrl(Link.Board.Topic.ShowTopic.GetNewPost(url.topicId))
    }

    fun loadPage(page: Int) {
        val url = currentPageUrl ?: return
        loadUrl(url.copy(offset = PageOffset(page)))
    }

    private fun backPage(): Boolean {
        if (history.size > 1) {
            loadAction = ActionState.BACK
            history.removeCurrent()
            viewState.updateView(currentPage!!)
            return true
        }
        return false
    }

    override fun onPollResultsClick() {
        val currentUrl = currentPage?.link ?: return
        val url = currentUrl.copy(showPollResults = true, anchor = null)
        loadUrl(url)
    }

    override fun onPollClick() {
        val currentUrl = currentPage?.link ?: return
        val url = currentUrl.copy(showPollResults = false, anchor = null)
        loadUrl(url)
    }

    fun updateHistoryLastHtml(html: String, scrollY: Int) {
        history.updateExist {
            it.copy(
                scrollY = scrollY,
                html = html.asDeferredData()
            )
        }
    }

    override fun shareText(text: String) {
        utils.shareText(text)
    }

    fun copyLink() {
        currentPage?.let {
            utils.copyToClipBoard("https://4pda.to/forum/index.php?showtopic=${it.id.id}")
        }
    }

    fun openSearch() {
        currentPage?.let {
            linkHandler.handle(
                "https://4pda.to/forum/index.php?forums=${it.forumId.id}&topics=${it.id.id}&act=search&source=pst&result=posts"
            )
        }
    }

    fun openSearchMyPosts() {
        currentPage?.let {
            viewModelScope.launch {
                var url =
                    "https://4pda.to/forum/index.php?forums=${it.forumId.id}&topics=${it.id.id}&act=search&source=pst&result=posts&username="

                try {
                    url += URLEncoder.encode(profileRepository.getCurrentUser()?.nick.orEmpty(), "windows-1251")
                } catch (e: UnsupportedEncodingException) {
                    e.printStackTrace()
                }

                linkHandler.handle(url)
            }
        }
    }

    fun openForum() {
        currentPage?.let {
            linkHandler.handle("https://4pda.to/forum/index.php?showforum=${it.forumId.id}")
        }
    }

    private fun getThemePostById(postId: PostId): ThemePost? = currentPage
        ?.posts
        ?.firstOrNull { it.post.id == postId }

    private fun getPostById(postId: PostId): ForumPost? = getThemePostById(postId)?.post


    override fun onFirstPageClick() = viewState.firstPage()

    override fun onPrevPageClick() = viewState.prevPage()

    override fun onNextPageClick() = viewState.nextPage()

    override fun onLastPageClick() = viewState.lastPage()

    override fun onSelectPageClick() = viewState.selectPage()

    override fun onUserMenuClick(postId: PostId) {
        getPostById(postId)?.let { viewState.showUserMenu(it) }
    }

    override fun onReputationMenuClick(postId: PostId) {
        getPostById(postId)?.let { viewState.showReputationMenu(it) }
    }

    override fun onPostMenuClick(postId: PostId) {
        getPostById(postId)?.let { viewState.showPostMenu(it) }
    }

    override fun onReportPostClick(postId: PostId) {
        getPostById(postId)?.let { viewState.reportPost(it) }
    }

    override fun onReplyPostClick(postId: PostId) {
        getPostById(postId)?.let {
            val text = "[snapback]${it.id.id}[/snapback] [b]${it.user.nick},[/b] \n"
            viewState.insertText(text)
        }
    }

    override fun onQuotePostClick(postId: PostId, text: String) {
        getPostById(postId)?.let {
            val date = utils.getForumDateTime(utils.parseForumDateTime(it.date))
            val insert =
                "[quote name=\"${it.user.nick}\" date=\"$date\" post=${it.id.id}]$text[/quote]\n"
            viewState.insertText(insert)
        }
    }

    override fun onDeletePostClick(postId: PostId) {
        getPostById(postId)?.let { viewState.deletePost(it) }
    }

    override fun onEditPostClick(postId: PostId) {
        getPostById(postId)?.let { viewState.editPost(it) }
    }

    override fun onVotePostClick(postId: PostId, type: Boolean) {
        getPostById(postId)?.let { viewState.votePost(it, type) }
    }

    override fun onSpoilerCopyLinkClick(postId: PostId, spoilNumber: String) {
        getPostById(postId)?.let { viewState.openSpoilerLinkDialog(it, spoilNumber) }
    }

    override fun onAnchorClick(postId: PostId, name: String) {
        getPostById(postId)?.let { viewState.openAnchorDialog(it, name) }
    }

    override fun onPollHeaderClick(bValue: Boolean) {
        history.updateExist { it.copy(isPollOpen = bValue) }
    }

    override fun onHatHeaderClick(bValue: Boolean) {
        history.updateExist { it.copy(isHatOpen = bValue) }
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

    private val LOG_TAG = ThemeFragmentWeb::class.java.simpleName
    fun handleNewUrl(uri: Uri) {
        Log.d(LOG_TAG, "handle $uri")
        val url = uri.toString()
        if (checkIsPoll(url)) {
            return
        }
        if (checkIsAttachment(url)) {
            return
        }
        val oldUrl = currentPageUrl ?: return
        val newUrl = linkTransformer.parse(url)
        if (newUrl == null || newUrl !is Link.Board.Topic) {
            linkHandler.handle(url)
            return
        }
        if (newUrl is Link.Board.Topic.ShowTopic) {
            if (oldUrl.topicId != newUrl.topicId) {
                loadUrl(newUrl)
                return
            }
        }
        val anchor = when (newUrl) {
            is Link.Board.Topic.FindPost -> newUrl.anchor ?: Link.Board.Topic.Anchor.Post(newUrl.postId)
            is Link.Board.Topic.ShowTopic.Page -> newUrl.anchor
            is Link.Board.Topic.ShowTopic.FindPost -> newUrl.anchor ?: Link.Board.Topic.Anchor.Post(newUrl.postId)
            is Link.Board.Topic.ShowTopic.GetLastPost,
            is Link.Board.Topic.ShowTopic.GetNewPost -> null
        }
        if (anchor != null && getPostById(anchor.postId) != null) {
            if (topicPreferencesHolder.anchorHistory.get()) {
                history.updateExist { it.copy(anchors = it.anchors + anchor) }
            }
            viewState.scrollToAnchor(anchor.value)
            return
        }
        loadUrl(newUrl)
    }

    private fun checkIsAttachment(url: String): Boolean {
        if (themeParser.parseAttachedImages(url).isEmpty()) return false
        val page = currentPage ?: return false
        page.posts.forEach { post ->
            post.attachImages.forEach { image ->
                if (!image.first.contains(url)) return@forEach
                val screen = Screen.ImageViewer(
                    urls = post.attachImages.map { it.first },
                    selectedUrl = image.first
                )
                router.navigateTo(screen)
                return true
            }
        }
        return false
    }

    // todo do something with sending poll
    private fun checkIsPoll(newUrl: String): Boolean {
        val m = Pattern.compile("4pda.to.*?addpoll=1").matcher(newUrl)
        if (!m.find()) {
            return false
        }
        reload()
        return true
    }

    fun onClickDeleteInFav() {
        currentPage?.let { viewState.showDeleteInFavDialog(it) }
    }

    fun onClickAddInFav() {
        currentPage?.let { viewState.showAddInFavDialog(it) }
    }

    fun onBackPressed(): Boolean {
        if (topicPreferencesHolder.anchorHistory.get()) {
            currentPage?.let { oldPage ->
                if (oldPage.anchors.size > 1) {
                    val newAnchors = oldPage.anchors.toMutableList()
                    newAnchors.removeAt(newAnchors.lastIndex)
                    val newPage = oldPage.copy(anchors = newAnchors)
                    history.update(newPage)
                    viewState.scrollToAnchor(newPage.anchor?.value)
                    return true
                }
            }
        }
        return backPage()
    }


    override fun openProfile(postId: PostId) {
        getPostById(postId)?.let {
            linkHandler.handle("https://4pda.to/forum/index.php?showuser=${it.user.id.id}")
        }
    }

    override fun openQms(postId: PostId) {
        getPostById(postId)?.let {
            linkHandler.handle(
                "https://4pda.to/forum/index.php?act=qms&amp;mid=${it.user.id.id}"
            )
        }
    }

    override fun openSearchUserTopic(postId: PostId) {
        getPostById(postId)?.let {
            linkHandler.handle(
                SearchSettings.default().copy(
                    source = SearchSettings.SOURCE_ALL.first,
                    nick = it.user.nick,
                    result = SearchSettings.RESULT_TOPICS.first
                ).toUrl()
            )
        }
    }

    override fun openSearchInTopic(postId: PostId) {
        getThemePostById(postId)?.let {
            val post = it.post
            linkHandler.handle(
                SearchSettings.default().copy(
                    forums = listOf(it.forumId.id),
                    topics = listOf(post.topicId.id),
                    source = SearchSettings.SOURCE_CONTENT.first,
                    nick = post.user.nick,
                    result = SearchSettings.RESULT_POSTS.first,
                    subforums = SearchSettings.SUB_FORUMS_FALSE
                ).toUrl()
            )
        }
    }

    override fun openSearchUserMessages(postId: PostId) {
        getPostById(postId)?.let {
            linkHandler.handle(
                SearchSettings.default().copy(
                    source = SearchSettings.SOURCE_CONTENT.first,
                    nick = it.user.nick,
                    result = SearchSettings.RESULT_POSTS.first,
                    subforums = SearchSettings.SUB_FORUMS_FALSE
                ).toUrl()
            )
        }
    }

    override fun onChangeReputationClick(postId: PostId, type: Boolean) {
        getPostById(postId)?.let { viewState.showChangeReputation(it, type) }
    }

    override fun changeReputation(postId: PostId, type: Boolean, message: String) {
        getPostById(postId)?.let {
            viewModelScope.launch {
                coRunCatching {
                    reputationRepository.changeReputation(it.id, it.user.id, type, message)
                }.onSuccess {
                    router.showSystemMessage(R.string.reputation_changed)
                }.onFailure {
                    errorHandler.handle(it)
                }
            }
        }
    }

    override fun votePost(postId: PostId, type: Boolean) {
        getPostById(postId)?.let {
            viewModelScope.launch {
                coRunCatching {
                    themeRepository.votePost(it.id, type)
                }.onSuccess {
                    router.showSystemMessage(it)
                }.onFailure {
                    errorHandler.handle(it)
                }
            }
        }
    }

    override fun openReputationHistory(postId: PostId) {
        getPostById(postId)?.let {
            linkHandler.handle(
                "https://4pda.to/forum/index.php?act=rep&view=history&amp;mid=${it.user.id.id}"
            )
        }
    }

    override fun quoteFromBuffer(postId: PostId) {
        getPostById(postId)?.let {
            val text = utils.readFromClipboard()
            if (!text.isNullOrEmpty()) {
                onQuotePostClick(postId, text)
            }
        }
    }

    override fun reportPost(postId: PostId, message: String) {
        getPostById(postId)?.let { post ->
            viewModelScope.launch {
                coRunCatching {
                    themeRepository.reportPost(post.topicId, post.id, message)
                }.onSuccess {
                    router.showSystemMessage("Жалоба отправлена")
                }.onFailure {
                    errorHandler.handle(it)
                }
            }
        }
    }

    override fun deletePost(postId: PostId) {
        getPostById(postId)?.let { post ->
            viewModelScope.launch {
                coRunCatching {
                    themeRepository.deletePost(post.id)
                }.onSuccess {
                    viewState.deletePostUi(post)
                    router.showSystemMessage(R.string.message_deleted)
                }.onFailure {
                    errorHandler.handle(it)
                }
            }
        }
    }

    override fun createNote(postId: PostId) {
        getPostById(postId)?.let {
            val themeTitle: String = currentPage?.title.orEmpty()
            val title = context.getString(R.string.post_Topic_Nick_Number, themeTitle, it.user.nick, it.id.id)
            val url = "https://4pda.to/forum/index.php?s=&showtopic=" + it.topicId.id + "&view=findpost&p=" + it.id.id
            viewState.showNoteCreate(title, url)
        }
    }

    override fun copyPostLink(postId: PostId) {
        getPostById(postId)?.let {
            val url =
                "https://4pda.to/forum/index.php?s=&showtopic=${it.topicId.id}&view=findpost&p=${it.id.id}"
            copyText(url)
        }
    }

    override fun sharePostLink(postId: PostId) {
        getPostById(postId)?.let {
            val url =
                "https://4pda.to/forum/index.php?s=&showtopic=${it.topicId.id}&view=findpost&p=${it.id.id}"
            shareText(url)
        }
    }

    override fun copyAnchorLink(postId: PostId, name: String) {
        getPostById(postId)?.let {
            val url = "https://4pda.to/forum/index.php?act=findpost&pid=${it.id.id}&anchor=$name"
            copyText(url)
        }
    }

    override fun copySpoilerLink(postId: PostId, spoilNumber: String) {
        getPostById(postId)?.let {
            val url =
                "https://4pda.to/forum/index.php?act=findpost&pid=${it.id.id}&anchor=Spoil-${it.id.id}-$spoilNumber"
            copyText(url)
        }
    }

    enum class ActionState(private val id: Int) {
        BACK(0),
        REFRESH(2),
        NORMAL(2);

        override fun toString() = id.toString()
    }
}