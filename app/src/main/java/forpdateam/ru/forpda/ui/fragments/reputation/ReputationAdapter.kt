package forpdateam.ru.forpda.ui.fragments.reputation

import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import com.nostra13.universalimageloader.core.ImageLoader
import by.kirich1409.viewbindingdelegate.viewBinding
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.databinding.ReputationItemBinding
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

        private val binding by viewBinding<ReputationItemBinding>()

        init {
            v.setOnClickListener(this)
            v.setOnLongClickListener(this)
        }

        override fun bind(item: RepItem, position: Int) {
            binding.repItemTitle.text = item.title
            binding.repItemLastNick.text = item.user.nick
            binding.repItemDate.text = item.date
            if (item.sourceUrl == null) {
                binding.repItemDesc.visibility = View.GONE
            } else {
                binding.repItemDesc.visibility = View.VISIBLE
                binding.repItemDesc.text = item.sourceTitle
            }
            ImageLoader.getInstance().displayImage(item.image, binding.repItemImage)
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
