package forpdateam.ru.forpda.ui.fragments.mentions

import android.graphics.Typeface
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import forpdateam.ru.forpda.App.Companion.getColorFromAttr
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.entity.remote.mentions.MentionItem
import forpdateam.ru.forpda.entity.remote.mentions.MentionItem.Companion.STATE_UNREAD
import forpdateam.ru.forpda.ui.fragments.mentions.MentionsAdapter.MentionHolder
import forpdateam.ru.forpda.ui.views.adapters.BaseAdapter
import forpdateam.ru.forpda.ui.views.adapters.BaseViewHolder

/**
 * Created by radiationx on 21.01.17.
 */
internal class MentionsAdapter : BaseAdapter<MentionItem, MentionHolder>() {
    private var titleColorNew = 0
    private var titleColor = 0
    private var itemClickListener: OnItemClickListener<MentionItem>? = null

    fun setOnItemClickListener(mItemClickListener: OnItemClickListener<MentionItem>?) {
        this.itemClickListener = mItemClickListener
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        titleColor = getColorFromAttr(recyclerView.context, R.attr.second_text_color)
        titleColorNew = getColorFromAttr(recyclerView.context, R.attr.default_text_color)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MentionHolder {
        val v = inflateLayout(parent, R.layout.topic_item)
        return MentionHolder(v)
    }

    override fun onBindViewHolder(holder: MentionHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    internal inner class MentionHolder(v: View) : BaseViewHolder<MentionItem>(v),
        View.OnClickListener, OnLongClickListener {
        var title: TextView = v.findViewById(R.id.topic_item_title)
        var lastNick: TextView =
            v.findViewById(R.id.topic_item_last_nick)
        var date: TextView = v.findViewById(R.id.topic_item_date)
        var desc: TextView = v.findViewById(R.id.topic_item_desc)
        var forumIcon: ImageView =
            v.findViewById(R.id.topic_item_forum_icon)
        var lockIcon: ImageView =
            v.findViewById(R.id.topic_item_lock_icon)
        var pollIcon: ImageView =
            v.findViewById(R.id.topic_item_poll_icon)

        init {
            v.setOnClickListener(this)
            v.setOnLongClickListener(this)
        }

        override fun bind(item: MentionItem, position: Int) {
            title.text = item.title
            title.typeface =
                if (item.state == STATE_UNREAD) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
            title.setTextColor(if (item.state == STATE_UNREAD) titleColorNew else titleColor)
            lastNick.text = item.nick
            date.text = item.date
            if (desc.visibility == View.VISIBLE) {
                desc.visibility = View.GONE
            }
        }

        override fun onClick(view: View) {
            if (itemClickListener != null) {
                itemClickListener!!.onItemClick(getItem(layoutPosition))
            }
        }

        override fun onLongClick(view: View): Boolean {
            if (itemClickListener != null) {
                itemClickListener!!.onItemLongClick(getItem(layoutPosition))
                return true
            }
            return false
        }
    }
}
