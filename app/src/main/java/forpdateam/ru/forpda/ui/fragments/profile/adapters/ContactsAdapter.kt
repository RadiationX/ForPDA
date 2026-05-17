package forpdateam.ru.forpda.ui.fragments.profile.adapters

import android.view.View
import android.view.ViewGroup
import by.kirich1409.viewbindingdelegate.viewBinding
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.databinding.ProfileSubItemContactBinding
import forpdateam.ru.forpda.entity.remote.profile.ProfileModel.Contact
import forpdateam.ru.forpda.model.repository.temp.TempHelper
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
        private val binding by viewBinding<ProfileSubItemContactBinding>()
        private var currentItem: Contact? = null

        init {
            itemView.setOnClickListener { v: View? -> listener.onClick(currentItem) }
        }

        override fun bind(item: Contact) {
            currentItem = item
            binding.itemIcon.setImageResource(TempHelper.getContactIcon(item.type))
            binding.itemIcon.contentDescription = item.title
        }

        internal interface Listener {
            fun onClick(item: Contact?)
        }
    }
}