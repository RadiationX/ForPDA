package forpdateam.ru.forpda.ui.fragments.other

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import forpdateam.ru.forpda.App
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.entity.app.CloseableInfo
import forpdateam.ru.forpda.entity.app.other.AppMenuItem
import forpdateam.ru.forpda.entity.remote.profile.ProfileModel
import forpdateam.ru.forpda.presentation.other.OtherPresenter
import forpdateam.ru.forpda.presentation.other.OtherView
import forpdateam.ru.forpda.ui.fragments.TabFragment
import forpdateam.ru.forpda.ui.views.drawers.adapters.DrawerMenuItem
import kotlinx.android.synthetic.main.fragment_other.*


/**
 * Created by radiationx on 16.12.17.
 */
class OtherFragment : TabFragment(), OtherView {

    private val otherAdapter by lazy {
        OtherAdapter(
                profileClickListener,
                logoutClickListener,
                menuClickListener,
                menuSequenceListener,
                infoCloseClickListener
        )
    }

    private var listScrollY = 0

    @InjectPresenter
    lateinit var presenter: OtherPresenter

    @ProvidePresenter
    fun provideOtherPresenter(): OtherPresenter {
        return OtherPresenter(
                App.get().Di().router,
                App.get().Di().authRepository,
                App.get().Di().profileRepository,
                App.get().Di().authHolder,
                App.get().Di().errorHandler,
                App.get().Di().menuRepository,
                App.get().Di().closeableInfoHolder,
                App.get().Di().linkHandler,
                App.get().Di().systemLinkHandler
        )
    }

    init {
        configuration.defaultTitle = "Полное меню приложения"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        baseInflateFragment(inflater, R.layout.fragment_other)
        return viewFragment
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        appBarLayout.visibility = View.GONE
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this.context)
            adapter = otherAdapter

            val touchHelper = ItemTouchHelper(OtherItemDragCallback(otherAdapter, itemDragListener))
            touchHelper.attachToRecyclerView(this)

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    listScrollY = recyclerView.computeVerticalScrollOffset()
                    updateToolbarShadow()
                }
            })
        }
    }

    override fun isShadowVisible(): Boolean {
        return listScrollY != 0
    }

    override fun showItems(profileItem: ProfileModel?, infoList: List<CloseableInfo>, menu: List<List<AppMenuItem>>) {
        otherAdapter.bindItems(profileItem, infoList, menu)
    }

    override fun updateProfile() {
        otherAdapter.notifyDataSetChanged()
    }

    override fun setRefreshing(refreshing: Boolean) {}

    private val profileClickListener = { item: ProfileModel? ->
        presenter.onProfileClick()
    }

    private val logoutClickListener = { presenter.signOut() }

    private val menuClickListener = { item: DrawerMenuItem -> presenter.onMenuClick(item.appItem) }

    private val infoCloseClickListener = { item: CloseableInfo -> presenter.onCloseInfo(item) }

    private val menuSequenceListener = { items: List<AppMenuItem> ->
        presenter.onChangeMenuSequence(items)
        Log.e("lplplp", "sequence ${items.joinToString { it.screen?.getKey().orEmpty() }}")
        Unit
    }

    private val itemDragListener = object : OtherItemDragCallback.ItemTouchHelperListener {
        override fun onDragStart() {
            presenter.onMenuDragModeChange(true)
        }

        override fun onDragEnd() {
            presenter.onMenuDragModeChange(false)
        }

        override fun onItemMove(fromPosition: Int, toPosition: Int) {
            otherAdapter.onItemMove(fromPosition, toPosition)
        }
    }

}
