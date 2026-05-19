package forpdateam.ru.forpda.ui.fragments.qms.chat

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.nostra13.universalimageloader.core.ImageLoader
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.common.Utils
import forpdateam.ru.forpda.common.filepicker.registerFilesPicker
import forpdateam.ru.forpda.common.webview.CustomWebChromeClient
import forpdateam.ru.forpda.common.webview.CustomWebViewClient
import forpdateam.ru.forpda.common.webview.DialogsHelper
import forpdateam.ru.forpda.databinding.FragmentQmsChatBinding
import forpdateam.ru.forpda.databinding.ToolbarQmsNewThemeBinding
import forpdateam.ru.forpda.entity.remote.editpost.AttachmentItem
import forpdateam.ru.forpda.entity.remote.others.user.ForumUser
import forpdateam.ru.forpda.entity.remote.qms.QmsChatModel
import forpdateam.ru.forpda.entity.remote.qms.QmsMessage
import forpdateam.ru.forpda.entity.remote.qms.asRegular
import forpdateam.ru.forpda.extensions.quillMoxyPresenter
import forpdateam.ru.forpda.model.data.remote.api.RequestFile
import forpdateam.ru.forpda.model.repository.temp.TempHelper
import forpdateam.ru.forpda.presentation.LinkHandler
import forpdateam.ru.forpda.presentation.SystemLinkHandler
import forpdateam.ru.forpda.presentation.TabRouter
import forpdateam.ru.forpda.presentation.qms.chat.QmsChatPresenter
import forpdateam.ru.forpda.presentation.qms.chat.QmsChatTemplate
import forpdateam.ru.forpda.presentation.qms.chat.QmsChatView
import forpdateam.ru.forpda.ui.fragments.TabFragment
import forpdateam.ru.forpda.ui.fragments.TabTopScroller
import forpdateam.ru.forpda.ui.fragments.WebViewTopScroller
import forpdateam.ru.forpda.ui.fragments.notes.NotesAddPopup
import forpdateam.ru.forpda.ui.fragments.tabBinding
import forpdateam.ru.forpda.ui.fragments.tabToolbarBinding
import forpdateam.ru.forpda.ui.views.ExtendedWebView
import forpdateam.ru.forpda.ui.views.messagepanel.MessagePanel
import forpdateam.ru.forpda.ui.views.messagepanel.MessagePanel.HeightChangeListener
import forpdateam.ru.forpda.ui.views.messagepanel.attachments.AttachmentsPopup
import ru.radiationx.quill.inject

/**
 * Created by radiationx on 25.08.16.
 */
class QmsChatFragment : TabFragment(R.layout.fragment_qms_chat),
    ChatThemeCreator.ThemeCreatorInterface,
    ExtendedWebView.JsLifeCycleListener, QmsChatView, TabTopScroller {

    private val binding by tabBinding(FragmentQmsChatBinding::bind)
    private val toolbarBinding by tabToolbarBinding(ToolbarQmsNewThemeBinding::bind)

    private val chatContainer: FrameLayout
        get() = binding.qmsChatContainer
    private val progressBar: ProgressBar
        get() = binding.progressBar

    private lateinit var blackListMenuItem: MenuItem
    private lateinit var noteMenuItem: MenuItem
    private lateinit var toDialogsMenuItem: MenuItem
    private var themeCreator: ChatThemeCreator? = null
    private lateinit var webView: ExtendedWebView
    lateinit var messagePanel: MessagePanel
        private set
    private lateinit var attachmentsPopup: AttachmentsPopup
    private lateinit var jsInterface: QmsChatJsInterface

    private lateinit var topScroller: WebViewTopScroller

    private val qmsChatTemplate by inject<QmsChatTemplate>()
    private val utils by inject<Utils>()
    private val linkHandler by inject<LinkHandler>()
    private val systemLinkHandler by inject<SystemLinkHandler>()
    private val router by inject<TabRouter>()
    private val webViewClient by inject<CustomWebViewClient>()
    private val presenter by quillMoxyPresenter<QmsChatPresenter>()

    private val filesPicker = registerFilesPicker {
        uploadFiles(it)
    }

    init {
        configuration.defaultTitle = getString(R.string.fragment_title_chat)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            presenter.userId = getInt(USER_ID_ARG, QmsChatModel.NOT_CREATED)
            presenter.themeId = getInt(THEME_ID_ARG, QmsChatModel.NOT_CREATED)
            presenter.title = getString(THEME_TITLE_ARG)
            presenter.avatarUrl = getString(USER_AVATAR_ARG)
            presenter.nick = getString(USER_NICK_ARG)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        messagePanel = MessagePanel(requireContext(), fragmentContainer, coordinatorLayout, false)
        webView = ExtendedWebView(requireContext())
        webView.setDialogsHelper(
            DialogsHelper(
                context = webView.context,
                linkHandler = linkHandler,
                systemLinkHandler = systemLinkHandler,
                utils = utils,
                router = router
            )
        )
        attachWebView(webView)
        chatContainer.addView(webView, 0)
        attachmentsPopup = messagePanel.attachmentsPopup!!


        jsInterface = QmsChatJsInterface(presenter)
        webView.setJsLifeCycleListener(this)
        webView.addJavascriptInterface(jsInterface, JS_INTERFACE)
        registerForContextMenu(webView)
        webView.webViewClient = webViewClient
        webView.webChromeClient = CustomWebChromeClient()
        loadBaseWebContainer()

        attachmentsPopup.setEnabledTextControls(false)
        attachmentsPopup.setAddOnClickListener { filesPicker.launch() }
        attachmentsPopup.setDeleteOnClickListener {
            attachmentsPopup.preDeleteFiles()
            val selectedFiles = attachmentsPopup.getSelected()
            for (item in selectedFiles) {
                item.status = AttachmentItem.STATUS_REMOVED
            }
            attachmentsPopup.onDeleteFiles(selectedFiles)
        }


        /*attachmentsPopup.setInsertAttachmentListener { item ->
            String.format(Locale.getDefault(),
                    "\n[url=%s]Файл: %s, Размер: %s, Thumb: %s[/url]\n",
                    item.url,
                    item.name,
                    item.weight,
                    item.imageUrl)
        }*/

        messagePanel.addSendOnClickListener { presenter.onSendClick() }


        messagePanel.heightChangeListener = HeightChangeListener {
            webView.paddingBottom = it
        }

        topScroller = WebViewTopScroller(webView, appBarLayout)
    }

    override fun toggleScrollTop() {
        topScroller.toggleScrollTop()
    }

    override fun setFontSize(size: Int) {
        webView.setRelativeFontSize(size)
    }

    override fun addBaseToolbarMenu(menu: Menu) {
        super.addBaseToolbarMenu(menu)
        blackListMenuItem = menu
            .add(R.string.add_to_blacklist)
            .setOnMenuItemClickListener {
                presenter.blockUser()
                false
            }
        noteMenuItem = menu
            .add(R.string.create_note)
            .setOnMenuItemClickListener {
                presenter.createThemeNote()
                true
            }
        toDialogsMenuItem = menu
            .add(R.string.to_dialogs)
            .setOnMenuItemClickListener {
                presenter.openDialogs()
                true
            }
        refreshToolbarMenuItems(false)
    }

    override fun refreshToolbarMenuItems(enable: Boolean) {
        super.refreshToolbarMenuItems(enable)
        if (enable) {
            blackListMenuItem.isEnabled = true
            noteMenuItem.isEnabled = true
            toDialogsMenuItem.isEnabled = true
        } else {
            blackListMenuItem.isEnabled = false
            noteMenuItem.isEnabled = false
            toDialogsMenuItem.isEnabled = false
        }
    }

    override fun setRefreshing(isRefreshing: Boolean) {
        progressBar.visibility = if (isRefreshing) View.VISIBLE else View.GONE
        refreshToolbarMenuItems(!isRefreshing)
    }


    override fun setChatMode(mode: String) {
        if (mode == QmsChatPresenter.MODE_CHAT) {
            themeCreator?.setVisible(false)
        } else if (mode == QmsChatPresenter.MODE_CREATING) {
            if (themeCreator == null) {
                baseInflateToolbar(R.layout.toolbar_qms_new_theme)
                themeCreator = ChatThemeCreator(this, presenter, toolbarBinding)
            }
            themeCreator?.setVisible(true)
        }
    }

    //From theme creator
    override fun onCreateNewTheme(nick: String, title: String, message: String) {
        presenter.sendNewTheme(nick, title, message, attachmentsPopup.getAttachments())
    }

    override fun temp_sendMessage() {
        sendMessage()
    }

    override fun temp_sendNewTheme() {
        themeCreator?.sendNewTheme()
    }

    override fun onShowSearchRes(res: List<ForumUser>) {
        themeCreator?.onShowSearchRes(res)
    }

    override fun setStyleType(type: String) {
        webView.evalJs("changeStyleType(\"$type\")")
    }

    override fun showChat(data: QmsChatModel) {
        progressBar.visibility = View.GONE
        refreshToolbarMenuItems(true)
        setTitles(data.title.orEmpty(), data.user.nick.orEmpty())
    }

    override fun setTitles(title: String, nick: String) {
        setSubtitle(nick)
        setTitle(title)
        setTabTitle(getString(R.string.fragment_tab_title_chat, title, nick))
    }

    override fun onNewThemeCreate(data: QmsChatModel) {
        messagePanel.clearMessage()
        messagePanel.clearAttachments()
    }

    //Chat
    private fun loadBaseWebContainer() {
        val html = qmsChatTemplate.generateHtmlBase()
        webView.loadDataWithBaseURL("https://4pda.to/forum/", html, "text/html", "utf-8", null)
    }


    override fun onNewMessages(items: List<QmsMessage>) {
        Log.d(LOG_TAG, "Returned messages " + items.size)
        if (!items.isEmpty()) {
            val html = qmsChatTemplate.generate(items)
            val messagesSrc = TempHelper.transformMessageSrc(html)
            webView.evalJs("showNewMess('$messagesSrc', true)")
        }
    }

    override fun setMessageRefreshing(isRefreshing: Boolean) {
        messagePanel.setProgressState(isRefreshing)
    }

    override fun onSentMessage(items: List<QmsMessage>) {
        if (items.isNotEmpty() && items.getOrNull(0)?.asRegular()?.content != null) {
            //Empty because result returned from websocket
            messagePanel.clearMessage()
            messagePanel.clearAttachments()
        }
    }

    private fun sendMessage() {
        presenter.sendMessage(messagePanel.message, attachmentsPopup.getAttachments())
    }

    override fun showAvatar(avatarUrl: String) {
        toolbarImageView.contentDescription = getString(R.string.user_avatar)
        toolbarImageView.setOnClickListener { presenter.openProfile() }
        ImageLoader.getInstance().displayImage(avatarUrl, toolbarImageView)
        toolbarImageView.visibility = View.VISIBLE
    }

    override fun onBlockUser(res: Boolean) {
        if (res) {
            Toast.makeText(requireContext(), R.string.user_added_to_blacklist, Toast.LENGTH_SHORT)
                .show()
        }
    }

    override fun showCreateNote(name: String, nick: String, url: String) {
        val title = getString(R.string.dialog_Title_Nick, name, nick)
        NotesAddPopup.showAddNoteDialog(requireContext(), title, url)
    }

    override fun showMoreMessages(items: List<QmsMessage>, startIndex: Int, endIndex: Int) {
        val html = qmsChatTemplate.generate(items, startIndex, endIndex)
        val messagesSrc = TempHelper.transformMessageSrc(html)
        webView.evalJs("showMoreMess('$messagesSrc')")
    }

    override fun makeAllRead() {
        webView.evalJs("makeAllRead();")
    }

    override fun onDomContentComplete(actions: ArrayList<String>) {}

    override fun onPageComplete(actions: ArrayList<String>) {}

    /* ATTACHMENTS LOADER */

    fun uploadFiles(files: List<RequestFile>) {
        val pending = attachmentsPopup.preUploadFiles(files)
        presenter.uploadFiles(files, pending)
    }

    override fun onUploadFiles(items: List<AttachmentItem>) {
        attachmentsPopup.onUploadFiles(items)
    }

    override fun onBackPressed(): Boolean {
        super.onBackPressed()
        return messagePanel.onBackPressed()
    }

    override fun onResumeOrShow() {
        super.onResumeOrShow()
        messagePanel.onResume()
        presenter.checkNewMessages()
    }

    override fun onPauseOrHide() {
        super.onPauseOrHide()
        messagePanel.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        messagePanel.onDestroy()
        unregisterForContextMenu(webView)
        webView.removeJavascriptInterface(JS_INTERFACE)
        webView.setJsLifeCycleListener(null)
        webView.endWork()
    }

    override fun hideKeyboard() {
        super.hideKeyboard()
        messagePanel.hidePopupWindows()
    }

    companion object {
        private val LOG_TAG = QmsChatFragment::class.java.simpleName
        private val JS_INTERFACE = "IChat"
        const val USER_ID_ARG = "USER_ID_ARG"
        const val USER_NICK_ARG = "USER_NICK_ARG"
        const val USER_AVATAR_ARG = "USER_AVATAR_ARG"
        const val THEME_ID_ARG = "THEME_ID_ARG"
        const val THEME_TITLE_ARG = "THEME_TITLE_ARG"
    }
}
