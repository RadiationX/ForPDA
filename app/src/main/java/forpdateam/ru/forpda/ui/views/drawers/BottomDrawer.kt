package forpdateam.ru.forpda.ui.views.drawers

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import forpdateam.ru.forpda.App
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.databinding.ActivityMainBinding
import forpdateam.ru.forpda.entity.app.other.AppMenuItem
import forpdateam.ru.forpda.model.MenuMapper
import forpdateam.ru.forpda.model.interactors.other.MenuRepository
import forpdateam.ru.forpda.model.preferences.MainPreferencesHolder
import forpdateam.ru.forpda.presentation.Screen
import forpdateam.ru.forpda.presentation.TabRouter
import forpdateam.ru.forpda.ui.fragments.TabFragment
import forpdateam.ru.forpda.ui.fragments.TabTopScroller
import forpdateam.ru.forpda.ui.navigation.TabHelper
import forpdateam.ru.forpda.ui.navigation.TabNavigator
import forpdateam.ru.forpda.ui.views.adapters.BaseAdapter
import forpdateam.ru.forpda.ui.views.control.BottomSheetBehaviorFixed
import forpdateam.ru.forpda.ui.views.control.BottomSheetBehaviorRecyclerManager
import forpdateam.ru.forpda.ui.views.drawers.adapters.BottomMenuAdapter
import forpdateam.ru.forpda.ui.views.drawers.adapters.DrawerMenuItem
import forpdateam.ru.forpda.ui.views.drawers.adapters.TabAdapter
import forpdateam.ru.forpda.ui.views.drawers.adapters.TabSwipeToDeleteCallback
import io.reactivex.disposables.CompositeDisposable
import kotlin.math.min

class BottomDrawer(
    private val activity: FragmentActivity,
    private val binding: ActivityMainBinding,
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

    private lateinit var bottomSheetBehavior: BottomSheetBehaviorFixed<View>

    private val otherMenuItem =
        MenuMapper.mapToDrawer(AppMenuItem(MenuRepository.item_other_menu, Screen.OtherMenu()))
    private var localItems = listOf(otherMenuItem)

    init {
        binding.drawerLayout.apply {

            bottomSheetBehavior = BottomSheetBehaviorFixed.from<View>(binding.bottomSheet2).apply {
                isHideable = false
                state = BottomSheetBehaviorFixed.STATE_COLLAPSED
                peekHeight = context.resources.getDimensionPixelSize(R.dimen.dp48)

                setBottomSheetCallback(object : BottomSheetBehaviorFixed.BottomSheetCallback() {
                    private val colorDrawable = ColorDrawable(Color.TRANSPARENT)

                    init {
                        binding.bottomMenuFade.background = colorDrawable
                    }

                    private fun getColor(offset: Float) = Color.argb((96 * offset).toInt(), 0, 0, 0)

                    override fun onSlide(bottomSheet: View, slideOffset: Float) {
                        colorDrawable.color = getColor(slideOffset)
                        drawerListener?.onSlide(slideOffset)
                        binding.bottomToggleArrow.rotationX = 180 * slideOffset
                    }

                    @SuppressLint("SwitchIntDef")
                    override fun onStateChanged(bottomSheet: View, newState: Int) {


                        when (newState) {
                            BottomSheetBehaviorFixed.STATE_EXPANDED -> {
                                colorDrawable.color = getColor(1.0f)
                                binding.bottomMenuContainer.setOnClickListener {
                                    hide()
                                }
                                binding.bottomMenuContainer.isClickable = true
                                drawerListener?.onShow()
                            }

                            BottomSheetBehaviorFixed.STATE_COLLAPSED -> {
                                colorDrawable.color = Color.TRANSPARENT
                                binding.bottomMenuContainer.setOnClickListener(null)
                                binding.bottomMenuContainer.isClickable = false
                                drawerListener?.onHide()
                            }
                        }
                    }
                })
            }

            binding.bottomToggleArrow.setOnClickListener {
                toggle()
            }
            updateArrowVisible(mainPreferencesHolder.getShowBottomArrow())

            binding.bottomMenuRecycler.apply {
                layoutManager = GridLayoutManager(context, 5)
                adapter = menuAdapter
                isNestedScrollingEnabled = false
            }

            val manager = BottomSheetBehaviorRecyclerManager(
                binding.bottomMenuContainer,
                bottomSheetBehavior,
                binding.bottomSheet2
            )
            manager.addControl(binding.bottomTabsRecycler)
            manager.create()

            binding.bottomCloseAllTabs.setOnClickListener {
                removeAllTabs()
            }

            binding.bottomTabsRecycler.apply {
                layoutManager = LinearLayoutManager(context).apply {
                    stackFromEnd = true
                }
                adapter = tabsAdapter

                val color = App.getColorFromAttr(context, R.attr.item_tab_close_color)
                val swipeHandler = object : TabSwipeToDeleteCallback(color) {
                    override fun onSwiped(
                        viewHolder: RecyclerView.ViewHolder,
                        p1: Int
                    ) {
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

            tabsAdapter.setCloseClickListener(object :
                BaseAdapter.OnItemClickListener<TabFragment> {
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
                            otherMenuItem.appItem.count = notExistMainCounters.sumOf { it.count }
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
        (binding.bottomMenuRecycler.layoutManager as? GridLayoutManager)?.spanCount =
            localItems.size
        menuAdapter.bindItems(localItems)
    }

    private fun updateArrowVisible(isVisible: Boolean) {
        binding.bottomToggleArrow.visibility = if (isVisible) {
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
        bottomSheetBehavior.state = BottomSheetBehaviorFixed.STATE_EXPANDED
    }

    fun hide() {
        bottomSheetBehavior.state = BottomSheetBehaviorFixed.STATE_COLLAPSED
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
        binding.bottomTabsRecycler.layoutManager = null
    }

    fun onStart() {
        binding.bottomTabsRecycler.apply {
            layoutManager = LinearLayoutManager(context).apply {
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
