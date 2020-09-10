package forpdateam.ru.forpda.ui.views.drawers

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import androidx.fragment.app.FragmentActivity
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.ItemTouchHelper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import forpdateam.ru.forpda.App
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.entity.app.other.AppMenuItem
import forpdateam.ru.forpda.model.MenuMapper
import forpdateam.ru.forpda.model.interactors.other.MenuRepository
import forpdateam.ru.forpda.model.preferences.MainPreferencesHolder
import forpdateam.ru.forpda.presentation.Screen
import forpdateam.ru.forpda.presentation.TabRouter
import forpdateam.ru.forpda.ui.views.drawers.adapters.DrawerMenuItem
import forpdateam.ru.forpda.ui.views.drawers.adapters.BottomMenuAdapter
import forpdateam.ru.forpda.ui.fragments.TabFragment
import forpdateam.ru.forpda.ui.fragments.TabTopScroller
import forpdateam.ru.forpda.ui.navigation.TabHelper
import forpdateam.ru.forpda.ui.navigation.TabNavigator
import forpdateam.ru.forpda.ui.views.BottomSheetBehaviorRecyclerManager
import forpdateam.ru.forpda.ui.views.BottomSheetBehavior_v27
import forpdateam.ru.forpda.ui.views.adapters.BaseAdapter
import forpdateam.ru.forpda.ui.views.drawers.adapters.TabSwipeToDeleteCallback
import forpdateam.ru.forpda.ui.views.drawers.adapters.TabAdapter
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlin.math.min

class BottomDrawer(
        private val activity: androidx.fragment.app.FragmentActivity,
        private val drawerLayout: ViewGroup,
        private val tabNavigator: TabNavigator,
        private val router: TabRouter,
        private val menuRepository: MenuRepository,
        private val mainPreferencesHolder: MainPreferencesHolder
) {
    private val menuAdapter = BottomMenuAdapter(object : BottomMenuAdapter.Listener {
        override fun onTabClick(menu: DrawerMenuItem) {
            menu.appItem.let { item ->
                item.screen?.also { screen ->
                    if (screen.getKey() == tabNavigator.tabController.getCurrent()?.screen?.key) {
                        (tabNavigator.getCurrentFragment() as? TabTopScroller)?.toggleScrollTop()
                    }
                    router.navigateTo(screen)
                }
                menuRepository.setLastOpened(item.id)
            }
            hide()
        }
    })

    private var drawerListener: DrawerListener? = null

    private val tabsAdapter = TabAdapter()

    private val compositeDisposable = CompositeDisposable()

    private lateinit var bottomSheetBehavior: BottomSheetBehavior_v27<View>

    private val otherMenuItem = MenuMapper.mapToDrawer(AppMenuItem(MenuRepository.item_other_menu, Screen.OtherMenu()))
    private var localItems = listOf(otherMenuItem)

    init {
        drawerLayout.apply {

            bottomSheetBehavior = BottomSheetBehavior_v27.from<View>(bottom_sheet2).apply {
                isHideable = false
                state = BottomSheetBehavior_v27.STATE_COLLAPSED
                peekHeight = context.resources.getDimensionPixelSize(R.dimen.dp48)

                setBottomSheetCallback(object : BottomSheetBehavior_v27.BottomSheetCallback() {
                    private val colorDrawable = ColorDrawable(Color.TRANSPARENT)

                    init {
                        bottomMenuFade.background = colorDrawable
                    }

                    private fun getColor(offset: Float) = Color.argb((96 * offset).toInt(), 0, 0, 0)

                    override fun onSlide(bottomSheet: View, slideOffset: Float) {
                        colorDrawable.color = getColor(slideOffset)
                        drawerListener?.onSlide(slideOffset)
                        bottomToggleArrow.rotationX = 180 * slideOffset
                    }

                    @SuppressLint("SwitchIntDef")
                    override fun onStateChanged(bottomSheet: View, newState: Int) {


                        when (newState) {
                            BottomSheetBehavior_v27.STATE_EXPANDED -> {
                                colorDrawable.color = getColor(1.0f)
                                bottomMenuContainer.setOnClickListener {
                                    hide()
                                }
                                bottomMenuContainer.isClickable = true
                                drawerListener?.onShow()
                            }
                            BottomSheetBehavior_v27.STATE_COLLAPSED -> {
                                colorDrawable.color = Color.TRANSPARENT
                                bottomMenuContainer.setOnClickListener(null)
                                bottomMenuContainer.isClickable = false
                                drawerListener?.onHide()
                            }
                        }
                    }
                })
            }

            bottomToggleArrow.setOnClickListener {
                toggle()
            }
            updateArrowVisible(mainPreferencesHolder.getShowBottomArrow())

            bottomMenuRecycler.apply {
                layoutManager = androidx.recyclerview.widget.GridLayoutManager(context, 5)
                adapter = menuAdapter
                isNestedScrollingEnabled = false
            }

            val manager = BottomSheetBehaviorRecyclerManager(bottomMenuContainer, bottomSheetBehavior, bottom_sheet2)
            manager.addControl(bottomTabsRecycler)
            manager.create()

            bottomCloseAllTabs.setOnClickListener {
                removeAllTabs()
            }

            bottomTabsRecycler.apply {
                layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context).apply {
                    stackFromEnd = true
                }
                adapter = tabsAdapter

                val color = App.getColorFromAttr(context, R.attr.item_tab_close_color)
                val swipeHandler = object : TabSwipeToDeleteCallback(color) {
                    override fun onSwiped(viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder, p1: Int) {
                        val tab = tabsAdapter.getItem(viewHolder.adapterPosition)
                        tabNavigator.close(tab?.tag)
                    }
                }
                val itemTouchHelper = ItemTouchHelper(swipeHandler)
                itemTouchHelper.attachToRecyclerView(this)
            }

            tabsAdapter.setItemClickListener(object : BaseAdapter.OnItemClickListener<TabFragment> {
                override fun onItemClick(item: TabFragment) {
                    tabNavigator.select(item.tag)
                    hide()
                }

                override fun onItemLongClick(item: TabFragment): Boolean {
                    return false
                }
            })

            tabsAdapter.setCloseClickListener(object : BaseAdapter.OnItemClickListener<TabFragment> {
                override fun onItemClick(item: TabFragment) {
                    tabNavigator.close(item.tag)
                }

                override fun onItemLongClick(item: TabFragment): Boolean {
                    return false
                }
            })

            compositeDisposable.add(
                    mainPreferencesHolder
                            .observeShowBottomArrow()
                            .subscribe {
                                updateArrowVisible(it)
                            }
            )

            compositeDisposable.add(
                    menuRepository
                            .observerMenu()
                            .subscribe {
                                it[MenuRepository.group_main]?.let { newItems ->
                                    val mainItems = newItems
                                            .filter { it.id != MenuRepository.item_auth }
                                            .take(min(newItems.size, 4))
                                            .map { MenuMapper.mapToDrawer(it) }
                                    val notExistMainCounters = newItems
                                            .filterNot { newItem ->
                                                mainItems.indexOfFirst { newItem.id == it.appItem.id } >= 0
                                            }
                                            .filter { it.count > 0 }
                                    otherMenuItem.appItem.count = notExistMainCounters.sumBy { it.count }
                                    localItems = mainItems.plusElement(otherMenuItem)
                                }
                                updateMenu()
                            }
            )

            compositeDisposable.add(
                    tabNavigator
                            .observeSubscribers()
                            .subscribe({
                                Log.e("lalala", "Menu subscribe")
                                tabsAdapter.setCurrentFragmentTag(tabNavigator.getCurrentFragment()?.tag)
                                tabsAdapter.addAll(it)
                                it.firstOrNull { tabNavigator.tabController.isCurrent(it.tag) }?.also {
                                    Log.e("lalala", "Menu activetab: $it")
                                    val screen = TabHelper.findScreenByFragment(it)
                                    Log.e("lalala", "Menu activescreen: $screen")
                                    findMenuItem(screen)?.also {
                                        selectMenuItem(it)
                                    }
                                }
                            }, {
                                Log.d("lalala", "menu error: ${it.message}")
                            })
            )
        }
    }

    private fun updateMenu() {
        (drawerLayout.bottomMenuRecycler.layoutManager as? androidx.recyclerview.widget.GridLayoutManager)?.spanCount = localItems.size
        menuAdapter.bindItems(localItems)
    }

    private fun updateArrowVisible(isVisible: Boolean) {
        drawerLayout.bottomToggleArrow.visibility = if (isVisible) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    fun setListener(listener: DrawerListener) {
        drawerListener = listener
    }

    fun isShown() = bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED

    fun show() {
        bottomSheetBehavior.state = BottomSheetBehavior_v27.STATE_EXPANDED
    }

    fun hide() {
        bottomSheetBehavior.state = BottomSheetBehavior_v27.STATE_COLLAPSED
    }

    fun toggle() {
        if (isShown()) {
            hide()
        } else {
            show()
        }
    }

    /* Очень странная хрень с этими onstop|onpause - когда переходишь в браузер по ссылке,
    или просто открывается intentchoser и ты скрываешь приложение, то не обновляется список
    фрагментов. Прям вот вызывается notify... но ничего не происходит */
    fun onStop() {
        drawerLayout.bottomTabsRecycler.layoutManager = null
    }

    fun onStart() {
        drawerLayout.bottomTabsRecycler.apply {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context).apply {
                stackFromEnd = true
            }
        }
    }

    fun destroy() {
        compositeDisposable.dispose()
    }

    private fun selectMenuItem(item: DrawerMenuItem) {
        Log.e("bottom drawer", "selectMenuItem ${item.appItem.screen?.getKey()}")
        item.appItem.screen?.getKey()?.let { menuAdapter.setSelected(it) }
    }

    private fun findMenuItem(appMenuId: Int): DrawerMenuItem? {
        for (item in localItems) {
            if (item.appItem.id == appMenuId)
                return item
        }
        return null
    }

    private fun findMenuItem(classObject: Class<out Screen>): DrawerMenuItem? {
        for (item in localItems) {
            if (item.appItem.screen?.javaClass == classObject)
                return item
        }
        return null
    }

    private fun removeAllTabs() {
        AlertDialog.Builder(activity)
                .setMessage(R.string.ask_close_other_tabs)
                .setPositiveButton(R.string.ok) { dialog, which ->
                    tabNavigator.closeOthers()
                    hide()
                }
                .setNegativeButton(R.string.no, null)
                .show()
    }

    interface DrawerListener {
        fun onHide()
        fun onShow()
        fun onSlide(slideOffset: Float)
    }
}
