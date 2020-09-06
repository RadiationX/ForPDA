package forpdateam.ru.forpda.presentation.theme

import android.net.Uri
import android.util.Log
import com.arellomobile.mvp.InjectViewState
import com.yandex.metrica.YandexMetrica
import forpdateam.ru.forpda.App
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.common.Utils
import forpdateam.ru.forpda.common.mvp.BasePresenter
import forpdateam.ru.forpda.entity.app.EditPostSyncData
import forpdateam.ru.forpda.entity.app.TabNotification
import forpdateam.ru.forpda.entity.app.profile.IUserHolder
import forpdateam.ru.forpda.entity.app.profile.UserHolder
import forpdateam.ru.forpda.entity.remote.IBaseForumPost
import forpdateam.ru.forpda.entity.remote.editpost.AttachmentItem
import forpdateam.ru.forpda.entity.remote.editpost.EditPostForm
import forpdateam.ru.forpda.entity.remote.events.NotificationEvent
import forpdateam.ru.forpda.entity.remote.search.SearchSettings
import forpdateam.ru.forpda.entity.remote.theme.ThemePage
import forpdateam.ru.forpda.model.AuthHolder
import forpdateam.ru.forpda.model.data.remote.api.RequestFile
import forpdateam.ru.forpda.model.data.remote.api.favorites.FavoritesApi
import forpdateam.ru.forpda.model.data.remote.api.theme.ThemeApi
import forpdateam.ru.forpda.model.interactors.CrossScreenInteractor
import forpdateam.ru.forpda.model.repository.events.EventsRepository
import forpdateam.ru.forpda.model.repository.faviorites.FavoritesRepository
import forpdateam.ru.forpda.model.repository.posteditor.PostEditorRepository
import forpdateam.ru.forpda.model.preferences.MainPreferencesHolder
import forpdateam.ru.forpda.model.preferences.OtherPreferencesHolder
import forpdateam.ru.forpda.model.preferences.TopicPreferencesHolder
import forpdateam.ru.forpda.model.repository.reputation.ReputationRepository
import forpdateam.ru.forpda.model.repository.theme.ThemeRepository
import forpdateam.ru.forpda.presentation.IErrorHandler
import forpdateam.ru.forpda.presentation.ILinkHandler
import forpdateam.ru.forpda.presentation.Screen
import forpdateam.ru.forpda.presentation.TabRouter
import forpdateam.ru.forpda.ui.TemplateManager
import forpdateam.ru.forpda.ui.activities.imageviewer.ImageViewerActivity
import forpdateam.ru.forpda.ui.fragments.theme.ThemeFragmentWeb
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

/**
 * Created by radiationx on 15.03.18.
 */
@InjectViewState
class ThemePresenter(
        private val themeRepository: ThemeRepository,
        private val reputationRepository: ReputationRepository,
        private val editorRepository: PostEditorRepository,
        private val favoritesRepository: FavoritesRepository,
        private val eventsRepository: EventsRepository,
        private val userHolder: IUserHolder,
        private val authHolder: AuthHolder,
        private val topicPreferencesHolder: TopicPreferencesHolder,
        private val mainPreferencesHolder: MainPreferencesHolder,
        private val otherPreferencesHolder: OtherPreferencesHolder,
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
        eventsRepository
                .observeEventsTab()
                .debounce(2L, TimeUnit.SECONDS)
                .subscribe {
                    handleEvent(it)
                }
                .untilDestroy()
        loadUrl(themeUrl)
    }

    override fun onDestroy() {
        super.onDestroy()
        router.removeResultListener(Screen.Theme.CODE_RESULT_SYNC)
        router.removeResultListener(Screen.Theme.CODE_RESULT_PAGE)
    }

    fun exit() {
        router.exit()
    }

    private fun handleEvent(event: TabNotification) {
        Log.e("SUKAT", "handleEvent " + event.isWebSocket + " : " + event.source + " : " + event.type)
        if (!event.isWebSocket)
            return
        if (!isPageLoaded())
            return
        Log.e("SUKAT", "handleEvent " + event.event.sourceId + " : " + getId())
        if (event.event.sourceId != getId())
            return
        if (event.event.userId == authHolder.get().userId)
            return

        if (event.source == NotificationEvent.Source.THEME) {
            when (event.type) {
                NotificationEvent.Type.NEW -> viewState.onEventNew(event)
                NotificationEvent.Type.READ -> viewState.onEventRead(event)
                NotificationEvent.Type.MENTION -> {
                }
                else -> {
                }
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
        themeRepository
                .getTheme(url, true, hatOpen, pollOpen)
                .map { themeTemplate.mapEntity(it) }
                .doOnSubscribe { viewState.setRefreshing(true) }
                .doAfterTerminate { viewState.setRefreshing(false) }
                .subscribe({
                    onLoadData(it)
                }, {
                    errorHandler.handle(it)
                })
                .untilDestroy()
    }

    private fun onLoadData(page: ThemePage) {
        if (page.pagination.current >= page.pagination.all) {
            crossScreenInteractor.onLoadTopic(page.id)
        }
        currentPage = page
        viewState.onLoadData(page)
        if (loadAction === ActionState.NORMAL) {
            saveToHistory(page)
        }
        if (loadAction === ActionState.REFRESH) {
            updateHistoryLast(page)
        }
    }

    fun addTopicToFavorite(topicId: Int, subType: String) {
        favoritesRepository
                .editFavorites(FavoritesApi.ACTION_ADD, -1, topicId, subType)
                .subscribe({
                    if (it) {
                        currentPage?.isInFavorite = true
                    }
                    viewState.onAddToFavorite(it)
                }, {
                    errorHandler.handle(it)
                })
                .untilDestroy()
    }

    fun deleteTopicFromFavorite(favId: Int) {
        favoritesRepository
                .editFavorites(FavoritesApi.ACTION_DELETE, favId, -1, null)
                .subscribe({
                    if (it) {
                        currentPage?.isInFavorite = false
                    }
                    viewState.onDeleteFromFavorite(it)
                }, {
                    errorHandler.handle(it)
                })
                .untilDestroy()
    }

    private fun createEditPostForm(message: String, attachments: MutableList<AttachmentItem>): EditPostForm? = currentPage?.let {
        val form = EditPostForm()
        form.forumId = it.forumId
        form.topicId = it.id
        form.st = it.pagination.current * it.pagination.perPage
        form.message = message
        form.attachments.addAll(attachments)
        form
    }

    fun openEditPostForm(message: String, attachments: MutableList<AttachmentItem>) {
        currentPage?.let { page ->
            createEditPostForm(message, attachments)?.let {
                router.navigateTo(Screen.EditPost().apply {
                    editPostForm = it
                    themeName = page.title
                })
                router.setResultListener(Screen.Theme.CODE_RESULT_SYNC, {
                    router.removeResultListener(Screen.Theme.CODE_RESULT_SYNC)
                    (it as? EditPostSyncData?)?.let {
                        if (it.topicId == page.id) {
                            viewState.syncEditPost(it)
                        }
                    }
                })
                router.setResultListener(Screen.Theme.CODE_RESULT_PAGE, {
                    router.removeResultListener(Screen.Theme.CODE_RESULT_PAGE)
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
                router.removeResultListener(Screen.Theme.CODE_RESULT_PAGE)
                (it as? ThemePage?)?.let { onLoadData(it) }
            })
        }
    }


    fun sendMessage(message: String, attachments: MutableList<AttachmentItem>) {
        createEditPostForm(message, attachments)?.let {
            viewState.setMessageRefreshing(true)
            editorRepository
                    .sendPost(it)
                    .map { themeTemplate.mapEntity(it) }
                    .doOnSubscribe { viewState.setMessageRefreshing(true) }
                    .doAfterTerminate { viewState.setMessageRefreshing(false) }
                    .subscribe({
                        onLoadData(it)
                        viewState.onMessageSent()
                    }, {
                        errorHandler.handle(it)
                    })
                    .untilDestroy()
        }
    }

    fun uploadFiles(files: List<RequestFile>, pending: List<AttachmentItem>) {
        editorRepository
                .uploadFiles(0, files, pending)
                .subscribe({
                    viewState.onUploadFiles(it)
                }, {
                    errorHandler.handle(it)
                })
                .untilDestroy()
    }

    fun deleteFiles(items: List<AttachmentItem>) {
        editorRepository
                .deleteFiles(0, items)
                .subscribe({
                    viewState.onDeleteFiles(it)
                }, {
                    errorHandler.handle(it)
                })
                .untilDestroy()
    }

    fun loadUrl(url: String) {
        loadData(url, ActionState.NORMAL)
    }

    fun reload() {
        loadData(themeUrl, ActionState.REFRESH)
    }

    fun loadNewPosts() {
        currentPage?.let {
            loadUrl("https://4pda.ru/forum/index.php?showtopic=${it.id}&view=getnewpost")
        }
    }

    fun loadPage(page: Int) {
        currentPage?.let {
            var url = "https://4pda.ru/forum/index.php?showtopic=${it.id}"
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

    private fun saveToHistory(themePage: ThemePage) {
        history.add(themePage)
    }

    private fun updateHistoryLast(themePage: ThemePage) {
        if (history.isNotEmpty()) {
            history.last().let {
                themePage.anchors.addAll(it.anchors)
                themePage.scrollY = it.scrollY
            }
            history[history.size - 1] = themePage
        }
    }

    fun updateHistoryLastHtml(html: String, scrollY: Int) {
        if (history.isNotEmpty()) {
            history.last().let {
                it.scrollY = scrollY
                it.html = html
            }
        }
    }

    override fun shareText(text: String) {
        Utils.shareText(text)
    }

    fun copyLink() {
        currentPage?.let {
            Utils.copyToClipBoard("https://4pda.ru/forum/index.php?showtopic=${it.id}")
        }
    }

    fun openSearch() {
        currentPage?.let {
            linkHandler.handle("https://4pda.ru/forum/index.php?forums=${it.forumId}&topics=${it.id}&act=search&source=pst&result=posts", router)
        }
    }

    fun openSearchMyPosts() {
        currentPage?.let {
            var url = ("https://4pda.ru/forum/index.php?forums=${it.forumId}&topics=${it.id}&act=search&source=pst&result=posts&username=")

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
            linkHandler.handle("https://4pda.ru/forum/index.php?showforum=${it.forumId}", router)
        }
    }


    private fun getPostById(postId: Int): IBaseForumPost? = currentPage
            ?.posts
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

    override fun onReplyPostClick(postId: Int) {
        getPostById(postId)?.let {
            val text = "[snapback]${it.id}[/snapback] [b]${it.nick},[/b] \n"
            viewState.insertText(text)
        }
    }

    override fun onQuotePostClick(postId: Int, text: String) {
        getPostById(postId)?.let {
            val date = Utils.getForumDateTime(Utils.parseForumDateTime(it.date))
            val insert = "[quote name=\"${it.nick}\" date=\"$date\" post=${it.id}]$text[/quote]\n"
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
        currentPage?.let { it.isPollOpen = bValue }
    }

    override fun onHatHeaderClick(bValue: Boolean) {
        currentPage?.let { it.isHatOpen = bValue }
    }

    override fun setHistoryBody(index: Int, body: String) {
        history[index].html = body
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
            if (uri.host != null && uri.host.matches("4pda.ru".toRegex())) {
                if (uri.pathSegments[0] == "forum") {
                    var param: String? = uri.getQueryParameter("showtopic")
                    Log.d(LOG_TAG, "param showtopic: $param")
                    if (param != null && param != Uri.parse(themeUrl).getQueryParameter("showtopic")) {
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
                            val finalAnchor = (if (elem == null) "entry" else "") + if (elem != null) elem else postId
                            currentPage?.let {
                                if (topicPreferencesHolder.getAnchorHistory()) {
                                    it.addAnchor(finalAnchor)
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
                                ImageViewerActivity.startActivity(App.getContext(), list, post.attachImages.indexOf(image))
                                return
                            }
                        }
                    }
                }
            }
        } catch (ex: Exception) {
            YandexMetrica.reportError("${ex.message ?: ex.toString()}; uri $uri", ex)
            //ACRA.getErrorReporter().handleException(ex)
        }
        linkHandler.handle(url, router)
    }

    private fun checkIsPoll(url: String): Boolean {
        currentPage?.let {
            val m = Pattern.compile("4pda.ru.*?addpoll=1").matcher(url)
            if (m.find()) {
                var uri = Uri.parse(url)
                uri = uri.buildUpon()
                        .appendQueryParameter("showtopic", Integer.toString(it.id))
                        .appendQueryParameter("st", "" + it.pagination.current * it.pagination.perPage)
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
                    it.removeAnchor()
                    viewState.scrollToAnchor(it.anchor)
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
            currentPage?.let {
                themeRepository
                        .reportPost(it.id, post.id, message)
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
            val themeTitle: String = currentPage?.title.orEmpty()
            val title = String.format(App.get().getString(R.string.post_Topic_Nick_Number), themeTitle, it.nick, it.id)
            val url = "https://4pda.ru/forum/index.php?s=&showtopic=" + it.topicId + "&view=findpost&p=" + it.id
            viewState.showNoteCreate(title, url)
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

    enum class ActionState(private val id: Int) {
        BACK(0),
        REFRESH(2),
        NORMAL(2);

        override fun toString() = id.toString()
    }
}