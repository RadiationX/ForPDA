package forpdateam.ru.forpda.ui.fragments.devdb.device.posts

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.nostra13.universalimageloader.core.ImageLoader
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.entity.remote.devdb.Device.PostItem
import forpdateam.ru.forpda.model.data.remote.api.ApiUtils.spannedFromHtml
import forpdateam.ru.forpda.ui.fragments.devdb.device.posts.PostsAdapter.PostHolder
import forpdateam.ru.forpda.ui.views.adapters.BaseAdapter
import forpdateam.ru.forpda.ui.views.adapters.BaseViewHolder

/**
 * Created by radiationx on 09.08.17.
 */
class PostsAdapter(
    private val listener: PostHolder.Listener
) : BaseAdapter<PostItem, PostHolder>() {

    private var source = 0

    fun setSource(source: Int) {
        this.source = source
    }

    val layout: Int
        get() {
            if (source == PostsFragment.SRC_NEWS) {
                return R.layout.device_post_news_item
            }
            return R.layout.device_post_forum_item
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostHolder {
        val v = inflateLayout(parent, layout)
        return PostHolder(v, listener)
    }

    override fun onBindViewHolder(holder: PostHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    class PostHolder(v: View, listener: Listener) : BaseViewHolder<PostItem>(v) {
        private val title: TextView = v.findViewById(R.id.item_title)
        private val date: TextView = v.findViewById(R.id.item_date)
        private val desc: TextView? = v.findViewById(R.id.item_desc)
        private val image: ImageView? = v.findViewById(R.id.item_image)
        private var currentItem: PostItem? = null

        init {
            v.setOnClickListener((View.OnClickListener { v1: View? ->
                listener.onClick(requireNotNull(currentItem))
            }))
        }

        override fun bind(item: PostItem, position: Int) {
            currentItem = item
            title.text = item.title
            date.text = item.date
            if (desc != null) {
                if (item.desc != null) {
                    desc.text = spannedFromHtml(item.desc)
                    desc.visibility = View.VISIBLE
                } else {
                    desc.visibility = View.GONE
                }
            }
            if (image != null && item.image != null) {
                ImageLoader.getInstance().displayImage(item.image, image)
            }
        }

        fun interface Listener {
            fun onClick(item: PostItem)
        }
    }
}
