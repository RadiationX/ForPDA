package forpdateam.ru.forpda.ui.fragments.qms.adapters

import android.graphics.Typeface
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.nostra13.universalimageloader.core.ImageLoader
import forpdateam.ru.forpda.R
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
        var avatar: ImageView =
            v.findViewById(R.id.qms_contact_avatar)
        var nick: TextView = v.findViewById(R.id.qms_contact_nick)
        var count: TextView = v.findViewById(R.id.qms_contact_count)

        init {
            v.setOnClickListener(this)
            v.setOnLongClickListener(this)
        }

        override fun bind(item: QmsContact, position: Int) {
            nick.text = item.nick
            ImageLoader.getInstance().displayImage(item.avatar, avatar)
            nick.typeface = if (item.count > 0) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
            if (item.count == 0) {
                count.visibility = View.GONE
            } else {
                count.text = item.count.toString()
                count.visibility = View.VISIBLE
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
