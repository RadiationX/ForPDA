package forpdateam.ru.forpda.ui.fragments.forum

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.unnamed.b.atv.model.TreeNode
import com.unnamed.b.atv.model.TreeNode.BaseNodeViewHolder
import forpdateam.ru.forpda.App.Companion.getDrawableResAttr
import forpdateam.ru.forpda.App.Companion.getVecDrawable
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.entity.remote.forum.ForumItemTree

/**
 * Created by radiationx on 28.02.17.
 */
class DefaultForumHolder(context: Context?) : BaseNodeViewHolder<ForumItemTree>(context) {
    var title: TextView? = null
    var icon: ImageView? = null
    var currentValue: ForumItemTree? = null

    override fun createNodeView(node: TreeNode, value: ForumItemTree): View {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.forum_item_default, null, false)
        title = view.findViewById(R.id.forum_item_title)
        icon = view.findViewById(R.id.forum_item_icon)

        currentValue = value
        title!!.setText(value.item.title)

        icon!!.setImageDrawable(
            getVecDrawable(
                context,
                if (value.forums.isEmpty()) R.drawable.ic_forum_go_to_topics else (if (node.isExpanded) R.drawable.ic_expand_less_black_24dp else R.drawable.ic_expand_more_black_24dp)
            )
        )

        if (value.forums.isEmpty()) {
            val bg = getDrawableResAttr(context, R.attr.count_background)
            icon!!.setBackgroundResource(bg)
        } else {
            icon!!.setBackground(null)
        }

        return view
    }

    override fun toggle(active: Boolean) {
        if (currentValue!!.forums.isEmpty()) {
            icon!!.rotationY = if (active) 1f else 0f
            icon!!.setImageDrawable(
                getVecDrawable(
                    context,
                    if (active) R.drawable.ic_expand_less_black_24dp else R.drawable.ic_expand_more_black_24dp
                )
            )
        }
    }
}
