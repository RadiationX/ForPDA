package forpdateam.ru.forpda.ui.fragments.devdb.device.adapter

import com.hannesdorfmann.adapterdelegates4.ListDelegationAdapter
import forpdateam.ru.forpda.entity.remote.devdb.Device
import forpdateam.ru.forpda.ui.views.drawers.adapters.DeviceArticleListItem
import forpdateam.ru.forpda.ui.views.drawers.adapters.DeviceCommentListItem
import forpdateam.ru.forpda.ui.views.drawers.adapters.DeviceSpecsListItem
import forpdateam.ru.forpda.ui.views.drawers.adapters.DeviceTopicListItem
import forpdateam.ru.forpda.ui.views.drawers.adapters.ListItem

class SubDeviceAdapter(
    private val articleClickListener: (Device.Article) -> Unit,
    private val topicClickListener: (Device.Topic) -> Unit,
    private val commentClickListener: (Device.Comment) -> Unit,
) : ListDelegationAdapter<List<ListItem>>() {

    init {
        delegatesManager.apply {
            addDelegate(DeviceSpecsDelegate())
            addDelegate(DeviceArticleDelegate(articleClickListener))
            addDelegate(DeviceTopicDelegate(topicClickListener))
            addDelegate(DeviceCommentDelegate(commentClickListener))
        }
    }

    fun bindSpecs(items: List<Device.Specs>) {
        this.items = items.map { DeviceSpecsListItem(it) }
        notifyDataSetChanged()
    }

    fun bindArticles(items: List<Device.Article>) {
        this.items = items.map { DeviceArticleListItem(it) }
        notifyDataSetChanged()
    }

    fun bindTopics(items: List<Device.Topic>) {
        this.items = items.map { DeviceTopicListItem(it) }
        notifyDataSetChanged()
    }

    fun bindComments(items: List<Device.Comment>) {
        this.items = items.map { DeviceCommentListItem(it) }
        notifyDataSetChanged()
    }
}