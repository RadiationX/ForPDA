package forpdateam.ru.forpda.ui.fragments.reputation

import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.nostra13.universalimageloader.core.ImageLoader
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.entity.remote.reputation.RepItem
import forpdateam.ru.forpda.ui.fragments.reputation.ReputationAdapter.ReputationHolder
import forpdateam.ru.forpda.ui.views.adapters.BaseAdapter
import forpdateam.ru.forpda.ui.views.adapters.BaseViewHolder

/**
 * Created by radiationx on 20.03.17.
 */
class ReputationAdapter :
    BaseAdapter<RepItem, ReputationHolder>() {
    private var itemClickListener: OnItemClickListener<RepItem>? = null

    fun setOnItemClickListener(mItemClickListener: OnItemClickListener<RepItem>?) {
        this.itemClickListener = mItemClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReputationHolder {
        val v = inflateLayout(parent, R.layout.reputation_item)
        return ReputationHolder(v)
    }

    override fun onBindViewHolder(holder: ReputationHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    inner class ReputationHolder(v: View) : BaseViewHolder<RepItem>(v), View.OnClickListener,
        OnLongClickListener {
        var title: TextView = v.findViewById(R.id.rep_item_title)
        var lastNick: TextView =
            v.findViewById(R.id.rep_item_last_nick)
        var date: TextView = v.findViewById(R.id.rep_item_date)
        var desc: TextView = v.findViewById(R.id.rep_item_desc)
        var image: ImageView =
            v.findViewById(R.id.rep_item_image)

        init {
            v.setOnClickListener(this)
            v.setOnLongClickListener(this)
        }

        override fun bind(item: RepItem, position: Int) {
            title.text = item.title
            lastNick.text = item.userNick
            date.text = item.date
            if (item.sourceUrl == null) {
                desc.visibility = View.GONE
            } else {
                desc.visibility = View.VISIBLE
                desc.text = item.sourceTitle
            }
            ImageLoader.getInstance().displayImage(item.image, image)
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
