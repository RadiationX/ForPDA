package forpdateam.ru.forpda.ui.fragments

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import androidx.core.view.doOnLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.viewbinding.ViewBinding
import by.kirich1409.viewbindingdelegate.ViewBindingProperty
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.databinding.FragmentBaseBinding
import forpdateam.ru.forpda.extensions.getColorFromAttr
import forpdateam.ru.forpda.extensions.getDimenPx
import forpdateam.ru.forpda.ui.DimensionHelper
import forpdateam.ru.forpda.ui.DimensionsProvider
import forpdateam.ru.forpda.ui.activities.MainActivity
import forpdateam.ru.forpda.ui.views.ContentController
import forpdateam.ru.forpda.ui.views.ExtendedWebView
import forpdateam.ru.forpda.ui.views.ScrollAwareFABBehavior
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import moxy.MvpAppCompatFragment
import ru.radiationx.quill.inject

/**
 * Created by radiationx on 07.08.16.
 */
fun <T : ViewBinding> TabFragment.tabBinding(
    vbFactory: (View) -> T,
): ViewBindingProperty<Fragment, T> {
    return viewBinding(
        vbFactory = vbFactory,
        viewProvider = {
            ViewCompat
                .requireViewById<FrameLayout>(requireView(), R.id.fragment_content)
                .getChildAt(0)
        }
    )
}

fun <T : ViewBinding> TabFragment.tabToolbarBinding(
    vbFactory: (View) -> T,
): ViewBindingProperty<Fragment, T> {
    return viewBinding(
        vbFactory = vbFactory,
        viewProvider = {
            ViewCompat.requireViewById(requireView(), R.id.toolbar_content)
        }
    )
}

open class TabFragment(
    @LayoutRes private val contentLayoutId: Int = 0
) : MvpAppCompatFragment(R.layout.fragment_base) {

    val configuration = TabConfiguration()

    private var titleText: String? = null
    private var tabTitleText: String? = null
    private var subtitleText: String? = null

    private val baseBinding by viewBinding<FragmentBaseBinding>()

    protected val toolbarProgress: ProgressBar
        get() = baseBinding.toolbarProgress
    protected val fragmentContainer: RelativeLayout
        get() = baseBinding.fragmentContainer
    protected val fragmentContent: ViewGroup
        get() = baseBinding.fragmentContent
    protected val additionalContent: ViewGroup
        get() = baseBinding.additionalContent
    protected val contentProgress: ProgressBar
        get() = baseBinding.contentProgress
    protected val titlesWrapper: LinearLayout
        get() = baseBinding.toolbarTitlesWrapper
    protected val coordinatorLayout: CoordinatorLayout
        get() = baseBinding.coordinatorLayout
    protected val appBarLayout: AppBarLayout
        get() = baseBinding.appbarLayout
    protected val toolbarLayout: CollapsingToolbarLayout
        get() = baseBinding.toolbarLayout
    protected val toolbar: Toolbar
        get() = baseBinding.toolbar
    protected val toolbarBackground: ImageView
        get() = baseBinding.toolbarImageBackground
    protected val toolbarImageView: ImageView
        get() = baseBinding.toolbarImageIcon
    protected val toolbarTitleView: TextView
        get() = baseBinding.toolbarTitle
    protected val toolbarSubtitleView: TextView
        get() = baseBinding.toolbarSubtitle
    protected val toolbarSpinner: Spinner
        get() = baseBinding.toolbarSpinner
    private val viewFragment: View
        get() = baseBinding.root
    protected val fab: FloatingActionButton
        get() = baseBinding.fab
    protected val preLpShadow: View
        get() = baseBinding.toolbarShadowPrelp
    protected val contentController: ContentController by lazy {
        ContentController(contentProgress, additionalContent, fragmentContent)
    }

    private val dimensionsProvider by inject<DimensionsProvider>()

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
    final override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        if (contentLayoutId != 0 && view != null) {
            inflater.inflate(
                contentLayoutId,
                view.findViewById(R.id.fragment_content),
                true
            )
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.doOnLayout { this.updateToolbarShadow() }

        toolbarTitleView.apply {
            ellipsize = TextUtils.TruncateAt.MARQUEE
            setHorizontallyScrolling(true)
            marqueeRepeatLimit = 3
            isSelected = true
            isHorizontalFadingEdgeEnabled = true
            setFadingEdgeLength(context.getDimenPx(R.dimen.dp16))
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

        dimensionsProvider
            .observeDimensions()
            .onEach { dimensions ->
                toolbar.doOnLayout {
                    updateDimens(dimensions)
                }
                updateDimens(dimensions)
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
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

    protected fun baseInflateToolbar(@LayoutRes res: Int) {
        baseBinding.toolbarContent.layoutResource = res
        baseBinding.toolbarContent.inflate()
    }

    @JvmOverloads
    protected fun setListsBackground(view: View = coordinatorLayout) {
        view.setBackgroundColor(view.context.getColorFromAttr(R.attr.background_for_lists))
    }

    @JvmOverloads
    protected fun setCardsBackground(view: View = coordinatorLayout) {
        view.setBackgroundColor(view.context.getColorFromAttr(R.attr.background_for_cards))
    }

    protected fun updateToolbarShadow() {
        val isVisible = isShadowVisible()
        preLpShadow.visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    @CallSuper
    protected open fun addBaseToolbarMenu(menu: Menu) {

    }

    @CallSuper
    protected open fun refreshToolbarMenuItems(enable: Boolean) {

    }

    protected open fun initFabBehavior() {
        val params =
            fab.layoutParams as CoordinatorLayout.LayoutParams
        val behavior = ScrollAwareFABBehavior(fab.context, null)
        params.behavior = behavior
        fab.requestLayout()
    }

    protected fun refreshLayoutStyle(refreshLayout: SwipeRefreshLayout) {
        refreshLayout.setProgressBackgroundColorSchemeColor(refreshLayout.context.getColorFromAttr(androidx.appcompat.R.attr.colorPrimary))
        refreshLayout.setColorSchemeColors(refreshLayout.context.getColorFromAttr(androidx.appcompat.R.attr.colorAccent))
    }

    protected fun refreshLayoutLongTrigger(refreshLayout: SwipeRefreshLayout) {
        refreshLayout.setDistanceToTriggerSync(refreshLayout.context.getDimenPx(R.dimen.dp48) * 3)
        refreshLayout.setProgressViewEndTarget(false, refreshLayout.context.getDimenPx(R.dimen.dp48) * 3)
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
    override fun onDestroyView() {
        super.onDestroyView()
        mainActivity.tabNavigator.unsubscribe(this)
        attachedWebView = null
        Log.d(LOG_TAG, "onDestroyView " + this)
        hideKeyboard()
        contentController.destroy()
    }

    @CallSuper
    override fun onDestroy() {
        super.onDestroy()
        Log.d(LOG_TAG, "onDestroy " + this)
    }

    protected open fun attachWebView(webView: ExtendedWebView) {
        this.attachedWebView = webView
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
    }
}
