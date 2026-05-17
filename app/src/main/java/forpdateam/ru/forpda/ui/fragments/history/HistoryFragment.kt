package forpdateam.ru.forpda.ui.fragments.history

import android.os.Bundle
import android.view.Menu
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import forpdateam.ru.forpda.App
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.entity.app.history.HistoryItem
import forpdateam.ru.forpda.extensions.quillMoxyPresenter
import forpdateam.ru.forpda.presentation.history.HistoryPresenter
import forpdateam.ru.forpda.presentation.history.HistoryView
import forpdateam.ru.forpda.ui.fragments.RecyclerFragment
import forpdateam.ru.forpda.ui.views.ContentController
import forpdateam.ru.forpda.ui.views.DynamicDialogMenu
import forpdateam.ru.forpda.ui.views.FunnyContent
import forpdateam.ru.forpda.ui.views.adapters.BaseAdapter

/**
 * Created by radiationx on 06.09.17.
 */

class HistoryFragment : RecyclerFragment(), HistoryView {

    private lateinit var adapter: HistoryAdapter
    private lateinit var dialogMenu: DynamicDialogMenu<HistoryFragment, HistoryItem>

    private val adapterListener = object : BaseAdapter.OnItemClickListener<HistoryItem> {
        override fun onItemClick(item: HistoryItem) {
            presenter.onItemClick(item)
        }

        override fun onItemLongClick(item: HistoryItem): Boolean {
            presenter.onItemLongClick(item)
            return false
        }
    }

    private val presenter by quillMoxyPresenter<HistoryPresenter>()

    init {
        configuration.defaultTitle = App.get().getString(R.string.fragment_title_history)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setScrollFlagsEnterAlways()

        dialogMenu = DynamicDialogMenu()
        dialogMenu.apply {
            addItem(getString(R.string.copy_link)) { _, data ->
                presenter.copyLink(data)
            }
            addItem(getString(R.string.delete)) { _, data ->
                presenter.remove(data.id)
            }
        }

        adapter = HistoryAdapter(adapterListener)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
    }

    override fun addBaseToolbarMenu(menu: Menu) {
        super.addBaseToolbarMenu(menu)
        menu.add("Удалить историю")
            .setOnMenuItemClickListener {
                presenter.clear()
                false
            }
    }

    override fun showHistory(items: List<HistoryItem>) {
        if (items.isEmpty()) {
            if (!contentController.contains(ContentController.TAG_NO_DATA)) {
                val funnyContent = FunnyContent(requireContext())
                    .setImage(R.drawable.ic_history)
                    .setTitle(R.string.funny_history_nodata_title)
                    .setDesc(R.string.funny_history_nodata_desc)
                contentController.addContent(funnyContent, ContentController.TAG_NO_DATA)
            }
            contentController.showContent(ContentController.TAG_NO_DATA)
        } else {
            contentController.hideContent(ContentController.TAG_NO_DATA)
        }
        adapter.addAll(items)
    }

    override fun showItemDialogMenu(item: HistoryItem) {
        dialogMenu.apply {
            disallowAll()
            allowAll()
            show(requireContext(), this@HistoryFragment, item)
        }
    }

}
