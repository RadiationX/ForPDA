package forpdateam.ru.forpda.ui.fragments.search

import android.annotation.SuppressLint
import android.app.SearchManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import com.google.android.material.bottomsheet.BottomSheetDialog
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.menu.ActionMenuItemView
import androidx.appcompat.widget.ActionMenuView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.widget.SearchView
import android.util.Log
import android.view.*
import android.widget.*
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import com.nostra13.universalimageloader.core.ImageLoader
import forpdateam.ru.forpda.App
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.common.webview.CustomWebChromeClient
import forpdateam.ru.forpda.common.webview.CustomWebViewClient
import forpdateam.ru.forpda.common.webview.DialogsHelper
import forpdateam.ru.forpda.entity.remote.IBaseForumPost
import forpdateam.ru.forpda.entity.remote.search.SearchItem
import forpdateam.ru.forpda.entity.remote.search.SearchResult
import forpdateam.ru.forpda.entity.remote.search.SearchSettings
import forpdateam.ru.forpda.model.data.remote.api.favorites.FavoritesApi
import forpdateam.ru.forpda.presentation.search.SearchPresenter
import forpdateam.ru.forpda.presentation.search.SearchSiteView
import forpdateam.ru.forpda.presentation.theme.ThemeJsInterface
import forpdateam.ru.forpda.ui.fragments.TabFragment
import forpdateam.ru.forpda.ui.fragments.devdb.brand.DevicesFragment
import forpdateam.ru.forpda.ui.fragments.favorites.FavoritesFragment
import forpdateam.ru.forpda.ui.fragments.notes.NotesAddPopup
import forpdateam.ru.forpda.ui.fragments.theme.ThemeDialogsHelper_V2
import forpdateam.ru.forpda.ui.fragments.theme.ThemeFragmentWeb
import forpdateam.ru.forpda.ui.views.*
import forpdateam.ru.forpda.ui.views.adapters.BaseAdapter
import forpdateam.ru.forpda.ui.views.pagination.PaginationHelper
import io.github.douglasjunior.androidSimpleTooltip.SimpleTooltip
import java.util.*

/**
 * Created by radiationx on 29.01.17.
 */

class SearchFragment : TabFragment(), SearchSiteView, ExtendedWebView.JsLifeCycleListener, BaseAdapter.OnItemClickListener<SearchItem> {

    private lateinit var searchSettingsView: ViewGroup
    private lateinit var nickBlock: ViewGroup
    private lateinit var resourceBlock: ViewGroup
    private lateinit var resultBlock: ViewGroup
    private lateinit var sortBlock: ViewGroup
    private lateinit var sourceBlock: ViewGroup
    private lateinit var resourceSpinner: Spinner
    private lateinit var resultSpinner: Spinner
    private lateinit var sortSpinner: Spinner
    private lateinit var sourceSpinner: Spinner
    private lateinit var nickField: TextView
    private lateinit var submitButton: Button
    private lateinit var saveSettingsButton: Button


    private lateinit var webView: ExtendedWebView
    private lateinit var recyclerView: androidx.recyclerview.widget.RecyclerView
    private lateinit var refreshLayout: androidx.swiperefreshlayout.widget.SwipeRefreshLayout
    private val adapter = SearchAdapter()
    private var webViewClient: CustomWebViewClient? = null


    private lateinit var paginationHelper: PaginationHelper
    private lateinit var dialogMenu: DynamicDialogMenu<SearchFragment, IBaseForumPost>


    private lateinit var searchView: SearchView
    private lateinit var searchItem: MenuItem
    private lateinit var dialog: BottomSheetDialog
    private var tooltip: SimpleTooltip? = null

    private lateinit var settingsMenuItem: MenuItem


    private lateinit var jsInterface: ThemeJsInterface
    private lateinit var dialogsHelper: ThemeDialogsHelper_V2

    private val authHolder = App.get().Di().authHolder
    private val mainPreferencesHolder = App.get().Di().mainPreferencesHolder
    private val otherPreferencesHolder = App.get().Di().otherPreferencesHolder

    private val listener = object : AdapterView.OnItemSelectedListener {
        override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
            val field = when (parent) {
                resourceSpinner -> SearchPresenter.FIELD_RESOURCE
                resultSpinner -> SearchPresenter.FIELD_RESULT
                sortSpinner -> SearchPresenter.FIELD_SORT
                sourceSpinner -> SearchPresenter.FIELD_SOURCE
                else -> null
            }

            if (field != null) {
                presenter.updateSettings(field, position)
            }
        }

        override fun onNothingSelected(parent: AdapterView<*>) {

        }
    }

    @InjectPresenter
    lateinit var presenter: SearchPresenter

    @ProvidePresenter
    internal fun providePresenter(): SearchPresenter = SearchPresenter(
            App.get().Di().searchRepository,
            App.get().Di().favoritesRepository,
            App.get().Di().themeRepository,
            App.get().Di().reputationRepository,
            App.get().Di().topicPreferencesHolder,
            App.get().Di().mainPreferencesHolder,
            App.get().Di().otherPreferencesHolder,
            App.get().Di().searchTemplate,
            App.get().Di().templateManager,
            App.get().Di().router,
            App.get().Di().linkHandler,
            App.get().Di().errorHandler
    )

    init {
        configuration.defaultTitle = App.get().getString(R.string.fragment_title_search)
    }

    override fun updateShowAvatarState(isShow: Boolean) {
        webView.evalJs("updateShowAvatarState($isShow)")
    }

    override fun updateTypeAvatarState(isCircle: Boolean) {
        webView.evalJs("updateTypeAvatarState($isCircle)")
    }

    override fun updateScrollButtonState(isEnabled: Boolean) {
        if (isEnabled) {
            fab.visibility = View.VISIBLE
        } else {
            fab.visibility = View.GONE
        }
    }

    override fun setFontSize(size: Int) {
        webView.setRelativeFontSize(size)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            presenter.initSearchSettings(getString(TabFragment.ARG_TAB))

        }
        dialogsHelper = ThemeDialogsHelper_V2(context, authHolder, otherPreferencesHolder)
    }

    override fun initFabBehavior() {
        val params = fab.layoutParams as androidx.coordinatorlayout.widget.CoordinatorLayout.LayoutParams
        val behavior = FabOnScroll(fab.context)
        params.behavior = behavior
        params.gravity = Gravity.CENTER_VERTICAL or Gravity.END
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
            params.setMargins(App.px16, App.px16, App.px16, App.px16)
        }
        fab.requestLayout()
    }

    @SuppressLint("JavascriptInterface")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        initFabBehavior()

        baseInflateFragment(inflater, R.layout.fragment_search)
        refreshLayout = findViewById(R.id.swipe_refresh_list) as androidx.swiperefreshlayout.widget.SwipeRefreshLayout
        searchSettingsView = View.inflate(context, R.layout.search_settings, null) as ViewGroup

        nickBlock = searchSettingsView.findViewById<View>(R.id.search_nick_block) as ViewGroup
        resourceBlock = searchSettingsView.findViewById<View>(R.id.search_resource_block) as ViewGroup
        resultBlock = searchSettingsView.findViewById<View>(R.id.search_result_block) as ViewGroup
        sortBlock = searchSettingsView.findViewById<View>(R.id.search_sort_block) as ViewGroup
        sourceBlock = searchSettingsView.findViewById<View>(R.id.search_source_block) as ViewGroup

        resourceSpinner = searchSettingsView.findViewById<View>(R.id.search_resource_spinner) as Spinner
        resultSpinner = searchSettingsView.findViewById<View>(R.id.search_result_spinner) as Spinner
        sortSpinner = searchSettingsView.findViewById<View>(R.id.search_sort_spinner) as Spinner
        sourceSpinner = searchSettingsView.findViewById<View>(R.id.search_source_spinner) as Spinner

        nickField = searchSettingsView.findViewById<View>(R.id.search_nick_field) as TextView

        submitButton = searchSettingsView.findViewById<View>(R.id.search_submit) as Button
        saveSettingsButton = searchSettingsView.findViewById<View>(R.id.search_save_settings) as Button

        webView = ExtendedWebView(context)
        webView.setDialogsHelper(DialogsHelper(
                webView.context,
                App.get().Di().linkHandler,
                App.get().Di().systemLinkHandler,
                App.get().Di().router
        ))
        attachWebView(webView)
        recyclerView = androidx.recyclerview.widget.RecyclerView(context!!)
        recyclerView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        webView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        refreshLayout.addView(recyclerView)

        paginationHelper = PaginationHelper(activity)
        paginationHelper.addInToolbar(inflater, toolbarLayout, configuration.isFitSystemWindow)

        contentController.setMainRefresh(refreshLayout)
        return viewFragment
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        jsInterface = ThemeJsInterface(presenter)
        setScrollFlagsEnterAlways()

        dialogMenu = DynamicDialogMenu()
        dialogMenu.apply {
            addItem(getString(R.string.topic_to_begin)) { _, data ->
                presenter.openTopicBegin(data)
            }
            addItem(getString(R.string.topic_newposts)) { _, data ->
                presenter.openTopicNew(data)
            }
            addItem(getString(R.string.topic_lastposts)) { _, data ->
                presenter.openTopicLast(data)
            }
            addItem(getString(R.string.copy_link)) { _, data ->
                presenter.copyLink(data)
            }
            addItem(getString(R.string.open_theme_forum)) { _, data ->
                presenter.openForum(data)
            }
            addItem(getString(R.string.add_to_favorites)) { _, data ->
                presenter.onClickAddInFav(data)
            }
        }


        fab.setOnClickListener {
            if (webView.direction == ExtendedWebView.DIRECTION_DOWN) {
                webView.pageDown(true)
            } else if (webView.direction == ExtendedWebView.DIRECTION_UP) {
                webView.pageUp(true)
            }
        }
        webView.setOnDirectionListener { direction ->
            if (direction == ExtendedWebView.DIRECTION_DOWN) {
                fab.setImageDrawable(App.getVecDrawable(fab.context, R.drawable.ic_arrow_down))
            } else if (direction == ExtendedWebView.DIRECTION_UP) {
                fab.setImageDrawable(App.getVecDrawable(fab.context, R.drawable.ic_arrow_up))
            }
        }

        webView.setJsLifeCycleListener(this)
        webView.addJavascriptInterface(jsInterface, ThemeFragmentWeb.JS_INTERFACE)
        webView.setRelativeFontSize(mainPreferencesHolder.getWebViewFontSize())

        fab.size = FloatingActionButton.SIZE_MINI
        if (mainPreferencesHolder.getScrollButtonEnabled()) {
            fab.visibility = View.VISIBLE
        } else {
            fab.visibility = View.GONE
        }
        fab.scaleX = 0.0f
        fab.scaleY = 0.0f
        fab.alpha = 0.0f

        setCardsBackground()

        paginationHelper.setListener(object : PaginationHelper.PaginationListener {
            override fun onTabSelected(tab: TabLayout.Tab): Boolean {
                return refreshLayout.isRefreshing
            }

            override fun onSelectedPage(pageNumber: Int) {
                presenter.search(pageNumber)
            }
        })

        //searchSettingsView.setVisibility(View.GONE);
        dialog = BottomSheetDialog(context!!)
        dialog.setOnShowListener {
            dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        }
        //dialog.setPeekHeight(App.getKeyboardHeight());
        //dialog.getWindow().getDecorView().setFitsSystemWindows(true);


        val searchManager = activity?.getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchView.setSearchableInfo(searchManager.getSearchableInfo(activity?.componentName))
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                startSearch()
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }
        })

        searchView.queryHint = getString(R.string.search_keywords)

        val searchEditFrame = searchView.findViewById<View>(R.id.search_edit_frame) as LinearLayout
        val params = searchEditFrame.layoutParams as LinearLayout.LayoutParams
        params.leftMargin = 0

        val searchSrcText = searchView.findViewById<View>(R.id.search_src_text)
        searchSrcText.setPadding(0, searchSrcText.paddingTop, 0, searchSrcText.paddingBottom)


        submitButton.setOnClickListener { startSearch() }
        saveSettingsButton.setOnClickListener { presenter.saveSettings() }
        //recyclerView.setHasFixedSize(true);
        recyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        recyclerView.addItemDecoration(DevicesFragment.SpacingItemDecoration(App.px8, true))
        val pauseOnScrollListener = PauseOnScrollListener(ImageLoader.getInstance(), true, true)
        recyclerView.addOnScrollListener(pauseOnScrollListener)
        recyclerView.adapter = adapter
        refreshLayoutStyle(refreshLayout)
        refreshLayoutLongTrigger(refreshLayout)
        refreshLayout.setOnRefreshListener { presenter.refreshData() }
        adapter.setOnItemClickListener(this)

        if (otherPreferencesHolder.getTooltipSearchSettings()) {
            for (toolbarChildIndex in 0 until toolbar.childCount) {
                val childView = toolbar.getChildAt(toolbarChildIndex)
                if (childView is ActionMenuView) {
                    for (menuChildIndex in 0 until childView.childCount) {
                        try {
                            val itemView = childView.getChildAt(menuChildIndex) as ActionMenuItemView
                            if (settingsMenuItem === itemView.itemData) {
                                tooltip = SimpleTooltip.Builder(context)
                                        .anchorView(itemView)
                                        .text(R.string.tooltip_search_settings)
                                        .gravity(Gravity.BOTTOM)
                                        .animated(false)
                                        .modal(true)
                                        .transparentOverlay(false)
                                        .backgroundColor(Color.BLACK)
                                        .textColor(Color.WHITE)
                                        .padding(App.px16.toFloat())
                                        .build()
                                        .apply {
                                            show()
                                        }
                                break
                            }
                        } catch (ignore: ClassCastException) {
                        }

                    }
                    break
                }
            }

            otherPreferencesHolder.setTooltipSearchSettings(false)
        }


    }


    override fun addBaseToolbarMenu(menu: Menu) {
        super.addBaseToolbarMenu(menu)
        menu.add(R.string.copy_link)
                .setOnMenuItemClickListener {
                    presenter.copyLink()
                    false
                }
        toolbar.inflateMenu(R.menu.qms_contacts_menu)

        settingsMenuItem = menu.add(R.string.settings)
                .setIcon(R.drawable.ic_toolbar_tune)
                .setOnMenuItemClickListener {
                    hideKeyboard()
                    if (searchSettingsView.parent != null && searchSettingsView.parent is ViewGroup) {
                        (searchSettingsView.parent as ViewGroup).removeView(searchSettingsView)
                    }
                    dialog.setContentView(searchSettingsView)
                    dialog.show()
                    false
                }
                .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)

        searchItem = menu.findItem(R.id.action_search)
        searchView = searchItem.actionView as SearchView
        searchView.setIconifiedByDefault(true)
    }

    override fun onResumeOrShow() {
        super.onResumeOrShow()
        searchItem.expandActionView()
    }

    override fun onPauseOrHide() {
        super.onPauseOrHide()
        searchItem.collapseActionView()
    }

    override fun onBackPressed(): Boolean {
        tooltip?.also {
            if (it.isShowing) {
                it.dismiss()
                return true
            }
        }
        return super.onBackPressed()
    }

    override fun setStyleType(type: String) {
        webView.evalJs("changeStyleType(\"$type\")")
    }

    override fun showAddInFavDialog(item: IBaseForumPost) {
        AlertDialog.Builder(context!!)
                .setTitle(R.string.favorites_subscribe_email)
                .setItems(FavoritesFragment.SUB_NAMES) { _, which ->
                    presenter.addTopicToFavorite(item.topicId, FavoritesApi.SUB_TYPES[which])
                }
                .show()
    }

    override fun onAddToFavorite(result: Boolean) {
        Toast.makeText(context, if (result) getString(R.string.favorites_added) else getString(R.string.error_occurred), Toast.LENGTH_SHORT).show()
        refreshToolbarMenuItems(true)
    }

    private fun checkArg(arg: String, pair: Pair<String, String>): Boolean {
        return arg == pair.first
    }

    private fun setSelection(spinner: Spinner, items: List<String>, pair: Pair<String, String>) {
        spinner.setSelection(items.indexOf(pair.second))
    }

    private fun setItems(spinner: Spinner, items: List<String>, selection: Int) {
        val adapter = ArrayAdapter(activity!!, android.R.layout.simple_spinner_item, items)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.setSelection(selection)
        spinner.onItemSelectedListener = listener
    }

    override fun fillSettingsData(settings: SearchSettings, fields: Map<String, List<String>>) {
        searchView.post { searchView.setQuery(settings.query, false) }

        nickField.text = settings.nick

        val resourceItems = fields[SearchPresenter.FIELD_RESOURCE] ?: emptyList()
        val resultItems = fields[SearchPresenter.FIELD_RESULT] ?: emptyList()
        val sortItems = fields[SearchPresenter.FIELD_SORT] ?: emptyList()
        val sourceItems = fields[SearchPresenter.FIELD_SOURCE] ?: emptyList()

        setItems(resourceSpinner, resourceItems, 0)
        setItems(resultSpinner, resultItems, 0)
        setItems(sortSpinner, sortItems, 0)
        setItems(sourceSpinner, sourceItems, 1)

        when {
            checkArg(settings.resourceType, SearchSettings.RESOURCE_NEWS) -> {
                setSelection(resourceSpinner, resourceItems, SearchSettings.RESOURCE_NEWS)
            }
            checkArg(settings.resourceType, SearchSettings.RESOURCE_FORUM) -> {
                setSelection(resourceSpinner, resourceItems, SearchSettings.RESOURCE_FORUM)
            }
        }

        when {
            checkArg(settings.result, SearchSettings.RESULT_TOPICS) -> {
                setSelection(resultSpinner, resultItems, SearchSettings.RESULT_TOPICS)
            }
            checkArg(settings.result, SearchSettings.RESULT_POSTS) -> {
                setSelection(resultSpinner, resultItems, SearchSettings.RESULT_POSTS)
            }
        }

        when {
            checkArg(settings.sort, SearchSettings.SORT_DA) -> {
                setSelection(sortSpinner, sortItems, SearchSettings.SORT_DA)
            }
            checkArg(settings.sort, SearchSettings.SORT_DD) -> {
                setSelection(sortSpinner, sortItems, SearchSettings.SORT_DD)
            }
            checkArg(settings.sort, SearchSettings.SORT_REL) -> {
                setSelection(sortSpinner, sortItems, SearchSettings.SORT_REL)
            }
        }

        when {
            checkArg(settings.source, SearchSettings.SOURCE_ALL) -> {
                setSelection(sourceSpinner, sourceItems, SearchSettings.SOURCE_ALL)
            }
            checkArg(settings.source, SearchSettings.SOURCE_TITLES) -> {
                setSelection(sourceSpinner, sourceItems, SearchSettings.SOURCE_TITLES)
            }
            checkArg(settings.source, SearchSettings.SOURCE_CONTENT) -> {
                setSelection(sourceSpinner, sourceItems, SearchSettings.SOURCE_CONTENT)
            }
        }
    }

    override fun setNewsMode() {
        nickBlock.visibility = View.GONE
        resultBlock.visibility = View.GONE
        sortBlock.visibility = View.GONE
        sourceBlock.visibility = View.GONE
    }

    override fun setForumMode() {
        nickBlock.visibility = View.VISIBLE
        resultBlock.visibility = View.VISIBLE
        sortBlock.visibility = View.VISIBLE
        sourceBlock.visibility = View.VISIBLE
    }

    private fun startSearch() {
        presenter.search(searchView.query.toString(), nickField.text.toString())
        dialog.dismiss()
    }

    override fun onStartSearch(settings: SearchSettings) {
        hideKeyboard()

        val titleBuilder = StringBuilder()
        titleBuilder.append("Поиск")
        if (settings.resourceType == SearchSettings.RESOURCE_NEWS.first) {
            titleBuilder.append(" новостей")
        } else {
            if (settings.result == SearchSettings.RESULT_POSTS.first) {
                titleBuilder.append(" сообщений")
            } else {
                titleBuilder.append(" тем")
            }
            if (!settings.nick.isEmpty()) {
                titleBuilder.append(" пользователя \"").append(settings.nick).append("\"")
            }
        }
        if (!settings.query.isEmpty()) {
            titleBuilder.append(" по запросу \"").append(settings.query).append("\"")
        }
        setTitle(titleBuilder.toString())
    }

    override fun showData(searchResult: SearchResult) {
        setRefreshing(false)
        recyclerView.scrollToPosition(0)
        hideKeyboard()
        Log.d("SUKA", "SEARCH SIZE " + searchResult.items.size)
        if (searchResult.items.isEmpty()) {
            if (!contentController.contains(ContentController.TAG_NO_DATA)) {
                val funnyContent = FunnyContent(context)
                        .setImage(R.drawable.ic_search)
                        .setTitle(R.string.funny_search_nodata_title)
                        .setDesc(R.string.funny_search_nodata_desc)
                contentController.addContent(funnyContent, ContentController.TAG_NO_DATA)
            }
            contentController.showContent(ContentController.TAG_NO_DATA)
        } else {
            contentController.hideContent(ContentController.TAG_NO_DATA)
        }
        if (
                searchResult.settings?.result == SearchSettings.RESULT_POSTS.first
                && searchResult.settings?.resourceType == SearchSettings.RESOURCE_FORUM.first
        ) {
            for (i in 0 until refreshLayout.childCount) {
                if (refreshLayout.getChildAt(i) is androidx.recyclerview.widget.RecyclerView) {
                    refreshLayout.removeViewAt(i)
                    fixTargetView()
                    break
                }
            }
            if (refreshLayout.childCount <= 1) {
                refreshLayout.addView(webView)
                Log.d(LOG_TAG, "add webview")
            }
            if (webViewClient == null) {
                webViewClient = CustomWebViewClient()
                webView.webViewClient = webViewClient
                webView.webChromeClient = CustomWebChromeClient()
            }
            Log.d("SUKA", "SEARCH SHOW WEBVIEW")
            webView.loadDataWithBaseURL("https://4pda.to/forum/", searchResult.html, "text/html", "utf-8", null)
        } else {
            for (i in 0 until refreshLayout.childCount) {
                if (refreshLayout.getChildAt(i) is ExtendedWebView) {
                    refreshLayout.removeViewAt(i)
                    fixTargetView()
                }
            }
            if (refreshLayout.childCount <= 1) {
                fab.visibility = View.GONE
                refreshLayout.addView(recyclerView)
                Log.d(LOG_TAG, "add recyclerview")
            }
            Log.d("SUKA", "SEARCH SHOW RECYCLERVIEW")
            adapter.clear()
            adapter.addAll(searchResult.items)
        }

        paginationHelper.updatePagination(searchResult.pagination)
        setSubtitle(paginationHelper.title)
    }


    //Поле mTarget это вьюха, от которой зависит обработка движений
    private fun fixTargetView() {
        try {
            val field = refreshLayout.javaClass.getDeclaredField("mTarget")
            field.isAccessible = true
            field.set(refreshLayout, null)
            field.isAccessible = false
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        unregisterForContextMenu(webView)
        webView.removeJavascriptInterface(ThemeFragmentWeb.JS_INTERFACE)
        webView.setJsLifeCycleListener(null)
        webView.endWork()
    }

    override fun onDestroy() {
        super.onDestroy()
        paginationHelper.destroy()
    }

    override fun onDomContentComplete(actions: ArrayList<String>) {}

    override fun onPageComplete(actions: ArrayList<String>) {
        actions.add("window.scrollTo(0, 0);")
    }

    override fun onItemClick(item: SearchItem) {
        presenter.onItemClick(item)
    }

    override fun onItemLongClick(item: SearchItem): Boolean {
        presenter.onItemLongClick(item)
        return false
    }

    override fun showItemDialogMenu(item: SearchItem, settings: SearchSettings) {
        dialogMenu.apply {
            disallowAll()
            if (settings.resourceType == SearchSettings.RESOURCE_NEWS.first) {
                allow(3)
            } else {
                allowAll()
            }
            show(context, this@SearchFragment, item)
        }
    }


    /* JS PRESENTER */

    override fun showNoteCreate(title: String, url: String) {
        NotesAddPopup.showAddNoteDialog(context, title, url)
    }

    override fun deletePostUi(post: IBaseForumPost) {
        webView.evalJs("onDeletePostClick(" + post.id + ");")
    }

    override fun openAnchorDialog(post: IBaseForumPost, anchorName: String) {
        dialogsHelper.openAnchorDialog(presenter, post, anchorName)
    }

    override fun openSpoilerLinkDialog(post: IBaseForumPost, spoilNumber: String) {
        dialogsHelper.openSpoilerLinkDialog(presenter, post, spoilNumber)
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

    override fun editPost(post: IBaseForumPost) {
        presenter.openEditPostForm(post.id)
    }

    companion object {
        private val LOG_TAG = SearchFragment::class.java.simpleName
    }

}
