package forpdateam.ru.forpda.ui.fragments.devdb.device.posts

import android.view.View
import android.view.ViewGroup
import com.nostra13.universalimageloader.core.ImageLoader
import by.kirich1409.viewbindingdelegate.viewBinding
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.databinding.DevicePostForumItemBinding
import forpdateam.ru.forpda.databinding.DevicePostNewsItemBinding
import forpdateam.ru.forpda.entity.remote.devdb.Device.PostItem
import forpdateam.ru.forpda.model.data.remote.api.ApiUtils.spannedFromHtml
import forpdateam.ru.forpda.ui.views.adapters.BaseAdapter
import forpdateam.ru.forpda.ui.views.adapters.BaseViewHolder

/**
 * Created by radiationx on 09.08.17.
 */
class PostsAdapter(
    private val source: Int,
    private val listener: Listener
) : BaseAdapter<PostItem, BaseViewHolder<PostItem>>() {


    val layout: Int
        get() {
            if (source == PostsFragment.SRC_NEWS) {
                return R.layout.device_post_news_item
            }
            return R.layout.device_post_forum_item
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<PostItem> {
        if (source == PostsFragment.SRC_NEWS) {
            return NewsPostHolder(inflateLayout(parent, R.layout.device_post_news_item), listener)
        }
        return ForumPostHolder(inflateLayout(parent, R.layout.device_post_news_item), listener)
    }

    override fun onBindViewHolder(holder: BaseViewHolder<PostItem>, position: Int) {
        if (source == PostsFragment.SRC_NEWS) {
            (holder as NewsPostHolder).bind(getItem(position), position)
        } else {
            (holder as ForumPostHolder).bind(getItem(position), position)
        }
    }

    class NewsPostHolder(v: View, listener: Listener) : BaseViewHolder<PostItem>(v) {
        private val binding by viewBinding<DevicePostNewsItemBinding>()
        private var currentItem: PostItem? = null

        init {
            v.setOnClickListener((View.OnClickListener { v1: View? ->
                listener.onClick(requireNotNull(currentItem))
            }))
        }

        override fun bind(item: PostItem, position: Int) {
            currentItem = item
            binding.itemTitle.text = item.title
            binding.itemDate.text = item.date
            if (item.desc != null) {
                binding.itemDesc.text = spannedFromHtml(item.desc)
                binding.itemDesc.visibility = View.VISIBLE
            } else {
                binding.itemDesc.visibility = View.GONE
            }
            if (item.image != null) {
                ImageLoader.getInstance().displayImage(item.image, binding.itemImage)
            }
        }
    }

    class ForumPostHolder(v: View, listener: Listener) : BaseViewHolder<PostItem>(v) {
        private val binding by viewBinding<DevicePostForumItemBinding>()
        private var currentItem: PostItem? = null

        init {
            v.setOnClickListener((View.OnClickListener { v1: View? ->
                listener.onClick(requireNotNull(currentItem))
            }))
        }

        override fun bind(item: PostItem, position: Int) {
            currentItem = item
            binding.itemTitle.text = item.title
            binding.itemDate.text = item.date
            if (item.desc != null) {
                binding.itemDesc.text = spannedFromHtml(item.desc)
                binding.itemDesc.visibility = View.VISIBLE
            } else {
                binding.itemDesc.visibility = View.GONE
            }
        }
    }

    fun interface Listener {
        fun onClick(item: PostItem)
    }
}
