package forpdateam.ru.forpda.ui.views.messagepanel.advanced.adapters

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener
import by.kirich1409.viewbindingdelegate.viewBinding
import forpdateam.ru.forpda.App.Companion.getVecDrawable
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.databinding.MessagePanelAdvancedItemBinding
import forpdateam.ru.forpda.ui.views.messagepanel.advanced.ButtonData
import forpdateam.ru.forpda.ui.views.messagepanel.advanced.adapters.ItemDragCallback.ItemTouchHelperAdapter
import java.util.Collections

/**
 * Created by radiationx on 08.01.17.
 */
class PanelItemAdapter(
    private val items: MutableList<ButtonData>,
    private val urlsToAssets: List<String>?,
    private val type: Int
) : RecyclerView.Adapter<PanelItemAdapter.ViewHolder>(), ItemTouchHelperAdapter {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.message_panel_advanced_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position], urlsToAssets?.getOrNull(position), type)
    }

    private var itemClickListener: OnItemClickListener? = null

    fun interface OnItemClickListener {
        fun onItemClick(item: ButtonData)
    }

    fun setOnItemClickListener(mItemClickListener: OnItemClickListener?) {
        this.itemClickListener = mItemClickListener
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {

        private val binding by viewBinding<MessagePanelAdvancedItemBinding>()

        init {
            view.setOnClickListener(this)
        }

        fun bind(item: ButtonData, urlToAsset: String?, type: Int){
            if (type == TYPE_ASSET) {
                ImageLoader.getInstance()
                    .loadImage(urlToAsset!!, object : SimpleImageLoadingListener() {
                        override fun onLoadingComplete(
                            imageUri: String,
                            view: View,
                            loadedImage: Bitmap
                        ) {
                            binding.itemIcon.setImageBitmap(loadedImage)
                        }
                    })
            } else if (type == TYPE_DRAWABLE) {
                binding.itemIcon.setImageDrawable(getVecDrawable(binding.root.context, item.iconRes))
                //holder.button.setColorFilter(colorFilter);
            }
            if (item.title == null) {
                binding.itemTitle.visibility = View.GONE
                binding.root.contentDescription = item.text
            } else {
                binding.root.contentDescription = item.title
                binding.itemTitle.text = item.title
                binding.itemTitle.visibility = View.VISIBLE
            }
        }

        override fun onClick(v: View) {
            val item = items[layoutPosition]
            if (item.listener != null) {
                item.listener!!.onClick(item)
            } else if (itemClickListener != null) {
                itemClickListener!!.onItemClick(item)
            }
        }
    }

    override fun onItemDismiss(position: Int) {
        items.removeAt(position)
        notifyItemRemoved(position)
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int) {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(items, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(items, i, i - 1)
            }
        }
        notifyItemMoved(fromPosition, toPosition)
        //Log.d("FORPDA_LOG", "onItemMove");
    }

    companion object {
        const val TYPE_ASSET: Int = 0
        const val TYPE_DRAWABLE: Int = 1
    }
}
