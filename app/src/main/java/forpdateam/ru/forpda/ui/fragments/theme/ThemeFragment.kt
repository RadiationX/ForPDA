package forpdateam.ru.forpda.ui.fragments.theme

import android.annotation.SuppressLint
import android.app.Activity
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.annotation.CallSuper
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.SearchView
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.widget.*
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import forpdateam.ru.forpda.App
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.common.FilePickHelper
import forpdateam.ru.forpda.entity.app.EditPostSyncData
import forpdateam.ru.forpda.entity.app.TabNotification
import forpdateam.ru.forpda.entity.remote.IBaseForumPost
import forpdateam.ru.forpda.entity.remote.editpost.AttachmentItem
import forpdateam.ru.forpda.entity.remote.theme.ThemePage
import forpdateam.ru.forpda.model.data.remote.api.RequestFile
import forpdateam.ru.forpda.model.data.remote.api.favorites.FavoritesApi
import forpdateam.ru.forpda.presentation.theme.ThemePresenter
import forpdateam.ru.forpda.presentation.theme.ThemeView
import forpdateam.ru.forpda.ui.fragments.TabFragment
import forpdateam.ru.forpda.ui.fragments.favorites.FavoritesFragment
import forpdateam.ru.forpda.ui.fragments.notes.NotesAddPopup
import forpdateam.ru.forpda.ui.views.FabOnScroll
import forpdateam.ru.forpda.ui.views.messagepanel.MessagePanel
import forpdateam.ru.forpda.ui.views.messagepanel.attachments.AttachmentsPopup
import forpdateam.ru.forpda.ui.views.pagination.PaginationHelper

/**
 * Created by radiationx on 20.10.16.
 */

abstract class ThemeFragment : TabFragment(), ThemeView {

    protected lateinit var dialogsHelper: ThemeDialogsHelper_V2

    protected lateinit var toggleMessagePanelItem: MenuItem
    protected lateinit var refreshMenuItem: MenuItem
    protected lateinit var copyLinkMenuItem: MenuItem
    protected lateinit var searchOnPageMenuItem: MenuItem
    protected lateinit var searchInThemeMenuItem: MenuItem
    protected lateinit var searchPostsMenuItem: MenuItem
    protected lateinit var deleteFavoritesMenuItem: MenuItem
    protected lateinit var addFavoritesMenuItem: MenuItem
    protected lateinit var openForumMenuItem: MenuItem

    protected lateinit var refreshLayout: androidx.swiperefreshlayout.widget.SwipeRefreshLayout

    private lateinit var paginationHelper: PaginationHelper

    //Тег для вьюхи поиска. Чтобы создавались кнопки и т.д, только при вызове поиска, а не при каждом создании меню.
    protected var searchViewTag = 0

    lateinit var messagePanel: MessagePanel
        protected set
    lateinit var attachmentsPopup: AttachmentsPopup
        protected set

    private lateinit var notificationView: View
    private lateinit var notificationTitle: TextView
    private lateinit var notificationButton: ImageButton

    private val authHolder = App.get().Di().authHolder
    private val mainPreferencesHolder = App.get().Di().mainPreferencesHolder
    private val otherPreferencesHolder = App.get().Di().otherPreferencesHolder

    @InjectPresenter
    lateinit var presenter: ThemePresenter

    @ProvidePresenter
    fun providePresenter(): ThemePresenter = ThemePresenter(
            App.get().Di().themeRepository,
            App.get().Di().reputationRepository,
            App.get().Di().editPostRepository,
            App.get().Di().favoritesRepository,
            App.get().Di().eventsRepository,
            App.get().Di().userHolder,
            App.get().Di().authHolder,
            App.get().Di().topicPreferencesHolder,
            App.get().Di().mainPreferencesHolder,
            App.get().Di().otherPreferencesHolder,
            App.get().Di().crossScreenInteractor,
            App.get().Di().themeTemplate,
            App.get().Di().templateManager,
            App.get().Di().router,
            App.get().Di().linkHandler,
            App.get().Di().errorHandler
    )

    override fun onEventNew(event: TabNotification) {
        Log.d("SUKAT", "onEventNew")
        notificationView.visibility = View.VISIBLE
    }

    override fun onEventRead(event: TabNotification) {
        Log.d("SUKAT", "onEventRead")
        notificationView.visibility = View.GONE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            presenter.themeUrl = getString(TabFragment.ARG_TAB, "")
        }
        dialogsHelper = ThemeDialogsHelper_V2(context, authHolder, otherPreferencesHolder)
    }

    override fun initFabBehavior() {
        val params = fab.layoutParams as androidx.coordinatorlayout.widget.CoordinatorLayout.LayoutParams
        val behavior = FabOnScroll(fab.context, null)
        params.behavior = behavior
        params.gravity = Gravity.CENTER_VERTICAL or Gravity.END
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
            params.setMargins(App.px16, App.px16, App.px16, App.px16)
        }
        fab.requestLayout()
    }


    @SuppressLint("InflateParams")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        initFabBehavior()
        baseInflateFragment(inflater, R.layout.fragment_theme)
        refreshLayout = findViewById(R.id.swipe_refresh_list) as androidx.swiperefreshlayout.widget.SwipeRefreshLayout
        messagePanel = MessagePanel(context, fragmentContainer, coordinatorLayout, false)
        paginationHelper = PaginationHelper(activity)
        paginationHelper.addInToolbar(inflater, toolbarLayout, configuration.isFitSystemWindow)

        notificationView = inflater.inflate(R.layout.new_message_notification, null)
        notificationTitle = notificationView.findViewById<View>(R.id.title) as TextView
        notificationButton = notificationView.findViewById<View>(R.id.icon) as ImageButton
        fragmentContent.addView(notificationView)
        notificationView.layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        contentController.setMainRefresh(refreshLayout)
        return viewFragment
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setFontSize(mainPreferencesHolder.getWebViewFontSize())

        notificationButton.setColorFilter(App.getColorFromAttr(context, R.attr.contrast_text_color), PorterDuff.Mode.SRC_ATOP)
        notificationTitle.text = "Новое сообщение"
        notificationView.visibility = View.GONE
        notificationButton.setOnClickListener { notificationView.visibility = View.GONE }
        notificationView
                .findViewById<View>(R.id.new_message_card)
                .setOnClickListener {
                    presenter.loadNewPosts()
                    notificationView.visibility = View.GONE
                }

        messagePanel.enableBehavior()
        messagePanel.addSendOnClickListener { sendMessage() }
        messagePanel.sendButton.setOnLongClickListener {
            presenter.openEditPostForm(messagePanel.message, messagePanel.attachments)
            true
        }
        messagePanel.fullButton.visibility = View.VISIBLE
        messagePanel.fullButton.setOnClickListener { presenter.openEditPostForm(messagePanel.message, messagePanel.attachments) }
        messagePanel.hideButton.visibility = View.VISIBLE
        messagePanel.hideButton.setOnClickListener { hideMessagePanel() }
        attachmentsPopup = messagePanel.attachmentsPopup
        attachmentsPopup.setAddOnClickListener { tryPickFile() }
        attachmentsPopup.setDeleteOnClickListener { removeFiles() }


        paginationHelper.setListener(object : PaginationHelper.PaginationListener {
            override fun onTabSelected(tab: TabLayout.Tab): Boolean {
                return refreshLayout.isRefreshing
            }

            override fun onSelectedPage(pageNumber: Int) {
                presenter.loadPage(pageNumber)
            }
        })

        fab.size = FloatingActionButton.SIZE_MINI
        if (mainPreferencesHolder.getScrollButtonEnabled()) {
            fab.visibility = View.VISIBLE
        } else {
            fab.visibility = View.GONE
        }
        fab.scaleX = 0.0f
        fab.scaleY = 0.0f
        fab.alpha = 0.0f

        refreshLayoutStyle(refreshLayout)
        refreshLayout.setOnRefreshListener { presenter.reload() }


        if (mainPreferencesHolder.getEditorDefaultHidden()) {
            hideMessagePanel()
        } else {
            showMessagePanel(false)
        }
    }

    override fun onResumeOrShow() {
        super.onResumeOrShow()
        messagePanel.onResume()
    }

    override fun onPauseOrHide() {
        super.onPauseOrHide()
        messagePanel.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        messagePanel.onDestroy()
        paginationHelper.destroy()
    }

    override fun hideKeyboard() {
        super.hideKeyboard()
        messagePanel.hidePopupWindows()
    }

    override fun onBackPressed(): Boolean {
        super.onBackPressed()

        if (messagePanel.onBackPressed()) {
            return true
        }

        if (toolbar.menu.findItem(R.id.action_search) != null && toolbar.menu.findItem(R.id.action_search).isActionViewExpanded) {
            toolbar.collapseActionView()
            return true
        }

        if (presenter.onBackPressed()) {
            return true
        }

        if (messagePanel.message != null && !messagePanel.message.isEmpty() || !messagePanel.attachments.isEmpty()) {
            AlertDialog.Builder(context!!)
                    .setMessage(R.string.editpost_lose_changes)
                    .setPositiveButton(R.string.ok) { _, _ ->
                        presenter.exit()
                    }
                    .setNegativeButton(R.string.no, null)
                    .show()
            return true
        }

        return false
    }


    override fun setRefreshing(isRefreshing: Boolean) {
        super.setRefreshing(isRefreshing)
        refreshToolbarMenuItems(!isRefreshing)
    }

    override fun onLoadData(newPage: ThemePage) {
        appBarLayout.setExpanded(false, true)
        updateView(newPage)
    }

    @CallSuper
    override fun updateView(page: ThemePage) {
        refreshToolbarMenuItems(true)
        paginationHelper.updatePagination(page.pagination)

        setTitle(page.title)

        setTabTitle(String.format(getString(R.string.fragment_tab_title_theme), page.title))

        val pagination = page.pagination
        setSubtitle("" + pagination.current + "/" + pagination.all)
    }

    private fun toggleMessagePanel() {
        if (messagePanel.visibility == View.VISIBLE) {
            hideMessagePanel()
        } else {
            showMessagePanel(true)
        }
    }

    private fun showMessagePanel(showKeyboard: Boolean) {
        if (messagePanel.visibility != View.VISIBLE) {
            messagePanel.visibility = View.VISIBLE
            if (showKeyboard) {
                messagePanel.show()
            }
            messagePanel.heightChangeListener.onChangedHeight(messagePanel.lastHeight)
            toggleMessagePanelItem.icon = App.getVecDrawable(context, R.drawable.ic_toolbar_transcribe_close)
        }
        if (showKeyboard) {
            //messagePanel.getMessageField().setSelection(messagePanel.getMessageField().length());
            messagePanel.messageField.requestFocus()
            showKeyboard(messagePanel.messageField)
        }
    }

    private fun hideMessagePanel() {
        messagePanel.visibility = View.GONE
        messagePanel.hidePopupWindows()
        hideKeyboard()
        messagePanel.heightChangeListener.onChangedHeight(0)
        toggleMessagePanelItem.icon = App.getVecDrawable(context, R.drawable.ic_toolbar_create)
    }

    override fun addBaseToolbarMenu(menu: Menu) {
        super.addBaseToolbarMenu(menu)
        toggleMessagePanelItem = menu
                .add(R.string.reply)
                .setIcon(App.getVecDrawable(context, R.drawable.ic_toolbar_create))
                .setOnMenuItemClickListener {
                    toggleMessagePanel()
                    false
                }
                .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)

        refreshMenuItem = menu
                .add(R.string.refresh)
                .setIcon(App.getVecDrawable(context, R.drawable.ic_toolbar_refresh))
                .setOnMenuItemClickListener {
                    presenter.reload()
                    false
                }

        copyLinkMenuItem = menu
                .add(R.string.copy_link)
                .setOnMenuItemClickListener {
                    presenter.copyLink()
                    false
                }
        addSearchOnPageItem(menu)
        searchInThemeMenuItem = menu
                .add(R.string.search_in_theme)
                .setOnMenuItemClickListener {
                    presenter.openSearch()
                    false
                }
        searchPostsMenuItem = menu
                .add(R.string.search_my_posts)
                .setOnMenuItemClickListener {
                    presenter.openSearchMyPosts()
                    false
                }

        deleteFavoritesMenuItem = menu
                .add(R.string.delete_from_favorites)
                .setOnMenuItemClickListener {
                    presenter.onClickDeleteInFav()
                    false
                }
        addFavoritesMenuItem = menu
                .add(R.string.add_to_favorites)
                .setOnMenuItemClickListener {
                    presenter.onClickAddInFav()
                    false
                }
        openForumMenuItem = menu
                .add(R.string.open_theme_forum)
                .setOnMenuItemClickListener {
                    presenter.openForum()
                    false
                }

        refreshToolbarMenuItems(false)
    }

    override fun refreshToolbarMenuItems(enable: Boolean) {
        super.refreshToolbarMenuItems(enable)
        if (enable) {
            val pageNotNull = presenter.isPageLoaded()

            toggleMessagePanelItem.isEnabled = true
            refreshMenuItem.isEnabled = true
            copyLinkMenuItem.isEnabled = pageNotNull
            searchInThemeMenuItem.isEnabled = pageNotNull
            searchPostsMenuItem.isEnabled = pageNotNull
            searchOnPageMenuItem.isEnabled = pageNotNull
            deleteFavoritesMenuItem.isEnabled = pageNotNull
            addFavoritesMenuItem.isEnabled = pageNotNull
            if (pageNotNull) {
                if (presenter.isInFavorites()) {
                    deleteFavoritesMenuItem.isVisible = true
                    addFavoritesMenuItem.isVisible = false
                } else {
                    deleteFavoritesMenuItem.isVisible = false
                    addFavoritesMenuItem.isVisible = true
                }
            }
            openForumMenuItem.isEnabled = pageNotNull
        } else {
            toggleMessagePanelItem.isEnabled = false
            refreshMenuItem.isEnabled = true
            copyLinkMenuItem.isEnabled = false
            searchInThemeMenuItem.isEnabled = false
            searchPostsMenuItem.isEnabled = false
            searchOnPageMenuItem.isEnabled = false
            deleteFavoritesMenuItem.isEnabled = false
            addFavoritesMenuItem.isEnabled = false
            deleteFavoritesMenuItem.isVisible = false
            addFavoritesMenuItem.isVisible = false
            openForumMenuItem.isEnabled = false
        }
        if (!authHolder.get().isAuth()) {
            toggleMessagePanelItem.isVisible = false
            deleteFavoritesMenuItem.isVisible = false
            addFavoritesMenuItem.isVisible = false
            searchPostsMenuItem.isEnabled = false
            hideMessagePanel()
        }
    }

    private fun addSearchOnPageItem(menu: Menu) {
        toolbar.inflateMenu(R.menu.theme_search_menu)
        searchOnPageMenuItem = menu.findItem(R.id.action_search)
        searchOnPageMenuItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                toggleMessagePanelItem.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
                return true
            }

            override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                toggleMessagePanelItem.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_NEVER)
                return true
            }
        })
        val searchView = searchOnPageMenuItem.actionView as SearchView
        searchView.tag = searchViewTag

        searchView.setOnSearchClickListener { _ ->
            if (searchView.tag == searchViewTag) {
                val searchClose = searchView.findViewById<View>(androidx.appcompat.appcompat.R.id.search_close_btn) as ImageView?
                if (searchClose != null)
                    (searchClose.parent as ViewGroup).removeView(searchClose)

                val navButtonsParams = ViewGroup.LayoutParams(App.px48, App.px48)
                val outValue = TypedValue()
                context?.theme?.resolveAttribute(android.R.attr.actionBarItemBackground, outValue, true)

                val btnNext = AppCompatImageButton(searchView.context)
                btnNext.setImageDrawable(App.getVecDrawable(context, R.drawable.ic_toolbar_search_next))
                btnNext.setBackgroundResource(outValue.resourceId)

                val btnPrev = AppCompatImageButton(searchView.context)
                btnPrev.setImageDrawable(App.getVecDrawable(context, R.drawable.ic_toolbar_search_prev))
                btnPrev.setBackgroundResource(outValue.resourceId)

                (searchView.getChildAt(0) as LinearLayout).addView(btnPrev, navButtonsParams)
                (searchView.getChildAt(0) as LinearLayout).addView(btnNext, navButtonsParams)

                btnNext.setOnClickListener { findNext(true) }
                btnPrev.setOnClickListener { findNext(false) }
                searchViewTag++
            }
        }

        val searchManager = activity?.getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchView.setSearchableInfo(searchManager.getSearchableInfo(activity?.componentName))

        searchView.setIconifiedByDefault(true)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                findText(newText)
                return false
            }
        })
    }

    override fun showAddInFavDialog(page: ThemePage) {
        AlertDialog.Builder(context!!)
                .setTitle(R.string.favorites_subscribe_email)
                .setItems(FavoritesFragment.SUB_NAMES) { _, which ->
                    presenter.addTopicToFavorite(page.id, FavoritesApi.SUB_TYPES[which])
                }
                .show()
    }

    override fun showDeleteInFavDialog(page: ThemePage) {
        if (page.favId == 0) {
            Toast.makeText(App.getContext(), R.string.fav_delete_error_id_not_found, Toast.LENGTH_SHORT).show()
        }
        AlertDialog.Builder(context!!)
                .setMessage(R.string.fav_ask_delete)
                .setPositiveButton(R.string.ok) { _, _ ->
                    presenter.deleteTopicFromFavorite(page.favId)
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
    }

    override fun onAddToFavorite(result: Boolean) {
        Toast.makeText(context, if (result) getString(R.string.favorites_added) else getString(R.string.error_occurred), Toast.LENGTH_SHORT).show()
        refreshToolbarMenuItems(true)
    }

    override fun onDeleteFromFavorite(result: Boolean) {
        Toast.makeText(App.getContext(), getString(if (result) R.string.favorite_theme_deleted else R.string.error), Toast.LENGTH_SHORT).show()
        refreshToolbarMenuItems(true)
    }


    /*
     *
     * EDIT POST FUNCTIONS
     *
     * */

    override fun syncEditPost(data: EditPostSyncData) {
        messagePanel.setText(data.message)
        messagePanel.messageField.setSelection(data.selectionStart, data.selectionEnd)
        data.attachments?.also { attachmentsPopup.setAttachments(it) }
    }

    private fun sendMessage() {
        hideKeyboard()
        presenter.sendMessage(messagePanel.message, messagePanel.attachments)
    }

    override fun onMessageSent() {
        messagePanel.clearAttachments()
        messagePanel.clearMessage()
        if (mainPreferencesHolder.getEditorDefaultHidden()) {
            hideMessagePanel()
        }
    }

    override fun setMessageRefreshing(isRefreshing: Boolean) {
        messagePanel.setProgressState(isRefreshing)
    }

    private fun tryPickFile() {
        App.get().checkStoragePermission({ startActivityForResult(FilePickHelper.pickFile(false), TabFragment.REQUEST_PICK_FILE) }, App.getActivity())
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == TabFragment.REQUEST_PICK_FILE && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                //Display an error
                return
            }
            uploadFiles(FilePickHelper.onActivityResult(context, data))
        }
    }

    fun uploadFiles(files: List<RequestFile>) {
        val pending = attachmentsPopup.preUploadFiles(files)
        presenter.uploadFiles(files, pending)
    }

    private fun removeFiles() {
        attachmentsPopup.preDeleteFiles()
        val selectedFiles = attachmentsPopup.getSelected()
        presenter.deleteFiles(selectedFiles)
    }

    override fun onUploadFiles(items: List<AttachmentItem>) {
        attachmentsPopup.onUploadFiles(items)
    }

    override fun onDeleteFiles(items: List<AttachmentItem>) {
        attachmentsPopup.onDeleteFiles(items)
    }

    /*
     *
     * Post functions
     *
     * */

    override fun showNoteCreate(title: String, url: String) {
        NotesAddPopup.showAddNoteDialog(context, title, url)
    }

    override fun firstPage() {
        paginationHelper.firstPage()
    }

    override fun prevPage() {
        paginationHelper.prevPage()
    }

    override fun nextPage() {
        paginationHelper.nextPage()
    }

    override fun lastPage() {
        paginationHelper.lastPage()
    }

    override fun selectPage() {
        paginationHelper.selectPageDialog()
    }


    override fun insertText(text: String) {
        messagePanel.insertText(text)
        showMessagePanel(true)
    }

    override fun editPost(post: IBaseForumPost) {
        presenter.openEditPostForm(post.id)
    }

    override fun toast(text: String) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
    }

    override fun log(text: String) {
        val maxLogSize = 1000
        for (i in 0..text.length / maxLogSize) {
            val start = i * maxLogSize
            var end = (i + 1) * maxLogSize
            end = if (end > text.length) text.length else end
            Log.v(LOG_TAG, text.substring(start, end))
        }
    }

    override fun showUserMenu(post: IBaseForumPost) {
        dialogsHelper.showUserMenu(presenter, post)
    }

    override fun showReputationMenu(post: IBaseForumPost) {
        dialogsHelper.showReputationMenu(presenter, post)
    }

    override fun showPostMenu(post: IBaseForumPost) {
        dialogsHelper.showPostMenu(presenter, post)
    }

    override fun reportPost(post: IBaseForumPost) {
        dialogsHelper.tryReportPost(presenter, post)
    }

    override fun deletePost(post: IBaseForumPost) {
        dialogsHelper.deletePost(presenter, post)
    }

    override fun votePost(post: IBaseForumPost, type: Boolean) {
        dialogsHelper.votePost(presenter, post, type)
    }

    override fun showChangeReputation(post: IBaseForumPost, type: Boolean) {
        dialogsHelper.changeReputation(presenter, post, type)
    }

    companion object {
        //Указывают на произведенное действие: переход назад, обновление, обычный переход по ссылке
        private val LOG_TAG = ThemeFragment::class.java.simpleName
    }
}
