package forpdateam.ru.forpda.ui.views.messagepanel.attachments

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Handler
import android.os.Message
import android.support.design.widget.TabLayout
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView

import com.github.rahatarmanahmed.cpv.CircularProgressView
import com.nostra13.universalimageloader.core.ImageLoader

import java.util.ArrayList

import forpdateam.ru.forpda.App
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.entity.remote.editpost.AttachmentItem
import forpdateam.ru.forpda.model.data.remote.IWebClient
import forpdateam.ru.forpda.ui.views.drawers.adapters.AttachmentListItem
import forpdateam.ru.forpda.ui.views.drawers.adapters.AttachmentSelectorListItem
import forpdateam.ru.forpda.ui.views.drawers.adapters.ListItem


/**
 * Created by radiationx on 09.01.17.
 */

class AttachmentAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val items = ArrayList<ListItem>()
    //private val selected = ArrayList<AttachmentItem>()
    private var itemClickListener: AttachmentAdapter.OnItemClickListener? = null
    private var reloadOnClickListener: OnReloadClickListener? = null
    private var selectorListener: SelectorListener? = null
    private var isLinear = false
    private var isReverse = false

    companion object {
        private const val TYPE_SELECTOR = 1
        private const val TYPE_ITEM = 2
        private const val TYPE_ITEM_HORIZONTAL = 3
    }

    init {
        clear()
    }

    fun updateIsLinear(isLinear: Boolean) {
        this.isLinear = isLinear
        val index = items.indexOfFirst { it is AttachmentSelectorListItem }
        if (index != -1) {
            (items[index] as AttachmentSelectorListItem).isLinear = isLinear
            notifyItemChanged(index)
        }
    }

    fun updateReverse(isReverse: Boolean) {
        this.isReverse = isReverse
        val index = items.indexOfFirst { it is AttachmentSelectorListItem }
        if (index != -1) {
            (items[index] as AttachmentSelectorListItem).isReverse = isReverse
            notifyItemChanged(index)
        }
    }

    fun updateItem(item: AttachmentItem) {
        val index = items.indexOfFirst { (it as? AttachmentListItem)?.item == item }
        if (index != -1) {
            notifyItemChanged(index)
        }
    }

    fun add(newItems: List<AttachmentItem>) {
        val finalItems = if (isReverse) {
            newItems.asReversed()
        } else {
            newItems
        }
        val insertIndex = if (isReverse) {
            1
        } else {
            this.items.size
        }
        this.items.addAll(insertIndex, finalItems.map { AttachmentListItem(it) })
        notifyItemRangeInserted(insertIndex, finalItems.size)
    }

    fun add(item: AttachmentItem) {
        add(listOf(item))
    }

    fun clear() {
        items.clear()
        items.add(AttachmentSelectorListItem(isLinear, isReverse))
        notifyDataSetChanged()
    }

    fun removeItem(item: AttachmentItem) {
        val index = items.indexOfFirst { (it as? AttachmentListItem)?.item == item }
        if (index != -1) {
            items.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val item = items[position]
        return when (item) {
            is AttachmentListItem -> if (isLinear) TYPE_ITEM_HORIZONTAL else TYPE_ITEM
            is AttachmentSelectorListItem -> TYPE_SELECTOR
            else -> -1
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutRes = when (viewType) {
            TYPE_ITEM -> R.layout.message_panel_attachment_item
            TYPE_ITEM_HORIZONTAL -> R.layout.message_panel_attachment_item_horizontal
            TYPE_SELECTOR -> R.layout.message_panel_attachments_selector
            else -> -1
        }
        val view = LayoutInflater.from(parent.context).inflate(layoutRes, parent, false)
        return when (viewType) {
            TYPE_ITEM, TYPE_ITEM_HORIZONTAL -> ViewHolder(view)
            TYPE_SELECTOR -> SelectorHolder(view)
            else -> throw NullPointerException()
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        val viewType = getItemViewType(position)
        when (viewType) {
            TYPE_ITEM, TYPE_ITEM_HORIZONTAL -> {
                (holder as ViewHolder).bind((item as AttachmentListItem).item)
            }
            TYPE_SELECTOR -> {
                val selectorItem = (item as AttachmentSelectorListItem)
                (holder as SelectorHolder).bind(selectorItem.isLinear, selectorItem.isReverse)
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun setOnItemClickListener(mItemClickListener: AttachmentAdapter.OnItemClickListener) {
        this.itemClickListener = mItemClickListener
    }

    fun setReloadOnClickListener(reloadOnClickListener: OnReloadClickListener) {
        this.reloadOnClickListener = reloadOnClickListener
    }

    fun setSelectorListener(selectorListener: SelectorListener) {
        this.selectorListener = selectorListener
    }

    interface OnItemClickListener {
        fun onItemClick(item: AttachmentItem)
    }

    interface OnReloadClickListener {
        fun onReloadClick(item: AttachmentItem)
    }

    interface SelectorListener {
        fun onViewTypeChanged(isLinear: Boolean)
        fun onReverseClick()
    }

    inner class SelectorHolder(view: View) : RecyclerView.ViewHolder(view) {
        private var tabLayout: TabLayout = view.findViewById(R.id.selectorTabLayout)
        private var reverseBtn = view.findViewById<ImageView>(R.id.selectorReverse)
        private var gridTab: TabLayout.Tab
        private var listTab: TabLayout.Tab
        private var listener: TabLayout.OnTabSelectedListener

        init {
            gridTab = tabLayout.newTab().setIcon(ContextCompat.getDrawable(tabLayout.context, R.drawable.ic_grid)).also {
                tabLayout.addTab(it)
            }
            listTab = tabLayout.newTab().setIcon(ContextCompat.getDrawable(tabLayout.context, R.drawable.ic_view_list)).also {
                tabLayout.addTab(it)
            }
            listener = object : TabLayout.OnTabSelectedListener {
                override fun onTabReselected(p0: TabLayout.Tab?) {}
                override fun onTabUnselected(p0: TabLayout.Tab?) {}

                override fun onTabSelected(p0: TabLayout.Tab?) {
                    selectorListener?.onViewTypeChanged(p0 == listTab)
                }
            }
            reverseBtn.setOnClickListener { selectorListener?.onReverseClick() }
        }

        fun bind(isLinear: Boolean, isReverse: Boolean) {
            tabLayout.removeOnTabSelectedListener(listener)
            if (!isLinear) {
                gridTab.select()
            } else {
                listTab.select()
            }
            tabLayout.addOnTabSelectedListener(listener)
        }
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        private var imageView: ImageView
        private var radioButton: RadioButton
        private var overlay: View
        private var progressBar: CircularProgressView
        private var progressValue: TextView
        private var reload: ImageButton
        private var name: TextView
        private var attributes: TextView
        private var description: View
        private val handler: Handler = @SuppressLint("HandlerLeak")
        object : Handler() {
            override fun handleMessage(msg: Message) {
                updateProgress(msg.obj as Int)
            }
        }
        private var progressListener = IWebClient.ProgressListener { percent ->
            handler.sendMessage(handler.obtainMessage().apply {
                obj = percent
            })
        }

        init {
            view.setOnClickListener(this)
            imageView = view.findViewById<View>(R.id.drawer_item_icon) as ImageView
            radioButton = view.findViewById<View>(R.id.radio_button) as RadioButton
            overlay = view.findViewById(R.id.overlay_and_text)
            progressBar = view.findViewById<View>(R.id.progress_bar) as CircularProgressView
            progressValue = view.findViewById(R.id.progress_value)
            reload = view.findViewById<View>(R.id.reload) as ImageButton
            name = view.findViewById<View>(R.id.file_name) as TextView
            attributes = view.findViewById<View>(R.id.file_attributes) as TextView
            description = view.findViewById(R.id.file_description) as View

            reload.setOnClickListener { v ->
                val item = (items[layoutPosition] as AttachmentListItem).item
                reloadOnClickListener?.onReloadClick(item)
            }
        }

        @SuppressLint("SetTextI18n")
        fun bind(item: AttachmentItem) {
            when (item.loadState) {
                AttachmentItem.STATE_LOADING -> {
                    description.visibility = View.GONE
                    progressBar.visibility = View.VISIBLE
                    progressValue.visibility = View.VISIBLE
                    reload.visibility = View.GONE
                    imageView.visibility = View.GONE
                    updateProgress(item.progress)
                    item.progressListener = progressListener
                }
                AttachmentItem.STATE_NOT_LOADED -> {
                    description.visibility = View.GONE
                    progressBar.visibility = View.GONE
                    progressValue.visibility = View.GONE
                    reload.visibility = View.VISIBLE
                    imageView.visibility = View.GONE
                }
                AttachmentItem.STATE_LOADED -> {
                    description.visibility = View.VISIBLE
                    name.setText(item.getName())
                    attributes.text = "${item.extension}, ${item.weight}"
                    progressBar.visibility = View.GONE
                    progressValue.visibility = View.GONE
                    reload.visibility = View.GONE
                    imageView.visibility = View.VISIBLE
                    if (item.typeFile == AttachmentItem.TYPE_IMAGE) {
                        ImageLoader.getInstance().displayImage(item.imageUrl, imageView)
                    } else {
                        imageView.setImageDrawable(App.getVecDrawable(itemView.context, R.drawable.ic_insert_drive_file_gray_24dp))
                    }
                }
            }
            updateChecked(item)
        }

        @SuppressLint("SetTextI18n")
        private fun updateProgress(progress: Int) {
            if (progressBar.isIndeterminate) {
                progressBar.isIndeterminate = false
            }
            progressBar.progress = progress.toFloat()
            progressValue.text = "$progress%"
        }

        override fun onClick(v: View) {
            val item = (items[layoutPosition] as AttachmentListItem).item
            itemClickListener?.onItemClick(item)
        }

        private fun updateChecked(item: AttachmentItem) {
            radioButton.isChecked = item.isSelected()
            if (item.loadState == AttachmentItem.STATE_NOT_LOADED) {
                overlay.visibility = View.VISIBLE
                overlay.setBackgroundColor(Color.argb(if (item.isSelected()) 96 else 48, 255, 0, 0))
            } else {
                overlay.setBackgroundColor(Color.argb(48, 0, 0, 0))
                overlay.visibility = if (item.isSelected()) View.VISIBLE else View.GONE
            }
        }
    }
}
