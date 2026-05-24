package forpdateam.ru.forpda.ui.fragments.other

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.databinding.FragmentOtherBinding
import forpdateam.ru.forpda.entity.app.CloseableInfo
import forpdateam.ru.forpda.entity.app.other.AppMenuItem
import forpdateam.ru.forpda.entity.remote.others.user.ForumUser
import forpdateam.ru.forpda.extensions.quillMoxyPresenter
import forpdateam.ru.forpda.presentation.other.OtherPresenter
import forpdateam.ru.forpda.presentation.other.OtherView
import forpdateam.ru.forpda.ui.fragments.TabFragment
import forpdateam.ru.forpda.ui.fragments.tabBinding
import forpdateam.ru.forpda.ui.views.drawers.adapters.DrawerMenuItem


/**
 * Created by radiationx on 16.12.17.
 */
class OtherFragment : TabFragment(R.layout.fragment_other), OtherView {

    companion object {
        fun newInstance() = OtherFragment()
    }

    private val binding by tabBinding(FragmentOtherBinding::bind)

    private val recyclerView: RecyclerView
        get() = binding.recyclerView

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

    private val presenter by quillMoxyPresenter<OtherPresenter>()

    init {
        configuration.defaultTitle = "Полное меню приложения"
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        appBarLayout.visibility = View.GONE
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this.context)
            adapter = otherAdapter

            val touchHelper = ItemTouchHelper(OtherItemDragCallback(itemDragListener))
            touchHelper.attachToRecyclerView(this)

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(
                    recyclerView: RecyclerView,
                    dx: Int,
                    dy: Int
                ) {
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

    override fun showItems(
        user: ForumUser?,
        infoList: List<CloseableInfo>,
        menu: List<List<AppMenuItem>>
    ) {
        otherAdapter.bindItems(user, infoList, menu)
    }

    override fun updateProfile() {
        otherAdapter.notifyDataSetChanged()
    }

    override fun setRefreshing(refreshing: Boolean) {}

    private val profileClickListener = { item: ForumUser? ->
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
