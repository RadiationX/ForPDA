package forpdateam.ru.forpda.ui.fragments.devdb.brand

import android.graphics.Bitmap
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.assist.FailReason
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener
import forpdateam.ru.forpda.App.Companion.getDrawableAttr
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.entity.remote.devdb.Brand.DeviceItem
import forpdateam.ru.forpda.ui.fragments.devdb.DevDbHelper.getColorFilter
import forpdateam.ru.forpda.ui.fragments.devdb.brand.DevicesAdapter.DeviceItemHolder
import forpdateam.ru.forpda.ui.views.adapters.BaseAdapter
import forpdateam.ru.forpda.ui.views.adapters.BaseViewHolder

/**
 * Created by radiationx on 08.08.17.
 */
class DevicesAdapter : BaseAdapter<DeviceItem, DeviceItemHolder>() {

    private var itemClickListener: OnItemClickListener<DeviceItem>? = null

    fun setItemClickListener(itemClickListener: OnItemClickListener<DeviceItem>?) {
        this.itemClickListener = itemClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceItemHolder {
        val v = inflateLayout(parent, R.layout.brand_item)
        return DeviceItemHolder(v)
    }

    override fun onBindViewHolder(holder: DeviceItemHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    inner class DeviceItemHolder(v: View) : BaseViewHolder<DeviceItem>(v),
        View.OnClickListener, OnLongClickListener {
        var title: TextView = v.findViewById(R.id.item_title)
        var rating: TextView = v.findViewById(R.id.item_rating)
        var image: ImageView =
            v.findViewById(R.id.item_image)
        var progressBar: ProgressBar =
            v.findViewById(R.id.progress_bar)

        init {
            image.tag = progressBar
            rating.background =
                getDrawableAttr(rating.context, R.attr.count_background)
            v.setOnClickListener(this)
            v.setOnLongClickListener(this)
        }

        override fun bind(item: DeviceItem, position: Int) {
            title.text = item.title
            if (item.rating > 0) {
                rating.text = item.rating.toString()
                rating.background.colorFilter = getColorFilter(item.rating)
                rating.visibility = View.VISIBLE
            } else {
                rating.visibility = View.GONE
            }
            ImageLoader.getInstance()
                .displayImage(item.imageSrc, image, object : SimpleImageLoadingListener() {
                    override fun onLoadingStarted(imageUri: String, view: View) {
                        val progressBar = view.tag as ProgressBar
                        progressBar.visibility = View.VISIBLE
                    }

                    override fun onLoadingCancelled(imageUri: String, view: View) {
                        val progressBar = view.tag as ProgressBar
                        progressBar.visibility = View.GONE
                    }

                    override fun onLoadingComplete(
                        imageUri: String,
                        view: View,
                        loadedImage: Bitmap
                    ) {
                        val progressBar = view.tag as ProgressBar
                        progressBar.visibility = View.GONE
                    }

                    override fun onLoadingFailed(
                        imageUri: String,
                        view: View,
                        failReason: FailReason
                    ) {
                        val progressBar = view.tag as ProgressBar
                        progressBar.visibility = View.GONE
                    }
                })
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
