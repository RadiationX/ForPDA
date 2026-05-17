package forpdateam.ru.forpda.ui.fragments.devdb.brand

import android.graphics.Bitmap
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import by.kirich1409.viewbindingdelegate.viewBinding
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.assist.FailReason
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.databinding.BrandItemBinding
import forpdateam.ru.forpda.entity.remote.devdb.Brand.DeviceItem
import forpdateam.ru.forpda.extensions.setBackgroundAttr
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

        private val binding by viewBinding<BrandItemBinding>()

        init {
            binding.itemRating.setBackgroundAttr(R.attr.count_background)
            v.setOnClickListener(this)
            v.setOnLongClickListener(this)
        }

        override fun bind(item: DeviceItem, position: Int) {
            binding.itemTitle.text = item.title
            if (item.rating > 0) {
                binding.itemRating.text = item.rating.toString()
                binding.itemRating.background.colorFilter = getColorFilter(item.rating)
                binding.itemRating.visibility = View.VISIBLE
            } else {
                binding.itemRating.visibility = View.GONE
            }
            ImageLoader.getInstance()
                .displayImage(item.imageSrc, binding.itemImage, object : SimpleImageLoadingListener() {
                    override fun onLoadingStarted(imageUri: String, view: View) {
                        binding.progressBar.visibility = View.VISIBLE
                    }

                    override fun onLoadingCancelled(imageUri: String, view: View) {
                        binding.progressBar.visibility = View.GONE
                    }

                    override fun onLoadingComplete(
                        imageUri: String,
                        view: View,
                        loadedImage: Bitmap
                    ) {
                        binding.progressBar.visibility = View.GONE
                    }

                    override fun onLoadingFailed(
                        imageUri: String,
                        view: View,
                        failReason: FailReason
                    ) {
                        binding.progressBar.visibility = View.GONE
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
