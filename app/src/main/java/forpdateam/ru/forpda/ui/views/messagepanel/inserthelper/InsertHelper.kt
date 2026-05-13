package forpdateam.ru.forpda.ui.views.messagepanel.inserthelper

import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import com.google.android.material.textfield.TextInputLayout
import forpdateam.ru.forpda.R

/**
 * Created by radiationx on 27.05.17.
 */
class InsertHelper(private val context: Context) {
    private val headers = ArrayList<Pair<String, String?>>()
    private val headersLayout = ArrayList<EditText?>()
    private var bodyLayout: EditText? = null
    private var body: Pair<String, String?>? = null
    private val title: String? = null
    private val inflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private val layoutContainer =
        inflater.inflate(R.layout.insert_helper_body, null) as ScrollView
    private val itemsContainer: LinearLayout =
        layoutContainer.findViewById(R.id.insert_helper_items_container)
    private var insertListener: InsertListener? = null

    fun addHeader(@StringRes titleRes: Int, code: String?) {
        addHeader(context.getString(titleRes), code)
    }

    fun addHeader(title: String, code: String?) {
        headers.add(Pair(title, code))
        val inputLayout = inflater.inflate(R.layout.insert_helper_item, null) as TextInputLayout
        inputLayout.hint = title
        headersLayout.add(inputLayout.editText)
        itemsContainer.addView(inputLayout)
    }

    fun setBody(@StringRes titleRes: Int, value: String?) {
        setBody(context.getString(titleRes), value)
    }

    fun setBody(title: String, value: String?) {
        this.body = Pair(title, value)
        val inputLayout = inflater.inflate(R.layout.insert_helper_item, null) as TextInputLayout
        inputLayout.hint = title
        val textView = inputLayout.findViewById<TextView>(R.id.insert_helper_item_text)
        textView.text = value
        bodyLayout = inputLayout.editText
        itemsContainer.addView(inputLayout)
    }

    fun setInsertListener(insertListener: InsertListener?) {
        this.insertListener = insertListener
    }

    fun show() {
        val alertDialog = AlertDialog.Builder(
            context
        )
            .setView(layoutContainer)
            .setPositiveButton(
                R.string.insert
            ) { dialog: DialogInterface?, which: Int ->
                if (insertListener != null) {
                    val resultHeaders =
                        ArrayList<Pair<String?, String?>>()
                    for (i in headers.indices) {
                        var value: String? = null
                        val editable = headersLayout[i]!!.text
                        if (editable != null) {
                            value = editable.toString()
                            if (value.length == 0) {
                                value = null
                            }
                        }
                        resultHeaders.add(
                            Pair(
                                headers[i].second,
                                value
                            )
                        )
                    }
                    if (bodyLayout != null) {
                        insertListener!!.onInsert(resultHeaders, bodyLayout!!.text.toString())
                    } else {
                        insertListener!!.onInsert(resultHeaders, null)
                    }
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
        if (alertDialog.window != null) alertDialog.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
    }

    fun interface InsertListener {
        fun onInsert(resultHeaders: ArrayList<Pair<String?, String?>>?, bodyResult: String?)
    }
}
