package forpdateam.ru.forpda.ui.fragments.mentions

import android.graphics.Typeface
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.databinding.TopicItemBinding
import forpdateam.ru.forpda.entity.remote.mentions.MentionItem
import forpdateam.ru.forpda.entity.remote.mentions.MentionItem.Companion.STATE_UNREAD
import forpdateam.ru.forpda.extensions.getColorFromAttr
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
        titleColor = recyclerView.context.getColorFromAttr(R.attr.second_text_color)
        titleColorNew = recyclerView.context.getColorFromAttr(R.attr.default_text_color)
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

        private val binding by viewBinding<TopicItemBinding>()

        init {
            v.setOnClickListener(this)
            v.setOnLongClickListener(this)
        }

        override fun bind(item: MentionItem, position: Int) {
            binding.topicItemTitle.text = item.title
            binding.topicItemTitle.typeface =
                if (item.state == STATE_UNREAD) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
            binding.topicItemTitle.setTextColor(if (item.state == STATE_UNREAD) titleColorNew else titleColor)
            binding.topicItemLastNick.text = item.nick
            binding.topicItemDate.text = item.date
            if (binding.topicItemDesc.visibility == View.VISIBLE) {
                binding.topicItemDesc.visibility = View.GONE
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
