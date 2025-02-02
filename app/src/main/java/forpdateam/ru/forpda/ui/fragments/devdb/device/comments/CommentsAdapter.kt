package forpdateam.ru.forpda.ui.fragments.devdb.device.comments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import forpdateam.ru.forpda.App.Companion.getDrawableAttr
import forpdateam.ru.forpda.App.Companion.getVecDrawable
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.entity.remote.devdb.Device
import forpdateam.ru.forpda.model.data.remote.api.ApiUtils.spannedFromHtml
import forpdateam.ru.forpda.ui.fragments.devdb.DevDbHelper.getColorFilter
import forpdateam.ru.forpda.ui.fragments.devdb.device.comments.CommentsAdapter.CommentHolder
import forpdateam.ru.forpda.ui.views.adapters.BaseAdapter
import forpdateam.ru.forpda.ui.views.adapters.BaseViewHolder

/**
 * Created by radiationx on 09.08.17.
 */
class CommentsAdapter(
    private val listener: CommentHolder.Listener
) : BaseAdapter<Device.Comment, CommentHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentHolder {
        val v =
            LayoutInflater.from(parent.context).inflate(R.layout.device_comment_item, parent, false)
        return CommentHolder(v, listener)
    }

    override fun onBindViewHolder(holder: CommentHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    class CommentHolder(v: View, listener: Listener) : BaseViewHolder<Device.Comment>(v) {
        private val title: TextView = v.findViewById(R.id.item_title)
        private val date: TextView = v.findViewById(R.id.item_date)
        private val desc: TextView = v.findViewById(R.id.item_desc)
        private val rating: TextView = v.findViewById(R.id.item_rating)
        private val like: Button = v.findViewById(R.id.item_like_btn)
        private val dislike: Button = v.findViewById(R.id.item_dislike_btn)
        private var currentItem: Device.Comment? = null

        init {
            like.setCompoundDrawablesRelativeWithIntrinsicBounds(
                getVecDrawable(
                    v.context,
                    R.drawable.ic_thumb_up
                ), null, null, null
            )
            dislike.setCompoundDrawablesRelativeWithIntrinsicBounds(
                getVecDrawable(
                    v.context,
                    R.drawable.ic_thumb_down
                ), null, null, null
            )
            title.setOnClickListener { view: View? -> listener.onClick(requireNotNull(currentItem)) }
            rating.background = getDrawableAttr(rating.context, R.attr.count_background)
        }

        override fun bind(item: Device.Comment, position: Int) {
            currentItem = item
            title.text = item.user.nick
            date.text = item.date
            desc.text = spannedFromHtml(item.text)
            rating.text = item.rating.toString()
            like.text = item.likes.toString()
            dislike.text = item.dislikes.toString()
            rating.background.colorFilter = getColorFilter(item.rating)
        }

        fun interface Listener {
            fun onClick(item: Device.Comment)
        }
    }
}
