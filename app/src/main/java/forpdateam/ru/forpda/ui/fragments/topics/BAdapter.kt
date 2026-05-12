package forpdateam.ru.forpda.ui.fragments.topics

import com.hannesdorfmann.adapterdelegates4.ListDelegationAdapter
import forpdateam.ru.forpda.entity.app.CloseableInfo
import forpdateam.ru.forpda.entity.app.other.AppMenuItem
import forpdateam.ru.forpda.entity.remote.devdb.Brands
import forpdateam.ru.forpda.entity.remote.favorites.FavItem
import forpdateam.ru.forpda.entity.remote.others.user.ForumUser
import forpdateam.ru.forpda.entity.remote.topics.TopicItem
import forpdateam.ru.forpda.model.MenuMapper
import forpdateam.ru.forpda.model.interactors.other.MenuRepository
import forpdateam.ru.forpda.ui.fragments.devdb.brands.BrandDelegate
import forpdateam.ru.forpda.ui.fragments.favorites.FavoriteDelegate
import forpdateam.ru.forpda.ui.views.adapters.OnItemClickListener
import forpdateam.ru.forpda.ui.views.adapters.SectionItemDelegate
import forpdateam.ru.forpda.ui.views.adapters.buildSections
import forpdateam.ru.forpda.ui.views.drawers.adapters.CloseableInfoListItem
import forpdateam.ru.forpda.ui.views.drawers.adapters.DividerShadowListItem
import forpdateam.ru.forpda.ui.views.drawers.adapters.DrawerMenuItem
import forpdateam.ru.forpda.ui.views.drawers.adapters.ListItem
import forpdateam.ru.forpda.ui.views.drawers.adapters.MenuListItem
import forpdateam.ru.forpda.ui.views.drawers.adapters.ProfileListItem
import java.util.Collections

class BAdapter(
    private val topicClickListener: OnItemClickListener<TopicItem>,
    private val favoriteClickListener: OnItemClickListener<FavItem>,
    private val brandClickListener: OnItemClickListener<Brands.Item>
) : ListDelegationAdapter<List<ListItem>>() {


    init {
        delegatesManager.apply {
            addDelegate(SectionItemDelegate())
            addDelegate(TopicAnnounceDelegate(topicClickListener))
            addDelegate(TopicForumDelegate(topicClickListener))
            addDelegate(TopicDelegate(topicClickListener))
            addDelegate(FavoriteDelegate(favoriteClickListener))
            addDelegate(BrandDelegate(brandClickListener))
        }
    }

    fun bindItems() {
        this.items = buildSections {

        }
        notifyDataSetChanged()
    }
}