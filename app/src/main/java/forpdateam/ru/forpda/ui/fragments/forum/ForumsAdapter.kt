package forpdateam.ru.forpda.ui.fragments.forum

import com.hannesdorfmann.adapterdelegates4.ListDelegationAdapter
import forpdateam.ru.forpda.entity.remote.forum.ForumItemFlat
import forpdateam.ru.forpda.ui.views.adapters.OnItemClickListener
import forpdateam.ru.forpda.ui.views.adapters.tree.ExpandableTree
import forpdateam.ru.forpda.ui.views.adapters.tree.ListToTreeTransformer
import forpdateam.ru.forpda.ui.views.drawers.adapters.ForumListItem
import forpdateam.ru.forpda.ui.views.drawers.adapters.ListItem

class ForumsAdapter(
    private val clickListener: OnItemClickListener<ForumItemFlat>
) : ListDelegationAdapter<List<ListItem>>() {

    private val clickInterceptor = object : OnItemClickListener<ForumListItem> {
        override fun onItemClick(item: ForumListItem) {
            if (item.isLeaf) {
                clickListener.onItemClick(item.item)
            } else {
                setExpanded(item.item.id, !item.expanded)
            }
        }

        override fun onItemLongClick(item: ForumListItem): Boolean {
            return clickListener.onItemLongClick(item.item)
        }
    }

    private val expandableTree = ExpandableTree<Int, ForumItemFlat>()

    private val transformer = ListToTreeTransformer<Int, ForumItemFlat>(
        keyGetter = { it.id },
        parentKeyGetter = { it.parentId }
    )

    init {
        delegatesManager.apply {
            addDelegate(ForumDelegate(clickInterceptor))
        }
    }

    fun bindItems(data: List<ForumItemFlat>) {
        val tree = transformer.transform(data, -1)
        expandableTree.setRoot(tree)
        updateItems()
    }

    fun expand(id: Int) {
        setExpanded(id, true)
    }

    fun getItemPosition(id: Int): Int {
        return items?.indexOfFirst { (it as? ForumListItem)?.item?.id == id } ?: -1
    }

    private fun setExpanded(id: Int, state: Boolean) {
        expandableTree.setExpanded(id, state)
        updateItems()
    }

    private fun updateItems() {
        this.items = expandableTree.transformSequence { node, expanded ->
            ForumListItem(node.value, node.level, node.children.isEmpty(), expanded)
        }.toList()
        notifyDataSetChanged()
    }
}