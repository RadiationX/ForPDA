package forpdateam.ru.forpda.ui.views.messagepanel.advanced.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import com.nostra13.universalimageloader.core.ImageLoader
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.databinding.MessagePanelAdvancedItemBinding
import forpdateam.ru.forpda.extensions.setTintColor
import forpdateam.ru.forpda.ui.views.messagepanel.advanced.PanelListItem
import forpdateam.ru.forpda.ui.views.messagepanel.advanced.adapters.ItemDragCallback.ItemTouchHelperAdapter
import java.util.Collections

/**
 * Created by radiationx on 08.01.17.
 */
class PanelItemAdapter(
    private val items: MutableList<PanelListItem>,
    private val clickListener: (PanelListItem) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), ItemTouchHelperAdapter {

    override fun getItemViewType(position: Int): Int {
        return items[position]::class.hashCode()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.message_panel_advanced_item, parent, false)
        return when (viewType) {
            PanelListItem.Smile::class.hashCode() -> SmileViewHolder(clickListener, view)
            PanelListItem.BBCode::class.hashCode() -> BBCodeViewHolder(clickListener, view)
            PanelListItem.Color::class.hashCode() -> ColorViewHolder(clickListener, view)
            else -> error("Unknown viewtype")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is PanelListItem.Smile -> (holder as SmileViewHolder).bind(item)
            is PanelListItem.BBCode -> (holder as BBCodeViewHolder).bind(item)
            is PanelListItem.Color -> (holder as ColorViewHolder).bind(item)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    class SmileViewHolder(
        private val clickListener: (PanelListItem.Smile) -> Unit,
        view: View
    ) : RecyclerView.ViewHolder(view) {

        private val binding by viewBinding<MessagePanelAdvancedItemBinding>()

        fun bind(item: PanelListItem.Smile) {
            val assetUrl = "assets://smiles/${item.assetFileName}"
            ImageLoader.getInstance().cancelDisplayTask(binding.itemIcon)
            ImageLoader.getInstance().displayImage(assetUrl, binding.itemIcon)
            binding.itemTitle.visibility = View.GONE
            binding.root.contentDescription = item.text
            binding.root.setOnClickListener {
                clickListener.invoke(item)
            }
        }
    }

    class BBCodeViewHolder(
        private val clickListener: (PanelListItem.BBCode) -> Unit,
        view: View
    ) : RecyclerView.ViewHolder(view) {

        private val binding by viewBinding<MessagePanelAdvancedItemBinding>()

        fun bind(item: PanelListItem.BBCode) {
            binding.itemIcon.setImageResource(item.iconRes)
            val title = binding.itemTitle.context.getString(item.titleRes)
            binding.itemTitle.isVisible = true
            binding.itemTitle.text = title
            binding.root.contentDescription = title
            binding.root.setOnClickListener {
                clickListener.invoke(item)
            }
        }
    }

    class ColorViewHolder(
        private val clickListener: (PanelListItem.Color) -> Unit,
        view: View
    ) : RecyclerView.ViewHolder(view) {

        private val binding by viewBinding<MessagePanelAdvancedItemBinding>()

        fun bind(item: PanelListItem.Color) {
            binding.itemIcon.setImageResource(R.drawable.bg_circle_black)
            binding.itemIcon.setTintColor(item.color)
            binding.root.contentDescription = item.hexColor
            binding.itemTitle.isVisible = false
            binding.root.setOnClickListener {
                clickListener.invoke(item)
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
}
