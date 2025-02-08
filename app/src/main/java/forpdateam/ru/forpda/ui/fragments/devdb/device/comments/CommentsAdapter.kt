package forpdateam.ru.forpda.ui.fragments.devdb.device.comments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import by.kirich1409.viewbindingdelegate.viewBinding
import forpdateam.ru.forpda.App.Companion.getDrawableAttr
import forpdateam.ru.forpda.App.Companion.getVecDrawable
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.databinding.DeviceCommentItemBinding
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
        private val binding by viewBinding<DeviceCommentItemBinding>()

        private var currentItem: Device.Comment? = null

        init {
            binding.itemLikeBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(
                getVecDrawable(
                    v.context,
                    R.drawable.ic_thumb_up
                ), null, null, null
            )
            binding.itemDislikeBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(
                getVecDrawable(
                    v.context,
                    R.drawable.ic_thumb_down
                ), null, null, null
            )
            binding.itemTitle.setOnClickListener { view: View? ->
                listener.onClick(
                    requireNotNull(
                        currentItem
                    )
                )
            }
            binding.itemRating.background =
                getDrawableAttr(binding.itemRating.context, R.attr.count_background)
        }

        override fun bind(item: Device.Comment, position: Int) {
            currentItem = item
            binding.itemTitle.text = item.user.nick
            binding.itemDate.text = item.date
            binding.itemDesc.text = spannedFromHtml(item.text)
            binding.itemRating.text = item.rating.toString()
            binding.itemLikeBtn.text = item.likes.toString()
            binding.itemDislikeBtn.text = item.dislikes.toString()
            binding.itemRating.background.colorFilter = getColorFilter(item.rating)
        }

        fun interface Listener {
            fun onClick(item: Device.Comment)
        }
    }
}
