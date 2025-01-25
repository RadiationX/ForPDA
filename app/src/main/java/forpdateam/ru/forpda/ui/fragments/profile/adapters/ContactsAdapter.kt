package forpdateam.ru.forpda.ui.fragments.profile.adapters

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import forpdateam.ru.forpda.App.Companion.getVecDrawable
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.entity.remote.profile.ProfileModel.Contact
import forpdateam.ru.forpda.model.repository.temp.TempHelper.getContactIcon
import forpdateam.ru.forpda.ui.views.adapters.BaseAdapter
import forpdateam.ru.forpda.ui.views.adapters.BaseViewHolder

/**
 * Created by radiationx on 15.09.17.
 */
internal class ContactsAdapter(
    private val listener: InfoHolder.Listener
) : BaseAdapter<Contact, ContactsAdapter.InfoHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InfoHolder {
        return InfoHolder(
            inflateLayout(parent, R.layout.profile_sub_item_contact),
            listener
        )
    }

    override fun onBindViewHolder(holder: InfoHolder, position: Int) {
        holder.bind(getItem(position))
    }

    internal class InfoHolder(
        itemView: View,
        listener: Listener
    ) : BaseViewHolder<Contact>(itemView) {
        private val icon: ImageView = itemView.findViewById(R.id.item_icon)
        private var currentItem: Contact? = null

        init {
            itemView.setOnClickListener { v: View? -> listener.onClick(currentItem) }
        }

        override fun bind(item: Contact) {
            currentItem = item
            icon.setImageDrawable(getVecDrawable(icon.context, getContactIcon(item.type)))
            icon.contentDescription = item.title
        }

        internal interface Listener {
            fun onClick(item: Contact?)
        }
    }
}