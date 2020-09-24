package forpdateam.ru.forpda.ui.views.messagepanel.attachments

import android.content.Context
import com.google.android.material.bottomsheet.BottomSheetDialog
import androidx.recyclerview.widget.GridLayoutManager
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView

import java.util.ArrayList

import forpdateam.ru.forpda.App
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.entity.remote.editpost.AttachmentItem
import forpdateam.ru.forpda.entity.remote.editpost.EditPostForm
import forpdateam.ru.forpda.model.data.remote.api.RequestFile
import forpdateam.ru.forpda.ui.views.messagepanel.AutoFitRecyclerView
import forpdateam.ru.forpda.ui.views.messagepanel.MessagePanel

/**
 * Created by radiationx on 09.01.17.
 */

class AttachmentsPopup(context: Context, private val messagePanel: MessagePanel) {
    private val dialog: BottomSheetDialog
    private val bottomSheet: View?
    private val recyclerView: AutoFitRecyclerView
    private val adapter = AttachmentAdapter()

    private val noAttachments: TextView
    private val textControls: RelativeLayout
    private val addFile: ImageButton
    private val deleteFile: ImageButton
    private val addToSpoiler: Button
    private val addToText: Button
    private val progressOverlay: FrameLayout

    private var enabledTextControls = true
    private var isLinear = true
    private var isReverse = false


    private val attachments = mutableListOf<AttachmentItem>()
    private val selected = mutableListOf<AttachmentItem>()


    private var insertAttachmentListener: OnInsertAttachmentListener? = null

    fun getAttachments(): List<AttachmentItem> = attachments

    fun getSelected(): List<AttachmentItem> = selected

    init {
        dialog = BottomSheetDialog(context)
        val lp = dialog.window!!.attributes
        lp.dimAmount = 1.0f
        dialog.window!!.attributes = lp
        dialog.window!!.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        //dialog.setPeekHeight(App.getKeyboardHeight());
        //dialog.getWindow().getDecorView().setFitsSystemWindows(true);

        bottomSheet = View.inflate(context, R.layout.message_panel_attachments, null)
        recyclerView = bottomSheet!!.findViewById<View>(R.id.auto_fit_recycler_view) as AutoFitRecyclerView
        progressOverlay = bottomSheet.findViewById<View>(R.id.progress_overlay) as FrameLayout

        noAttachments = bottomSheet.findViewById<View>(R.id.no_attachments_text) as TextView
        textControls = bottomSheet.findViewById<View>(R.id.text_controls) as RelativeLayout
        addFile = bottomSheet.findViewById<View>(R.id.add_file) as ImageButton
        deleteFile = bottomSheet.findViewById<View>(R.id.delete_file) as ImageButton
        addToSpoiler = bottomSheet.findViewById<View>(R.id.add_to_spoiler) as Button
        addToText = bottomSheet.findViewById<View>(R.id.add_to_text) as Button

        recyclerView.setColumnWidth(App.get().dpToPx(112, recyclerView.context))
        adapter.updateIsLinear(isLinear)
        adapter.updateReverse(isReverse)
        recyclerView.setFakeLinear(isLinear)
        recyclerView.adapter = adapter

        recyclerView.manager.spanSizeLookup = object : androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(i: Int): Int {
                return if (isLinear) {
                    1
                } else if (i == 0) {
                    recyclerView.manager.spanCount
                } else {
                    1
                }
            }
        }

        /*addFile.setItemClickListener(v -> {
            uploadFiles();
        });*/
        //deleteFile.setItemClickListener(v -> adapter.deleteSelected());
        adapter.setReloadOnClickListener(object : AttachmentAdapter.OnReloadClickListener {
            override fun onReloadClick(item: AttachmentItem) {

            }
        })

        adapter.setOnItemClickListener(object : AttachmentAdapter.OnItemClickListener {
            override fun onItemClick(item: AttachmentItem) {
                item.toggle()
                if (item.isSelected) {
                    if (!selected.contains(item)) {
                        selected.add(item)
                    }
                } else {
                    selected.remove(item)
                }
                onSelectedChange()
                adapter.updateItem(item)
            }
        })
        adapter.setSelectorListener(object : AttachmentAdapter.SelectorListener {
            override fun onViewTypeChanged(isLinear: Boolean) {
                this@AttachmentsPopup.isLinear = isLinear
                recyclerView.setFakeLinear(isLinear)
                adapter.updateIsLinear(isLinear)
            }

            override fun onReverseClick() {
                isReverse = !isReverse
                adapter.updateReverse(isReverse)
                adapter.clear()
                adapter.add(attachments)
            }
        })
        onDataChange(0)

        addToText.setOnClickListener { v -> insertAttachment(selected, false) }
        addToSpoiler.setOnClickListener { v -> insertAttachment(selected, true) }

        messagePanel.addAttachmentsOnClickListener { v ->
            if (bottomSheet != null && bottomSheet.parent != null && bottomSheet.parent is ViewGroup) {
                (bottomSheet.parent as ViewGroup).removeView(bottomSheet)
            }
            dialog.setContentView(bottomSheet)
            dialog.show()
        }

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window window = dialog.getWindow();
            if (window != null) {
                window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                }
            }
        }*/
    }

    fun setEnabledTextControls(enabled: Boolean) {
        enabledTextControls = enabled
    }

    fun insertAttachment(items: List<AttachmentItem>, toSpoiler: Boolean) {
        val text = StringBuilder()
        if (toSpoiler)
            text.append("[spoiler]")
        for (item in items) {
            if (insertAttachmentListener != null) {
                text.append(insertAttachmentListener!!.onInsert(item))
            } else {
                text.append("[attachment=").append(item.id).append(":").append(item.name).append("]")
            }
        }
        if (toSpoiler)
            text.append("[/spoiler]")
        messagePanel.insertText(text.toString())
        unSelectItems()
        dialog.cancel()
    }

    fun unSelectItems() {
        for (item in selected) {
            if (item.isSelected) item.toggle()
            adapter.updateItem(item)
        }
        selected.clear()
        onSelectedChange()
    }

    fun containNotLoaded(): Boolean {
        for (item in selected) {
            if (item.loadState != AttachmentItem.STATE_LOADED)
                return true
        }
        return false
    }


    fun deleteSelected() {
        for (item in selected) {
            if (item.status == AttachmentItem.STATUS_REMOVED) {
                attachments.remove(item)
                adapter.removeItem(item)
                updateDataCounter()
            }
        }
        unSelectItems()
    }


    private fun onDataChange(count: Int) {
        messagePanel.updateAttachmentsCounter(count)
        if (count > 0) {
            noAttachments.text = String.format(App.get().getString(R.string.attachments_count), count)
            //dialog.setPeekHeight(App.getKeyboardHeight());
        } else {
            noAttachments.setText(R.string.no_attachments)
            //dialog.setPeekHeight(App.px48);
        }
    }

    private fun updateDataCounter() {
        onDataChange(attachments.size)
    }

    private fun onSelectedChange() {
        val firstGroup = if (selected.size > 0) View.GONE else View.VISIBLE
        val secondGroup = if (selected.size > 0) View.VISIBLE else View.GONE

        if (!enabledTextControls) {
            noAttachments.visibility = View.VISIBLE
        } else if (noAttachments.visibility != firstGroup) {
            noAttachments.visibility = firstGroup
        }
        if (addFile.visibility != firstGroup)
            addFile.visibility = firstGroup
        if (!enabledTextControls) {
            textControls.visibility = View.GONE
        } else if (textControls.visibility != secondGroup) {
            textControls.visibility = secondGroup
        }
        if (deleteFile.visibility != secondGroup)
            deleteFile.visibility = secondGroup

        tryLockControls(!containNotLoaded())
    }

    private fun tryLockControls(enable: Boolean) {
        if (textControls.visibility == View.VISIBLE) {
            addToSpoiler.isEnabled = enable
            addToText.isEnabled = enable
            deleteFile.isEnabled = enable
        }
    }


    fun setAddOnClickListener(listener: () -> Unit) {
        addFile.setOnClickListener { listener.invoke() }
    }

    fun setDeleteOnClickListener(listener: () -> Unit) {
        deleteFile.setOnClickListener { listener.invoke() }
    }

    fun onLoadAttachments(form: EditPostForm) {
        attachments.addAll(form.attachments)
        adapter.add(form.attachments)
        updateDataCounter()
    }

    fun preUploadFiles(files: List<RequestFile>): List<AttachmentItem> {
        Log.d(LOG_TAG, "preUploadFiles $files")
        val loadingItems = ArrayList<AttachmentItem>()
        for (file in files) {
            val item = AttachmentItem(file.fileName)
            item.setProgressListener { percent ->

            }
            Log.d(LOG_TAG, "Add loading item $item")
            attachments.add(item)
            adapter.add(item)
            loadingItems.add(item)
        }
        return loadingItems
    }

    fun onUploadFiles(items: List<AttachmentItem>) {
        Log.d(LOG_TAG, "onUploadFiles $items")
        for (item in items) {
            Log.d(LOG_TAG, "Loading item $item")
            if (item.loadState == AttachmentItem.STATE_NOT_LOADED) {
                attachments.remove(item)
                selected.remove(item)
                adapter.removeItem(item)
                //SHOW ERROR
            } else {
                adapter.updateItem(item)
            }
        }
        updateDataCounter()
        onSelectedChange()
    }

    fun preDeleteFiles() {
        //block ui
        progressOverlay.visibility = View.VISIBLE
        tryLockControls(false)
    }

    fun setAttachments(items: List<AttachmentItem>) {
        clearAttachments()
        attachments.addAll(items)
        adapter.add(items)
        updateDataCounter()
    }

    fun clearAttachments() {
        attachments.clear()
        selected.clear()
        adapter.clear()
        updateDataCounter()
        onSelectedChange()
    }


    fun onDeleteFiles(deletedItems: List<AttachmentItem>) {
        //unblock ui
        Log.d(LOG_TAG, "onDeleteFiles $deletedItems")
        for (item in deletedItems) {
            Log.d(LOG_TAG, "Delete file $item")
            messagePanel.setText(messagePanel.message.replace(("\\[attachment=['\"]?" + item.id + ":[^\\]]*?]").toRegex(), ""))
        }
        progressOverlay.visibility = View.GONE
        deleteSelected()
    }

    fun setInsertAttachmentListener(insertAttachmentListener: OnInsertAttachmentListener) {
        this.insertAttachmentListener = insertAttachmentListener
    }

    interface OnInsertAttachmentListener {
        fun onInsert(item: AttachmentItem): String
    }

    companion object {
        private val LOG_TAG = AttachmentsPopup::class.java.simpleName
    }
}
