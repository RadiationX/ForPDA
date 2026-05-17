package forpdateam.ru.forpda.ui.views.messagepanel

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.common.simple.SimpleTextWatcher
import forpdateam.ru.forpda.entity.remote.editpost.AttachmentItem
import forpdateam.ru.forpda.extensions.getColorFromAttr
import forpdateam.ru.forpda.extensions.getDimenPx
import forpdateam.ru.forpda.model.preferences.MainPreferencesHolder
import forpdateam.ru.forpda.ui.views.CodeEditor
import forpdateam.ru.forpda.ui.views.messagepanel.advanced.AdvancedPopup
import forpdateam.ru.forpda.ui.views.messagepanel.attachments.AttachmentsPopup
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.radiationx.quill.inject

/**
 * Created by radiationx on 07.01.17.
 */
@SuppressLint("ViewConstructor")
class MessagePanel(
    context: Context,
    fragmentContainer: ViewGroup,
    targetContainer: ViewGroup,
    fullForm: Boolean
) : CardView(context) {
    var advancedButton: ImageButton? = null
        private set
    var attachmentsButton: ImageButton? = null
        private set
    var sendButton: ImageButton? = null
        private set
    var fullButton: ImageButton? = null
        private set
    var hideButton: ImageButton? = null
        private set
    var editPollButton: ImageButton? = null
        private set
    private var attachmentsCounter: TextView? = null
    private val advancedListeners: MutableList<OnClickListener> = ArrayList()
    private val attachmentsListeners: MutableList<OnClickListener> = ArrayList()
    private val sendListeners: MutableList<OnClickListener> = ArrayList()
    var messageField: CodeEditor? = null
    private var panelBehavior: MessagePanelBehavior? = null
    private var advancedPopup: AdvancedPopup? = null
    var attachmentsPopup: AttachmentsPopup? = null
        private set

    @JvmField
    val fragmentContainer: ViewGroup
    private var sendProgress: ProgressBar? = null
    var formProgress: ProgressBar? = null
        private set
    private var messageWrapper: ScrollView? = null
    var lastHeight: Int = 0
        private set
    var heightChangeListener: HeightChangeListener? = null
    private var fullForm = false
    private var params: CoordinatorLayout.LayoutParams? = null
    private var isMonospace = true
    private val mainPreferencesHolder by inject<MainPreferencesHolder>()
    private val coroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    init {
        isMonospace = mainPreferencesHolder.editorMonospace.get()
        this.fragmentContainer = fragmentContainer
        this.fullForm = fullForm
        init()
        targetContainer.addView(this, targetContainer.childCount - 1)
        onCreatePanel()
    }

    private fun init() {
        inflate(
            context, if (fullForm) R.layout.message_panel_full else R.layout.message_panel_quick,
            this
        )
        isClickable = true
        advancedButton = findViewById(R.id.button_advanced_input)
        attachmentsButton = findViewById(R.id.button_attachments)
        attachmentsCounter = findViewById(R.id.attachment_counter)
        sendButton = findViewById(R.id.button_send)
        fullButton = findViewById(R.id.button_full)
        hideButton = findViewById(R.id.button_hide)
        editPollButton = findViewById(R.id.button_edt_poll)
        messageField = findViewById(R.id.message_field)
        sendProgress = findViewById(R.id.send_progress)
        formProgress = findViewById(R.id.form_load_progress)
        messageWrapper = findViewById(R.id.message_wrapper)

        messageField!!.attachToScrollView(messageWrapper)
        messageWrapper!!.setEnabled(true)
        messageWrapper!!.setVerticalFadingEdgeEnabled(true)
        messageWrapper!!.setFadingEdgeLength(context.getDimenPx(R.dimen.dp8))

        panelBehavior = MessagePanelBehavior(context)
        params = CoordinatorLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            if (fullForm) ViewGroup.LayoutParams.MATCH_PARENT else ViewGroup.LayoutParams.WRAP_CONTENT
        )
        //params.setBehavior(panelBehavior);
        params!!.gravity = Gravity.BOTTOM
        if (!fullForm) params!!.setMargins(context.getDimenPx(R.dimen.dp8), context.getDimenPx(R.dimen.dp8), context.getDimenPx(R.dimen.dp8), context.getDimenPx(R.dimen.dp8))
        layoutParams = params
        clipToPadding = true
        radius = (if (fullForm) 0 else context.getDimenPx(R.dimen.dp8)).toFloat()
        preventCornerOverlap = false
        setCardBackgroundColor(context.getColorFromAttr(R.attr.cards_background))
        //На случай, когда добавляются несколько слушателей
        advancedButton!!.setOnClickListener(OnClickListener { v: View? ->
            for (listener in advancedListeners) listener.onClick(v)
        })
        attachmentsButton!!.setOnClickListener(OnClickListener { v: View? ->
            for (listener in attachmentsListeners) listener.onClick(v)
        })
        sendButton!!.setOnClickListener(OnClickListener { v: View? ->
            for (listener in sendListeners) listener.onClick(v)
        })


        lastHeight = height + context.getDimenPx(R.dimen.dp16)
        addOnLayoutChangeListener { v: View?, left: Int, top: Int, right: Int, bottom: Int, oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int ->
            if (heightChangeListener == null) return@addOnLayoutChangeListener
            val newHeight = height + context.getDimenPx(R.dimen.dp16)
            if (newHeight != lastHeight) {
                lastHeight = newHeight
                heightChangeListener!!.onChangedHeight(newHeight)
            }
        }

        messageField!!.addTextChangedListener(object : SimpleTextWatcher() {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.length > 0) {
                    if (sendButton!!.getColorFilter() == null) {
                        sendButton!!.setColorFilter(context.getColorFromAttr(androidx.appcompat.R.attr.colorAccent))
                    }
                } else {
                    if (sendButton!!.getColorFilter() != null) {
                        sendButton!!.clearColorFilter()
                    }
                }
            }
        })
        messageField!!.setTypeface(if (isMonospace) Typeface.MONOSPACE else Typeface.DEFAULT)

        mainPreferencesHolder
            .editorMonospace
            .onEach { value: Boolean ->
                isMonospace = value
                messageField!!.setTypeface(if (isMonospace) Typeface.MONOSPACE else Typeface.DEFAULT)
            }
            .launchIn(coroutineScope)
    }

    fun disableBehavior() {
        params!!.behavior = null
        layoutParams = params
    }

    fun enableBehavior() {
        params!!.behavior = panelBehavior
        layoutParams = params
    }

    fun setProgressState(state: Boolean) {
        sendProgress!!.visibility =
            if (state) VISIBLE else GONE
        sendButton!!.visibility = if (state) GONE else VISIBLE
    }

    fun show() {
        this.translationY = 0f
    }

    fun setText(text: String?) {
        messageField!!.setText(text)
    }

    fun insertText(text: String): Boolean {
        return insertText(text, null)
    }

    val selectionRange: IntArray
        get() {
            var selectionStart = messageField!!.selectionStart
            var selectionEnd = messageField!!.selectionEnd
            if (selectionEnd < selectionStart && selectionEnd != -1) {
                val c = selectionStart
                selectionStart = selectionEnd
                selectionEnd = c
            }
            return intArrayOf(selectionStart, selectionEnd)
        }

    @JvmOverloads
    fun insertText(startText: String, endText: String?, selectionInside: Boolean = true): Boolean {
        val selectionRange = selectionRange
        val selectionStart = selectionRange[0]
        val selectionEnd = selectionRange[1]
        return insertText(startText, endText, selectionStart, selectionEnd, selectionInside)
    }

    @JvmOverloads
    fun insertText(
        startText: String,
        endText: String?,
        selectionStart: Int,
        selectionEnd: Int,
        selectionInside: Boolean = true
    ): Boolean {
        show()
        if (endText != null && selectionStart != -1 && selectionStart != selectionEnd) {
            messageField!!.text!!.insert(selectionStart, startText)
            messageField!!.text!!.insert(selectionEnd + startText.length,  /* - 1*/endText)
            return true
        }
        messageField!!.text!!.insert(selectionStart, startText)
        if (endText != null) {
            messageField!!.text!!.insert(selectionStart + startText.length, endText)
            if (selectionInside) {
                messageField!!.setSelection(selectionStart + startText.length)
            }
        }

        return false
    }

    val selectedText: String
        get() {
            val selectionRange = selectionRange
            return messageField!!.text.toString()
                .substring(selectionRange[0], selectionRange[1])
        }

    fun deleteSelected() {
        val selectionRange = selectionRange
        messageField!!.text!!.delete(selectionRange[0], selectionRange[1])
    }

    fun updateAttachmentsCounter(count: Int) {
        attachmentsCounter!!.text = "" + count
        attachmentsCounter!!.visibility =
            if (count > 0) VISIBLE else GONE
    }

    val message: String
        get() = messageField!!.text.toString()

    fun clearMessage() {
        messageField!!.setText("")
    }

    fun clearAttachments() {
        attachmentsPopup!!.clearAttachments()
    }

    val attachments: List<AttachmentItem>
        get() = attachmentsPopup!!.getAttachments()

    private fun onCreatePanel() {
        attachmentsPopup = AttachmentsPopup(context, this)
        advancedPopup = AdvancedPopup(context, this)
    }

    fun addAdvancedOnClickListener(listener: OnClickListener) {
        advancedListeners.add(listener)
    }

    fun addAttachmentsOnClickListener(listener: OnClickListener) {
        attachmentsListeners.add(listener)
    }

    fun addSendOnClickListener(listener: OnClickListener) {
        sendListeners.add(listener)
    }

    fun getMessageField(): EditText? {
        return messageField
    }

    fun showKeyboard() {
    }

    fun setCanScrolling(canScrolling: Boolean) {
        panelBehavior!!.setCanScrolling(canScrolling)
    }

    fun interface HeightChangeListener {
        fun onChangedHeight(newHeight: Int)
    }

    fun onBackPressed(): Boolean {
        return advancedPopup == null || advancedPopup!!.onBackPressed()
    }

    fun onResume() {
        if (advancedPopup != null) advancedPopup!!.onResume()
    }

    fun onDestroy() {
        if (advancedPopup != null) advancedPopup!!.onDestroy()
        coroutineScope.cancel()
    }

    fun onPause() {
        if (advancedPopup != null) advancedPopup!!.onPause()
    }

    fun hidePopupWindows() {
        if (advancedPopup != null) advancedPopup!!.hidePopupWindows()
    }
}
