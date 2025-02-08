package forpdateam.ru.forpda.ui.fragments.qms.adapters

import android.graphics.Typeface
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.nostra13.universalimageloader.core.ImageLoader
import by.kirich1409.viewbindingdelegate.viewBinding
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.databinding.QmsContactItemBinding
import forpdateam.ru.forpda.entity.remote.qms.QmsContact
import forpdateam.ru.forpda.ui.fragments.qms.adapters.QmsContactsAdapter.ContactHolder
import forpdateam.ru.forpda.ui.views.adapters.BaseAdapter
import forpdateam.ru.forpda.ui.views.adapters.BaseViewHolder

/**
 * Created by radiationx on 25.08.16.
 */
class QmsContactsAdapter : BaseAdapter<QmsContact, ContactHolder>() {
    private var itemClickListener: OnItemClickListener<QmsContact>? = null

    fun setOnItemClickListener(mItemClickListener: OnItemClickListener<QmsContact>?) {
        this.itemClickListener = mItemClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactHolder {
        val v = inflateLayout(parent, R.layout.qms_contact_item)
        return ContactHolder(v)
    }

    override fun onBindViewHolder(holder: ContactHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    inner class ContactHolder(v: View) : BaseViewHolder<QmsContact>(v), View.OnClickListener,
        OnLongClickListener {
        private val binding by viewBinding<QmsContactItemBinding>()

        init {
            v.setOnClickListener(this)
            v.setOnLongClickListener(this)
        }

        override fun bind(item: QmsContact, position: Int) {
            binding.qmsContactNick.text = item.user.nick
            ImageLoader.getInstance().displayImage(item.user.avatar, binding.qmsContactAvatar)
            binding.qmsContactNick.typeface = if (item.count > 0) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
            if (item.count == 0) {
                binding.qmsContactCount.visibility = View.GONE
            } else {
                binding.qmsContactCount.text = item.count.toString()
                binding.qmsContactCount.visibility = View.VISIBLE
            }
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
