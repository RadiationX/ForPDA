package forpdateam.ru.forpda.ui.fragments.devdb.brands

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.hannesdorfmann.adapterdelegates4.AbsListItemAdapterDelegate
import forpdateam.ru.forpda.databinding.BrandsItemBinding
import forpdateam.ru.forpda.entity.remote.devdb.Brands
import forpdateam.ru.forpda.ui.views.adapters.OnItemClickListener
import forpdateam.ru.forpda.ui.views.drawers.adapters.BrandListItem
import forpdateam.ru.forpda.ui.views.drawers.adapters.ListItem

class BrandDelegate(
    private val clickListener: OnItemClickListener<Brands.Item>,
) : AbsListItemAdapterDelegate<BrandListItem, ListItem, BrandDelegate.ViewHolder>() {

    override fun isForViewType(item: ListItem, items: MutableList<ListItem>, position: Int): Boolean {
        return item is BrandListItem
    }

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder = ViewHolder(
        binding = BrandsItemBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        clickListener = clickListener,
    )

    override fun onBindViewHolder(item: BrandListItem, holder: ViewHolder, payloads: List<Any?>) {
        holder.bind(item)
    }

    class ViewHolder(
        private val binding: BrandsItemBinding,
        private val clickListener: OnItemClickListener<Brands.Item>
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(listItem: BrandListItem) {
            val item = listItem.item
            binding.itemTitle.text = item.title
            binding.itemCount.text = item.count.toString()
            clickListener.attachTo(binding.root, listItem.item)
        }
    }
}
