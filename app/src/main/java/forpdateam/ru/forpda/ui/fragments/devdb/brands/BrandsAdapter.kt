package forpdateam.ru.forpda.ui.fragments.devdb.brands

import com.hannesdorfmann.adapterdelegates4.ListDelegationAdapter
import forpdateam.ru.forpda.entity.remote.devdb.Brands
import forpdateam.ru.forpda.ui.views.adapters.OnItemClickListener
import forpdateam.ru.forpda.ui.views.adapters.SectionItemDelegate
import forpdateam.ru.forpda.ui.views.adapters.buildSections
import forpdateam.ru.forpda.ui.views.drawers.adapters.BrandListItem
import forpdateam.ru.forpda.ui.views.drawers.adapters.ListItem

class BrandsAdapter(
    private val brandClickListener: OnItemClickListener<Brands.Item>
) : ListDelegationAdapter<List<ListItem>>() {

    init {
        delegatesManager.apply {
            addDelegate(SectionItemDelegate())
            addDelegate(BrandDelegate(brandClickListener))
        }
    }

    fun bindItems(data: Brands) {
        this.items = buildSections {
            for ((key, value) in data.letterMap) {
                addSection(key, value) {
                    BrandListItem(it)
                }
            }
        }
        notifyDataSetChanged()
    }
}