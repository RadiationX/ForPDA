package forpdateam.ru.forpda.presentation.theme

import android.net.Uri
import android.util.Log
import com.yandex.metrica.YandexMetrica
import forpdateam.ru.forpda.App
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.common.Utils
import forpdateam.ru.forpda.common.mvp.BasePresenter
import forpdateam.ru.forpda.entity.app.EditPostSyncData
import forpdateam.ru.forpda.entity.app.profile.IUserHolder
import forpdateam.ru.forpda.entity.asDeferredData
import forpdateam.ru.forpda.entity.remote.ForumPost
import forpdateam.ru.forpda.entity.remote.editpost.AttachmentItem
import forpdateam.ru.forpda.entity.remote.editpost.EditPostForm
import forpdateam.ru.forpda.entity.remote.events.WebSocketEvent
import forpdateam.ru.forpda.entity.remote.search.SearchSettings
import forpdateam.ru.forpda.entity.remote.theme.ThemePage
import forpdateam.ru.forpda.entity.remote.theme.ThemePost
import forpdateam.ru.forpda.extensions.coRunCatching
import forpdateam.ru.forpda.extensions.replaceAt
import forpdateam.ru.forpda.model.AuthHolder
import forpdateam.ru.forpda.model.data.remote.api.RequestFile
import forpdateam.ru.forpda.model.data.remote.api.favorites.FavoritesApi
import forpdateam.ru.forpda.model.data.remote.api.theme.ThemeApi
import forpdateam.ru.forpda.model.interactors.CrossScreenInteractor
import forpdateam.ru.forpda.model.interactors.events.EventsController
import forpdateam.ru.forpda.model.preferences.MainPreferencesHolder
import forpdateam.ru.forpda.model.preferences.TopicPreferencesHolder
import forpdateam.ru.forpda.model.repository.faviorites.FavoritesRepository
import forpdateam.ru.forpda.model.repository.posteditor.PostEditorRepository
import forpdateam.ru.forpda.model.repository.reputation.ReputationRepository
import forpdateam.ru.forpda.model.repository.theme.ThemeRepository
import forpdateam.ru.forpda.presentation.IErrorHandler
import forpdateam.ru.forpda.presentation.ILinkHandler
import forpdateam.ru.forpda.presentation.Screen
import forpdateam.ru.forpda.presentation.TabRouter
import forpdateam.ru.forpda.ui.TemplateManager
import forpdateam.ru.forpda.ui.activities.imageviewer.ImageViewerActivity
import forpdateam.ru.forpda.ui.fragments.theme.ThemeFragmentWeb
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import moxy.InjectViewState
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.util.regex.Pattern
import kotlin.time.Duration.Companion.seconds

/**
 * Created by radiationx on 15.03.18.
 */
@InjectViewState
class ThemePresenter(
    private val themeRepository: ThemeRepository,
    private val reputationRepository: ReputationRepository,
    private val editorRepository: PostEditorRepository,
    private val favoritesRepository: FavoritesRepository,
    private val eventsController: EventsController,
    private val userHolder: IUserHolder,
    private val authHolder: AuthHolder,
    private val topicPreferencesHolder: TopicPreferencesHolder,
    private val mainPreferencesHolder: MainPreferencesHolder,
    private val crossScreenInteractor: CrossScreenInteractor,
    private val themeTemplate: ThemeTemplate,
    private val templateManager: TemplateManager,
    private val router: TabRouter,
    private val linkHandler: ILinkHandler,
    private val errorHandler: IErrorHandler
) : BasePresenter<ThemeView>(), IThemePresenter {

    var loadAction = ActionState.NORMAL
    var currentPage: ThemePage? = null
    var history = mutableListOf<ThemePage>()
    var themeUrl: String = ""

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        topicPreferencesHolder
            .observeShowAvatars()
            .onEach {
                viewState.updateShowAvatarState(it)
            }
            .launchIn(viewModelScope)

        topicPreferencesHolder
            .observeCircleAvatars()
            .onEach {
                viewState.updateTypeAvatarState(it)
            }
            .launchIn(viewModelScope)

        mainPreferencesHolder
            .observeScrollButtonEnabled()
            .onEach {
                viewState.updateScrollButtonState(it)
            }
            .launchIn(viewModelScope)

        mainPreferencesHolder
            .observeWebViewFontSize()
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
        eventsController
            .observeWebSocketEvents()
            .debounce(2.seconds)
            .onEach {
                handleEvent(it)
            }
            .launchIn(viewModelScope)
        loadUrl(themeUrl)
    }

    fun exit() {
        router.exit()
    }

    private fun handleEvent(event: WebSocketEvent) {
        if (event !is WebSocketEvent.Topic) {
            return
        }
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

    private fun loadData(url: String, action: ActionState) {
        var hatOpen = false
        var pollOpen = false
        currentPage?.let {
            hatOpen = it.isHatOpen
            pollOpen = it.isPollOpen
        }
        themeUrl = url
        loadAction = action
        viewState.updateHistoryLastHtml()
        viewModelScope.launch {
            viewState.setRefreshing(true)
            coRunCatching {
                themeRepository.getTheme(url, hatOpen, pollOpen)
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
        currentPage = page
        viewState.onLoadData(page)
        if (loadAction === ActionState.NORMAL) {
            saveToHistory()
        }
        if (loadAction === ActionState.REFRESH) {
            updateHistoryLast()
        }
    }

    fun addTopicToFavorite(topicId: Int, subType: String) {
        viewModelScope.launch {
            coRunCatching {
                favoritesRepository.editFavorites(FavoritesApi.ACTION_ADD, -1, topicId, subType)
            }.onSuccess {
                if (it) {
                    currentPage = currentPage?.copy(isInFavorite = true)
                }
                viewState.onAddToFavorite(it)
            }.onFailure {
                errorHandler.handle(it)
            }
        }
    }

    fun deleteTopicFromFavorite(favId: Int) {
        viewModelScope.launch {
            coRunCatching {
                favoritesRepository.editFavorites(FavoritesApi.ACTION_DELETE, favId, -1, null)
            }.onSuccess {
                if (it) {
                    currentPage = currentPage?.copy(isInFavorite = false)
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
        form.st = it.pagination.currentPage()
        form.message = message
        form.attachments.addAll(attachments)
        form
    }

    fun openEditPostForm(message: String, attachments: List<AttachmentItem>) {
        currentPage?.let { page ->
            createEditPostForm(message, attachments)?.let {
                router.navigateTo(Screen.EditPost().apply {
                    editPostForm = it
                    themeName = page.title
                })
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

    fun openEditPostForm(postId: Int) {
        currentPage?.let {
            router.navigateTo(Screen.EditPost().apply {
                this.postId = postId
                topicId = it.id
                forumId = it.forumId
                st = it.st
                themeName = it.title
            })
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
                editorRepository.uploadFiles(0, files, pending)
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
                editorRepository.deleteFiles(0, items)
            }.onSuccess {
                viewState.onDeleteFiles(it)
            }.onFailure {
                errorHandler.handle(it)
            }
        }
    }

    fun loadUrl(url: String) {
        loadData(url, ActionState.NORMAL)
    }

    fun reload() {
        loadData(themeUrl, ActionState.REFRESH)
    }

    fun loadNewPosts() {
        currentPage?.let {
            loadUrl("https://4pda.to/forum/index.php?showtopic=${it.id}&view=getnewpost")
        }
    }

    fun loadPage(page: Int) {
        currentPage?.let {
            var url = "https://4pda.to/forum/index.php?showtopic=${it.id}"
            if (page != 0) {
                url = "$url&st=$page"
            }
            loadUrl(url)
        }
    }

    fun backPage() {
        if (history.size > 1) {
            loadAction = ActionState.BACK
            history.removeAt(history.size - 1)
            history.last().let {
                currentPage = it
                themeUrl = it.url.orEmpty()
                viewState.updateView(it)
            }
        }
    }

    override fun onPollResultsClick() {
        val url = themeUrl
            .replaceFirst("#[^&]*", "")
            .replace("&mode=show", "")
            .replace("&poll_open=true", "") + "&mode=show&poll_open=true"
        loadUrl(url)
    }

    override fun onPollClick() {
        val url = themeUrl
            .replaceFirst("#[^&]*", "")
            .replace("&mode=show", "")
            .replace("&poll_open=true", "") + "&poll_open=true"
        loadUrl(url)
    }

    private fun saveToHistory() {
        currentPage?.also { history.add(it) }
    }

    private fun updateHistoryLast() {
        val page = currentPage ?: return
        if (history.isNotEmpty()) {
            val newPage = history.last().let {
                page.copy(
                    anchors = page.anchors + it.anchors,
                    scrollY = it.scrollY
                )
            }
            currentPage = newPage
            history.replaceAt(history.lastIndex) { newPage }
        }
    }

    fun updateHistoryLastHtml(html: String, scrollY: Int) {
        if (history.isNotEmpty()) {
            history.replaceAt(history.lastIndex) {
                it.copy(
                    scrollY = scrollY,
                    html = html.asDeferredData()
                )
            }
        }
    }

    override fun shareText(text: String) {
        Utils.shareText(text)
    }

    fun copyLink() {
        currentPage?.let {
            Utils.copyToClipBoard("https://4pda.to/forum/index.php?showtopic=${it.id}")
        }
    }

    fun openSearch() {
        currentPage?.let {
            linkHandler.handle(
                "https://4pda.to/forum/index.php?forums=${it.forumId}&topics=${it.id}&act=search&source=pst&result=posts",
                router
            )
        }
    }

    fun openSearchMyPosts() {
        currentPage?.let {
            var url =
                ("https://4pda.to/forum/index.php?forums=${it.forumId}&topics=${it.id}&act=search&source=pst&result=posts&username=")

            try {
                url += URLEncoder.encode(userHolder.user?.nick.orEmpty(), "windows-1251")
            } catch (e: UnsupportedEncodingException) {
                e.printStackTrace()
            }

            linkHandler.handle(url, router)
        }
    }

    fun openForum() {
        currentPage?.let {
            linkHandler.handle("https://4pda.to/forum/index.php?showforum=${it.forumId}", router)
        }
    }

    private fun getThemePostById(postId: Int): ThemePost? = currentPage
        ?.posts
        ?.firstOrNull { it.post.id == postId }

    private fun getPostById(postId: Int): ForumPost? = getThemePostById(postId)?.post


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

    override fun onReplyPostClick(postId: Int) {
        getPostById(postId)?.let {
            val text = "[snapback]${it.id}[/snapback] [b]${it.user.nick},[/b] \n"
            viewState.insertText(text)
        }
    }

    override fun onQuotePostClick(postId: Int, text: String) {
        getPostById(postId)?.let {
            val date = Utils.getForumDateTime(Utils.parseForumDateTime(it.date))
            val insert =
                "[quote name=\"${it.user.nick}\" date=\"$date\" post=${it.id}]$text[/quote]\n"
            viewState.insertText(insert)
        }
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

    override fun onPollHeaderClick(bValue: Boolean) {
        currentPage = currentPage?.copy(isPollOpen = bValue)
    }

    override fun onHatHeaderClick(bValue: Boolean) {
        currentPage = currentPage?.copy(isHatOpen = bValue)
    }

    override fun setHistoryBody(index: Int, body: String) {
        history[index] = history[index].copy(html = body.asDeferredData())
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

    private val LOG_TAG = ThemeFragmentWeb::class.java.simpleName
    fun handleNewUrl(uri: Uri) {
        Log.d(LOG_TAG, "handle $uri")
        val url = uri.toString()
        try {
            if (checkIsPoll(url)) {
                return
            }
            if (uri.host != null && uri.host?.matches("4pda.to".toRegex()) == true) {
                if (uri.pathSegments[0] == "forum") {
                    var param: String? = uri.getQueryParameter("showtopic")
                    Log.d(LOG_TAG, "param showtopic: $param")
                    if (param != null && param != Uri.parse(themeUrl)
                            .getQueryParameter("showtopic")
                    ) {
                        loadUrl(url)
                        return
                    }
                    param = uri.getQueryParameter("act")
                    if (param == null)
                        param = uri.getQueryParameter("view")
                    Log.d(LOG_TAG, "param act|view: $param")
                    if (param != null && param == "findpost") {
                        var postId: String? = uri.getQueryParameter("pid")
                        if (postId == null)
                            postId = uri.getQueryParameter("p")
                        Log.d(LOG_TAG, "param pid|p: $postId")
                        if (postId != null) {
                            postId = postId.replace("[^\\d][\\s\\S]*?".toRegex(), "")
                        }
                        Log.d(LOG_TAG, "param postId: $postId")
                        if (postId != null && getPostById(Integer.parseInt(postId.trim { it <= ' ' })) != null) {
                            val matcher = ThemeApi.elemToScrollPattern.matcher(url)
                            var elem: String? = null
                            while (matcher.find()) {
                                elem = matcher.group(1)
                            }
                            Log.d(LOG_TAG, " scroll to $postId : $elem")
                            val finalAnchor = (if (elem == null) "entry" else "") + (elem ?: postId)
                            if (topicPreferencesHolder.getAnchorHistory()) {
                                currentPage = currentPage?.let {
                                    it.copy(anchors = it.anchors + finalAnchor)
                                }
                            }

                            viewState.scrollToAnchor(finalAnchor)
                            return
                        } else {
                            loadUrl(url)
                            return
                        }
                    }
                }
            }

            if (ThemeApi.attachImagesPattern.matcher(url).find()) {
                currentPage?.let {
                    for (post in it.posts) {
                        for (image in post.attachImages) {
                            if (image.first.contains(url)) {
                                val list = ArrayList<String>()
                                for (attaches in post.attachImages) {
                                    list.add(attaches.first)
                                }
                                ImageViewerActivity.startActivity(
                                    App.getContext(),
                                    list,
                                    post.attachImages.indexOf(image)
                                )
                                return
                            }
                        }
                    }
                }
            }
        } catch (ex: Exception) {
            YandexMetrica.reportError("${ex.message ?: ex.toString()}; uri $uri", ex)
        }
        linkHandler.handle(url, router)
    }

    private fun checkIsPoll(url: String): Boolean {
        currentPage?.let {
            val m = Pattern.compile("4pda.to.*?addpoll=1").matcher(url)
            if (m.find()) {
                var uri = Uri.parse(url)
                uri = uri.buildUpon()
                    .appendQueryParameter("showtopic", Integer.toString(it.id))
                    .appendQueryParameter("st", "${it.pagination.currentPage()}")
                    .build()
                loadUrl(uri.toString())
                return true
            }
        }
        return false
    }


    fun onClickDeleteInFav() {
        currentPage?.let { viewState.showDeleteInFavDialog(it) }
    }

    fun onClickAddInFav() {
        currentPage?.let { viewState.showAddInFavDialog(it) }
    }

    fun onBackPressed(): Boolean {
        if (topicPreferencesHolder.getAnchorHistory()) {
            currentPage?.let {
                if (it.anchors.size > 1) {
                    val newAnchors = it.anchors.toMutableList()
                    newAnchors.removeAt(newAnchors.lastIndex)
                    val newPage = it.copy(anchors = newAnchors)
                    currentPage = newPage
                    viewState.scrollToAnchor(newPage.anchor)
                    return true
                }
            }
        }
        if (history.size > 1) {
            backPage()
            return true
        }
        return false
    }


    override fun openProfile(postId: Int) {
        getPostById(postId)?.let {
            linkHandler.handle("https://4pda.to/forum/index.php?showuser=${it.user.id}", router)
        }
    }

    override fun openQms(postId: Int) {
        getPostById(postId)?.let {
            linkHandler.handle(
                "https://4pda.to/forum/index.php?act=qms&amp;mid=${it.user.id}",
                router
            )
        }
    }

    override fun openSearchUserTopic(postId: Int) {
        getPostById(postId)?.let {
            linkHandler.handle(
                SearchSettings.default().copy(
                    source = SearchSettings.SOURCE_ALL.first,
                    nick = it.user.nick,
                    result = SearchSettings.RESULT_TOPICS.first
                ).toUrl(),
                router
            )
        }
    }

    override fun openSearchInTopic(postId: Int) {
        getThemePostById(postId)?.let {
            val post = it.post
            linkHandler.handle(
                SearchSettings.default().copy(
                    forums = listOf(it.forumId),
                    topics = listOf(post.topicId),
                    source = SearchSettings.SOURCE_CONTENT.first,
                    nick = post.user.nick,
                    result = SearchSettings.RESULT_POSTS.first,
                    subforums = SearchSettings.SUB_FORUMS_FALSE
                ).toUrl(),
                router
            )
        }
    }

    override fun openSearchUserMessages(postId: Int) {
        getPostById(postId)?.let {
            linkHandler.handle(
                SearchSettings.default().copy(
                    source = SearchSettings.SOURCE_CONTENT.first,
                    nick = it.user.nick,
                    result = SearchSettings.RESULT_POSTS.first,
                    subforums = SearchSettings.SUB_FORUMS_FALSE
                ).toUrl(),
                router
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
                    reputationRepository.changeReputation(it.id, it.user.id, type, message)
                }.onSuccess {
                    router.showSystemMessage(App.get().getString(R.string.reputation_changed))
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
                    themeRepository.votePost(it.id, type)
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
                "https://4pda.to/forum/index.php?act=rep&view=history&amp;mid=${it.user.id}",
                router
            )
        }
    }

    override fun quoteFromBuffer(postId: Int) {
        getPostById(postId)?.let {
            val text = Utils.readFromClipboard()
            if (!text.isNullOrEmpty()) {
                onQuotePostClick(postId, text)
            }
        }
    }

    override fun reportPost(postId: Int, message: String) {
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

    override fun deletePost(postId: Int) {
        getPostById(postId)?.let { post ->
            viewModelScope.launch {
                coRunCatching {
                    themeRepository.deletePost(post.id)
                }.onSuccess {
                    viewState.deletePostUi(post)
                    router.showSystemMessage(App.get().getString(R.string.message_deleted))
                }.onFailure {
                    errorHandler.handle(it)
                }
            }
        }
    }

    override fun createNote(postId: Int) {
        getPostById(postId)?.let {
            val themeTitle: String = currentPage?.title.orEmpty()
            val title = String.format(
                App.get().getString(R.string.post_Topic_Nick_Number),
                themeTitle,
                it.user.nick,
                it.id
            )
            val url =
                "https://4pda.to/forum/index.php?s=&showtopic=" + it.topicId + "&view=findpost&p=" + it.id
            viewState.showNoteCreate(title, url)
        }
    }

    override fun copyPostLink(postId: Int) {
        getPostById(postId)?.let {
            val url =
                "https://4pda.to/forum/index.php?s=&showtopic=${it.topicId}&view=findpost&p=${it.id}"
            copyText(url)
        }
    }

    override fun sharePostLink(postId: Int) {
        getPostById(postId)?.let {
            val url =
                "https://4pda.to/forum/index.php?s=&showtopic=${it.topicId}&view=findpost&p=${it.id}"
            shareText(url)
        }
    }

    override fun copyAnchorLink(postId: Int, name: String) {
        getPostById(postId)?.let {
            val url = "https://4pda.to/forum/index.php?act=findpost&pid=${it.id}&anchor=$name"
            copyText(url)
        }
    }

    override fun copySpoilerLink(postId: Int, spoilNumber: String) {
        getPostById(postId)?.let {
            val url =
                "https://4pda.to/forum/index.php?act=findpost&pid=${it.id}&anchor=Spoil-${it.id}-$spoilNumber"
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