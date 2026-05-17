package forpdateam.ru.forpda.ui.fragments.devdb.device.comments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import by.kirich1409.viewbindingdelegate.viewBinding
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.databinding.DeviceCommentItemBinding
import forpdateam.ru.forpda.entity.remote.devdb.Device
import forpdateam.ru.forpda.extensions.setBackgroundAttr
import forpdateam.ru.forpda.extensions.setBackgroundTintColor
import forpdateam.ru.forpda.model.data.remote.api.ApiUtils
import forpdateam.ru.forpda.ui.fragments.devdb.DevDbHelper
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
                binding.itemLikeBtn.context.getDrawable(R.drawable.ic_thumb_up), null, null, null
            )
            binding.itemDislikeBtn.setCompoundDrawablesRelativeWithIntrinsicBounds(
                binding.itemLikeBtn.context.getDrawable(R.drawable.ic_thumb_down), null, null, null
            )
            binding.itemTitle.setOnClickListener { view: View? ->
                listener.onClick(
                    requireNotNull(
                        currentItem
                    )
                )
            }
            binding.itemRating.setBackgroundAttr(R.attr.count_background)
        }

        override fun bind(item: Device.Comment, position: Int) {
            currentItem = item
            binding.itemTitle.text = item.user.nick
            binding.itemDate.text = item.date
            binding.itemDesc.text = ApiUtils.spannedFromHtml(item.text)
            binding.itemRating.text = item.rating.toString()
            binding.itemLikeBtn.text = item.likes.toString()
            binding.itemDislikeBtn.text = item.dislikes.toString()
            binding.itemRating.setBackgroundTintColor(DevDbHelper.getColor(item.rating))
        }

        fun interface Listener {
            fun onClick(item: Device.Comment)
        }
    }
}
