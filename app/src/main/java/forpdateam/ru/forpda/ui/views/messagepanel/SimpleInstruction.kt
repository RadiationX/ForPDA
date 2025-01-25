package forpdateam.ru.forpda.ui.views.messagepanel

import android.content.Context
import android.view.View
import android.widget.Button
import android.widget.ScrollView
import android.widget.TextView
import forpdateam.ru.forpda.R

/**
 * Created by radiationx on 26.05.17.
 */
class SimpleInstruction(context: Context?) : ScrollView(context) {
    private val messageView: TextView
    private val closeButton: Button
    private var listener: OnClickListener? = null

    init {
        addView(inflate(context, R.layout.message_panel_instruction, null))
        isFillViewport = true
        messageView = findViewById(R.id.instruction_message)
        closeButton = findViewById(R.id.instruction_close_button)
        closeButton.setOnClickListener { v: View? ->
            this.visibility =
                GONE
            if (listener != null) {
                listener!!.onClick(v)
            }
        }
    }

    fun setText(text: String?) {
        messageView.text = text
    }

    fun setOnCloseClick(listener: OnClickListener?) {
        this.listener = listener
    }
}
