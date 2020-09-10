package forpdateam.ru.forpda.ui.fragments

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.annotation.CallSuper
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.appcompat.widget.Toolbar
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.*

import com.arellomobile.mvp.MvpAppCompatFragment

import forpdateam.ru.forpda.App
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.ui.DimensionHelper
import forpdateam.ru.forpda.ui.activities.MainActivity
import forpdateam.ru.forpda.ui.views.ContentController
import forpdateam.ru.forpda.ui.views.ExtendedWebView
import forpdateam.ru.forpda.ui.views.ScrollAwareFABBehavior
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_base.view.*

/**
 * Created by radiationx on 07.08.16.
 */
open class TabFragment : MvpAppCompatFragment() {

    private val mHandler = Handler(Looper.getMainLooper())
    private lateinit var mUiThread: Thread

    val configuration = TabConfiguration()

    private var titleText: String? = null
    private var tabTitleText: String? = null
    private var subtitleText: String? = null

    protected lateinit var toolbarProgress: ProgressBar
    protected lateinit var fragmentContainer: RelativeLayout
    protected lateinit var fragmentContent: ViewGroup
    protected lateinit var additionalContent: ViewGroup
    protected lateinit var contentProgress: ProgressBar
    protected lateinit var titlesWrapper: LinearLayout
    protected lateinit var coordinatorLayout: androidx.coordinatorlayout.widget.CoordinatorLayout
    protected lateinit var appBarLayout: AppBarLayout
    protected lateinit var toolbarLayout: CollapsingToolbarLayout
    protected lateinit var toolbar: Toolbar
    protected lateinit var toolbarBackground: ImageView
    protected lateinit var toolbarImageView: ImageView
    protected lateinit var toolbarTitleView: TextView
    protected lateinit var toolbarSubtitleView: TextView
    protected lateinit var toolbarSpinner: Spinner
    protected lateinit var viewFragment: View
    protected lateinit var fab: FloatingActionButton
    protected lateinit var contentController: ContentController
    protected lateinit var preLpShadow: View

    protected var disposables = CompositeDisposable()
    protected var networkState = App.get().Di().networkState
    private val countersHolder = App.get().Di().countersHolder
    private val dimensionsProvider = App.get().Di().dimensionsProvider

    protected open fun isShadowVisible(): Boolean = true

    private val mainActivity: MainActivity
        get() = activity as MainActivity

    private var attachedWebView: ExtendedWebView? = null

    fun getTitle(): String {
        return titleText ?: configuration.defaultTitle
    }

    fun setTitle(newTitle: String?) {
        this.titleText = newTitle
        if (tabTitleText == null) {
            mainActivity.tabNavigator.notifyUpdate(this)
        }
        toolbarTitleView.text = getTitle()
    }

    protected fun getSubtitle(): String? {
        return subtitleText
    }

    fun setSubtitle(newSubtitle: String?) {
        this.subtitleText = newSubtitle
        if (subtitleText == null) {
            if (toolbarSubtitleView.visibility != View.GONE)
                toolbarSubtitleView.visibility = View.GONE
        } else {
            if (toolbarSubtitleView.visibility != View.VISIBLE)
                toolbarSubtitleView.visibility = View.VISIBLE
            toolbarSubtitleView.text = getSubtitle()
        }
    }

    fun getTabTitle(): String {
        return tabTitleText ?: getTitle()
    }

    fun setTabTitle(tabTitle: String) {
        this.tabTitleText = tabTitle
        mainActivity.tabNavigator.notifyUpdate(this)
    }

    protected fun addToDisposable(disposable: Disposable) {
        disposables.add(disposable)
    }

    //False - можно закрывать
    //True - еще нужно что-то сделать, не закрывать
    @CallSuper
    open fun onBackPressed(): Boolean {
        Log.d(LOG_TAG, "onBackPressed " + this)
        return false
    }

    open fun hideKeyboard() {
        mainActivity.hideKeyboard()
    }

    open fun showKeyboard(view: View) {
        mainActivity.showKeyboard(view)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainActivity.tabNavigator.subscribe(this)
        mUiThread = Thread.currentThread()
        Log.d(LOG_TAG, "onCreate " + this)

        savedInstanceState?.also {
            titleText = it.getString(BUNDLE_TITLE)
            subtitleText = it.getString(BUNDLE_SUBTITLE)
            tabTitleText = it.getString(BUNDLE_TAB_TITLE)
            configuration.isAlone = it.getBoolean(BUNDLE_CONFIG_ALONE, configuration.isAlone)
            configuration.isMenu = it.getBoolean(BUNDLE_CONFIG_MENU, configuration.isMenu)
        }

        arguments?.also {
            titleText = it.getString(ARG_TITLE)
            subtitleText = it.getString(ARG_SUBTITLE)
        }
        setHasOptionsMenu(true)
    }

    @CallSuper
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewFragment = inflater.inflate(R.layout.fragment_base, container, false)
        //Осторожно! Чувствительно к структуре разметки! (по идеи так должно работать чуть быстрее)
        fragmentContainer = findViewById(R.id.fragment_container) as RelativeLayout
        coordinatorLayout = fragmentContainer.findViewById(R.id.coordinator_layout)
        appBarLayout = coordinatorLayout.findViewById(R.id.appbar_layout)
        toolbarLayout = appBarLayout.findViewById(R.id.toolbar_layout)
        toolbarBackground = toolbarLayout.findViewById(R.id.toolbar_image_background)
        toolbar = toolbarLayout.findViewById(R.id.toolbar)
        toolbarImageView = toolbar.findViewById(R.id.toolbar_image_icon)
        toolbarTitleView = toolbar.findViewById(R.id.toolbar_title)
        toolbarSubtitleView = toolbar.findViewById(R.id.toolbar_subtitle)
        toolbarProgress = toolbar.findViewById(R.id.toolbar_progress)
        titlesWrapper = toolbar.findViewById(R.id.toolbar_titles_wrapper)
        toolbarSpinner = toolbar.findViewById(R.id.toolbar_spinner)
        fragmentContent = coordinatorLayout.findViewById(R.id.fragment_content)
        additionalContent = coordinatorLayout.findViewById(R.id.additional_content)
        contentProgress = additionalContent.findViewById(R.id.content_progress)
        preLpShadow = findViewById(R.id.toolbar_shadow_prelp)
        //// TODO: 20.03.17 удалить и юзать только там, где нужно
        fab = coordinatorLayout.findViewById(R.id.fab)
        contentController = ContentController(contentProgress, additionalContent, fragmentContent)
        return viewFragment
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.post { this.updateToolbarShadow() }

        toolbarTitleView.apply {
            ellipsize = TextUtils.TruncateAt.MARQUEE
            setHorizontallyScrolling(true)
            marqueeRepeatLimit = 3
            isSelected = true
            isHorizontalFadingEdgeEnabled = true
            setFadingEdgeLength(App.px16)
        }


        toolbar.apply {
            if (this@TabFragment is TabTopScroller) {
                isClickable = true
                setOnClickListener { v -> (this@TabFragment as TabTopScroller).toggleScrollTop() }
            }

            val isToggle = configuration.isAlone || configuration.isMenu
            if (!isToggle) {
                setNavigationOnClickListener { v -> mainActivity.removeTabListener.invoke(v) }
                setNavigationIcon(R.drawable.ic_toolbar_arrow_back)
                navigationContentDescription = getString(R.string.close_tab)
                contentInsetEndWithActions = 0
                contentInsetStartWithNavigation = 0
                setContentInsetsRelative(0, contentInsetEnd)
            }
        }

        setTitle(titleText)
        setSubtitle(subtitleText)
        addBaseToolbarMenu(toolbar.menu)

        disposables.add(
                dimensionsProvider
                        .observeDimensions()
                        .subscribe { dimensions ->
                            if (viewFragment.toolbar != null) {
                                toolbar.post {
                                    if (viewFragment.toolbar != null) {
                                        updateDimens(dimensions)
                                    }
                                }
                            }
                            updateDimens(dimensions)
                        }
        )
    }

    private fun updateDimens(dimensions: DimensionHelper.Dimensions) {
        if (!configuration.isFitSystemWindow) {
            fragmentContainer.setPadding(
                    fragmentContainer.paddingLeft,
                    dimensions.statusBar,
                    fragmentContainer.paddingRight,
                    fragmentContainer.paddingBottom
            )
            return
        }
        val params = toolbar.layoutParams as CollapsingToolbarLayout.LayoutParams
        params.topMargin = dimensions.statusBar
        toolbar.layoutParams = params
    }

    protected fun baseInflateFragment(inflater: LayoutInflater, @LayoutRes res: Int) {
        inflater.inflate(res, fragmentContent, true)
    }

    @JvmOverloads
    protected fun setListsBackground(view: View = coordinatorLayout) {
        view.setBackgroundColor(App.getColorFromAttr(context, R.attr.background_for_lists))
    }

    @JvmOverloads
    protected fun setCardsBackground(view: View = coordinatorLayout) {
        view.setBackgroundColor(App.getColorFromAttr(context, R.attr.background_for_cards))
    }

    protected fun updateToolbarShadow() {
        val isVisible = isShadowVisible()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            preLpShadow.visibility = if (isVisible) View.VISIBLE else View.GONE
        } else {
            preLpShadow.visibility = if (isVisible) View.VISIBLE else View.GONE
        }
    }

    @CallSuper
    protected open fun addBaseToolbarMenu(menu: Menu) {

    }

    @CallSuper
    protected open fun refreshToolbarMenuItems(enable: Boolean) {

    }

    protected open fun initFabBehavior() {
        val params = fab.layoutParams as androidx.coordinatorlayout.widget.CoordinatorLayout.LayoutParams
        val behavior = ScrollAwareFABBehavior(fab.context, null)
        params.behavior = behavior
        fab.requestLayout()
    }

    protected fun refreshLayoutStyle(refreshLayout: androidx.swiperefreshlayout.widget.SwipeRefreshLayout) {
        refreshLayout.setProgressBackgroundColorSchemeColor(App.getColorFromAttr(context, R.attr.colorPrimary))
        refreshLayout.setColorSchemeColors(App.getColorFromAttr(context, R.attr.colorAccent))
    }

    protected fun refreshLayoutLongTrigger(refreshLayout: androidx.swiperefreshlayout.widget.SwipeRefreshLayout) {
        refreshLayout.setDistanceToTriggerSync(App.px48 * 3)
        refreshLayout.setProgressViewEndTarget(false, App.px48 * 3)
    }

    protected fun setScrollFlagsExitUntilCollapsed() {
        setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED)
    }

    protected fun setScrollFlagsEnterAlways() {
        setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS)
    }

    protected fun setScrollFlagsEnterAlwaysCollapsed() {
        setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL or AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS_COLLAPSED)
    }

    protected fun setScrollFlags(flags: Int) {
        val params = toolbarLayout.layoutParams as AppBarLayout.LayoutParams
        params.scrollFlags = flags
        toolbarLayout.layoutParams = params
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(BUNDLE_TITLE, titleText)
        outState.putString(BUNDLE_SUBTITLE, subtitleText)
        outState.putString(BUNDLE_TAB_TITLE, tabTitleText)
        outState.putBoolean(BUNDLE_CONFIG_ALONE, configuration.isAlone)
        outState.putBoolean(BUNDLE_CONFIG_MENU, configuration.isMenu)
    }


    fun findViewById(@IdRes id: Int): View {
        return viewFragment.findViewById(id)
    }

    override fun onResume() {
        super.onResume()
        if (!isHidden) {
            onResumeOrShow()
        }
        Log.d(LOG_TAG, "onResume " + this)
    }


    override fun onPause() {
        super.onPause()
        onPauseOrHide()
        Log.d(LOG_TAG, "onPause " + this)
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (hidden) {
            onPauseOrHide()
        } else {
            onResumeOrShow()
        }
    }

    @CallSuper
    open fun onResumeOrShow() {
        Log.e("ukulele", "onResumeOrShow " + this)
        updateStatusBar()
        attachedWebView?.onResume()
    }

    @CallSuper
    open fun onPauseOrHide() {
        Log.e("ukulele", "onPauseOrHide " + this)
        hideKeyboard()
        attachedWebView?.onPause()
    }

    private fun updateStatusBar() {
        val defaultSb = MainActivity.getDefaultLightStatusBar(mainActivity)
        MainActivity.setLightStatusBar(mainActivity, defaultSb)
    }

    @CallSuper
    override fun onDestroy() {
        super.onDestroy()
        mainActivity.tabNavigator.unsubscribe(this)
        attachedWebView = null
        Log.d(LOG_TAG, "onDestroy " + this)
        if (!disposables.isDisposed) {
            disposables.dispose()
        }
        hideKeyboard()
        contentController.destroy()
    }

    open protected fun attachWebView(webView: ExtendedWebView) {
        this.attachedWebView = webView
    }

    fun runInUiThread(action: Runnable) {
        if (Thread.currentThread() === mUiThread) {
            action.run()
        } else {
            mHandler.post(action)
        }
    }

    protected fun startRefreshing() {
        contentController.startRefreshing()
    }

    protected fun stopRefreshing() {
        contentController.stopRefreshing()
    }

    open fun setRefreshing(isRefreshing: Boolean) {
        if (isRefreshing)
            startRefreshing()
        else
            stopRefreshing()
    }

    companion object {
        private val LOG_TAG = TabFragment::class.java.simpleName
        private val BUNDLE_PREFIX = "tab_fragment_"
        private val CONFIG_PREFIX = BUNDLE_PREFIX + "config_"
        private val BUNDLE_TITLE = BUNDLE_PREFIX + "title"
        private val BUNDLE_TAB_TITLE = BUNDLE_PREFIX + "tab_title"
        private val BUNDLE_SUBTITLE = BUNDLE_PREFIX + "subtitle"
        private val BUNDLE_CONFIG_MENU = CONFIG_PREFIX + "menu"
        private val BUNDLE_CONFIG_ALONE = CONFIG_PREFIX + "alone"

        const val ARG_TITLE = "TAB_TITLE"
        const val ARG_SUBTITLE = "TAB_SUBTITLE"
        const val ARG_TAB = "TAB_URL"

        const val REQUEST_PICK_FILE = 1228
        const val REQUEST_SAVE_FILE = 1117
        const val REQUEST_STORAGE = 1
    }
}
