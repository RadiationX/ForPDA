package forpdateam.ru.forpda.ui.views.messagepanel.advanced

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.ItemTouchHelper
import com.google.android.material.textfield.TextInputLayout
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.common.simple.SimpleTextWatcher
import forpdateam.ru.forpda.extensions.getDimenPx
import forpdateam.ru.forpda.model.preferences.OtherPreferencesHolder
import forpdateam.ru.forpda.ui.views.messagepanel.MessagePanel
import forpdateam.ru.forpda.ui.views.messagepanel.SimpleInstruction
import forpdateam.ru.forpda.ui.views.messagepanel.advanced.adapters.ItemDragCallback
import forpdateam.ru.forpda.ui.views.messagepanel.advanced.adapters.PanelItemAdapter
import forpdateam.ru.forpda.ui.views.messagepanel.colorpicker.ColorPicker
import forpdateam.ru.forpda.ui.views.messagepanel.inserthelper.InsertHelper
import ru.radiationx.quill.inject
import java.util.Collections

/**
 * Created by radiationx on 08.01.17.
 */
@SuppressLint("ViewConstructor")
class CodesPanelItem(context: Context, panel: MessagePanel) :
    BasePanelItem(context, panel, context.getString(R.string.codes_title)) {
    private val openedCodes: List<String> = ArrayList()
    private val otherPreferencesHolder by context.inject<OtherPreferencesHolder>()
    private val clickListener = listener@{ item: PanelListItem ->
        if (item !is PanelListItem.BBCode) {
            return@listener
        }
        when (item.text) {
            "URL" -> urlInsert(item)
            "QUOTE" -> quoteInsert(item)
            "CODE" -> codeInsert(item)
            "SPOILER" -> spoilerInsert(item)
            "LIST" -> listInsert(item, false)
            "NUMLIST" -> listInsert(item, true)
            "COLOR" -> colorInsert(item)
            "BACKGROUND" -> colorInsert(item)
            "SIZE" -> sizeInsert(item)
            "FONT" -> fontInsert(item)
            else -> simpleInsertText(item)
        }
    }

    init {
        val adapter = PanelItemAdapter(codes.toMutableList(), clickListener)

        recyclerView.setColumnWidth(recyclerView.context.getDimenPx(R.dimen.dp96))
        val touchHelper = ItemTouchHelper(ItemDragCallback(adapter))
        touchHelper.attachToRecyclerView(recyclerView)
        recyclerView.adapter = adapter

        if (otherPreferencesHolder.tooltipMessagePanelSorting.get()) {
            val instruction = SimpleInstruction(getContext())
            instruction.setText(context.getString(R.string.code_panel_instruction))
            instruction.setOnCloseClick { v: View? ->
                otherPreferencesHolder.tooltipMessagePanelSorting.set(false)
            }
            addView(instruction)
        }
    }

    private fun listInsert(item: PanelListItem.BBCode, num: Boolean) {
        val selected = messagePanel.selectedText
        val listLines: MutableList<String> = ArrayList()
        val tag = "LIST"
        if (selected.length > 0) {
            AlertDialog.Builder(context)
                .setMessage(R.string.transform_string_to_list)
                .setPositiveButton(R.string.ok) { dialog: DialogInterface?, which: Int ->
                    val lines = TextUtils.split(selected, "\n")
                    Collections.addAll(listLines, *lines)
                    messagePanel.deleteSelected()
                }
                .setNegativeButton(R.string.no, null)
                .setOnDismissListener { dialog: DialogInterface? ->
                    listInsert(
                        tag,
                        num,
                        listLines
                    )
                }
                .show()
        } else {
            listInsert(tag, num, listLines)
        }
    }

    private fun listInsert(tag: String, num: Boolean, listLines: MutableList<String>) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val layout = checkNotNull(inflater.inflate(R.layout.report_layout, null))
        val messageField = layout.findViewById<EditText>(R.id.report_text_field)
        val inputLayout = layout.findViewById<TextInputLayout>(R.id.report_input_layout)
        val i = intArrayOf(listLines.size + 1)
        inputLayout.hint = context.getString(R.string.codes_list_item_Pos, i[0])
        val alertDialog = AlertDialog.Builder(
            context
        )
            .setView(layout)
            .setPositiveButton(R.string.add, null)
            .setNegativeButton(R.string.close) { dialog: DialogInterface?, which: Int ->
                val body = StringBuilder()
                for (line in listLines) {
                    body.append("[*]").append(line).append('\n')
                }
                val resultHeaders: MutableList<Pair<String?, String?>> =
                    ArrayList()
                if (num) {
                    resultHeaders.add(Pair(null, "1"))
                }
                val bbcodes = createBbCode(tag, resultHeaders, body.toString())
                messagePanel.insertText(bbcodes[0], bbcodes[1], false)
            }
            .show()
        val positiveButton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE)
        positiveButton.isEnabled = false
        positiveButton.setOnClickListener { v: View? ->
            i[0]++
            listLines.add(messageField.text.toString())
            messageField.setText("")
            inputLayout.hint = context.getString(R.string.codes_list_item_Pos, i[0])
        }
        messageField.addTextChangedListener(object : SimpleTextWatcher() {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                positiveButton.isEnabled = s.length > 0
            }
        })
    }

    private fun colorInsert(item: PanelListItem.BBCode) {
        ColorPicker(context, messagePanel) {
            val color = getHtmlColor(it.hexColor)
            val resultHeaders: MutableList<Pair<String?, String?>> = ArrayList()
            resultHeaders.add(Pair(null, color))
            val bbcodes = createBbCode(item.text, resultHeaders, null)
            messagePanel.insertText(bbcodes[0], bbcodes[1])
        }
    }

    private fun sizeInsert(item: PanelListItem.BBCode) {
        val items = arrayOf<CharSequence>(
            "1 (8pt)",
            "2 (10pt)",
            "3 (12pt)",
            "4 (14pt)",
            "5 (18pt)",
            "6 (24pt)",
            "7 (36pt)"
        )
        for (i in items.indices) {
            items[i] = context.getString(R.string.codes_text_size_item_Size,items[i])
        }
        AlertDialog.Builder(context)
            .setTitle(R.string.codes_text_size)
            .setItems(items) { dialog: DialogInterface, which: Int ->
                val resultHeaders: MutableList<Pair<String?, String?>> =
                    ArrayList()
                resultHeaders.add(
                    Pair(
                        null,
                        (which + 1).toString()
                    )
                )
                val bbcodes = createBbCode(item.text, resultHeaders, null)
                messagePanel.insertText(bbcodes[0], bbcodes[1])
                dialog.dismiss()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun fontInsert(item: PanelListItem.BBCode) {
        val selected = messagePanel.selectedText
        val range = messagePanel.selectionRange
        val insertHelper = InsertHelper(
            context
        )
        insertHelper.addHeader(R.string.codes_font, null)
        if (selected.isEmpty()) insertHelper.setBody(
            R.string.codes_font_text,
            null
        )
        insertHelper.setInsertListener { resultHeaders, bodyResult ->
            val bbcodes = createBbCode(item.text, resultHeaders, bodyResult)
            messagePanel.insertText(bbcodes[0], bbcodes[1], range[0], range[1])
        }
        insertHelper.show()
    }

    private fun urlInsert(item: PanelListItem.BBCode) {
        val selected = messagePanel.selectedText
        val range = messagePanel.selectionRange
        val insertHelper = InsertHelper(
            context
        )
        insertHelper.addHeader(R.string.codes_link, null)
        if (selected.length == 0) insertHelper.setBody(
            R.string.codes_link_text,
            null
        )
        insertHelper.setInsertListener { resultHeaders, bodyResult ->
            val bbcodes = createBbCode(item.text, resultHeaders, bodyResult)
            messagePanel.insertText(bbcodes[0], bbcodes[1], range[0], range[1])
        }
        insertHelper.show()
    }

    private fun spoilerInsert(item: PanelListItem.BBCode) {
        val selected = messagePanel.selectedText
        val range = messagePanel.selectionRange
        val insertHelper = InsertHelper(
            context
        )
        insertHelper.addHeader(R.string.codes_block_title, null)
        if (selected.length == 0) insertHelper.setBody(
            R.string.codes_spoiler_text,
            null
        )
        insertHelper.setInsertListener { resultHeaders, bodyResult ->
            val bbcodes = createBbCode(item.text, resultHeaders, bodyResult)
            messagePanel.insertText(bbcodes[0], bbcodes[1], range[0], range[1])
        }
        insertHelper.show()
    }

    private fun codeInsert(item: PanelListItem.BBCode) {
        val selected = messagePanel.selectedText
        val range = messagePanel.selectionRange
        val insertHelper = InsertHelper(
            context
        )
        insertHelper.addHeader(R.string.codes_block_title, null)
        if (selected.length == 0) insertHelper.setBody(
            R.string.codes_code_text,
            null
        )
        insertHelper.setInsertListener { resultHeaders, bodyResult ->
            val bbcodes = createBbCode(item.text, resultHeaders, bodyResult)
            messagePanel.insertText(bbcodes[0], bbcodes[1], range[0], range[1])
        }
        insertHelper.show()
    }

    private fun quoteInsert(item: PanelListItem.BBCode) {
        val selected = messagePanel.selectedText
        val range = messagePanel.selectionRange
        val insertHelper = InsertHelper(
            context
        )
        insertHelper.addHeader(R.string.codes_block_title, "name")
        /*insertHelper.addHeader("Дата", "date");
        insertHelper.addHeader("ID поста", "post");*/
        if (selected.length == 0) insertHelper.setBody(
            R.string.codes_quote_text,
            null
        )
        insertHelper.setInsertListener { resultHeaders, bodyResult ->
            val bbcodes = createBbCode(item.text, resultHeaders, bodyResult)
            messagePanel.insertText(bbcodes[0], bbcodes[1], range[0], range[1])
        }
        insertHelper.show()
    }

    private fun createBbCode(
        tag: String,
        headers: List<Pair<String?, String?>>?,
        body: String?
    ): Array<String> {
        var start: StringBuilder? = null
        var end: String? = null

        start = StringBuilder("[$tag")
        if (headers != null) {
            for (header in headers) {
                if (header.first == null && header.second != null) {
                    start.append("=").append(header.second)
                    break
                }
            }
            for (header in headers) {
                if (header.first == null || header.second == null) continue
                start.append(" ").append(header.first).append("=\"").append(header.second)
                    .append("\"")
            }
        }
        start.append("]")

        if (body != null) {
            start.append(body)
        }
        end = "[/$tag]"

        //Log.d("FORPDA_LOG", "CREATE BB CODE " + start + " : " + end);
        return arrayOf(start.toString(), end)
    }

    private fun simpleInsertText(item: PanelListItem.BBCode) {
        val bbcodes = createBbCode(item.text, null, null)
        messagePanel.insertText(bbcodes[0], bbcodes[1])
    }

    /* private void defaultInsertText(ButtonData item) {
        String tag = item.getText();
        String startText = null;
        String endText = null;
        int indexOf = openedCodes.indexOf(tag);


        startText = "[".concat(indexOf >= 0 ? "/" : "").concat(tag).concat("]");
        if (indexOf < 0)
            endText = "[/".concat(tag).concat("]");

        if (messagePanel.insertText(startText, endText)) return;

        if (indexOf >= 0) {
            openedCodes.remove(indexOf);
        } else {
            openedCodes.add(tag);
        }
    }
*/
    override fun onDetachedFromWindow() {
        val listCodes: MutableList<String?> = ArrayList()
        for (item in codes) {
            listCodes.add(item.text)
        }
        val sorted = TextUtils.join(",", listCodes)
        otherPreferencesHolder.messagePanelBbCodes.set(sorted)
        super.onDetachedFromWindow()
    }

    private val codes: MutableList<PanelListItem.BBCode>
        get() {
            if (Companion.codes != null) return Companion.codes!!
            val codes = ArrayList<PanelListItem.BBCode>()
            Companion.codes = codes
            val tempCodes = mutableListOf(
                PanelListItem.BBCode(
                    "B",
                    R.drawable.ic_code_bold,
                    R.string.codes_name_bold
                ),
                PanelListItem.BBCode(
                    "I",
                    R.drawable.ic_code_italic,
                    R.string.codes_name_italic
                ),
                PanelListItem.BBCode(
                    "U",
                    R.drawable.ic_code_underline,
                    R.string.codes_name_underline
                ),
                PanelListItem.BBCode(
                    "S",
                    R.drawable.ic_code_s,
                    R.string.codes_name_s
                ),
                PanelListItem.BBCode(
                    "URL",
                    R.drawable.ic_code_url,
                    R.string.codes_name_link
                ),
                PanelListItem.BBCode(
                    "SPOILER",
                    R.drawable.ic_code_spoiler,
                    R.string.codes_name_spoiler
                ),
                PanelListItem.BBCode(
                    "OFFTOP",
                    R.drawable.ic_code_offtop,
                    R.string.codes_name_offtop
                ),
                PanelListItem.BBCode(
                    "QUOTE",
                    R.drawable.ic_code_quote,
                    R.string.codes_name_quote
                ),
                PanelListItem.BBCode(
                    "CODE",
                    R.drawable.ic_code_code,
                    R.string.codes_name_code
                ),
                PanelListItem.BBCode(
                    "COLOR",
                    R.drawable.ic_code_color,
                    R.string.codes_name_text_color
                ),
                PanelListItem.BBCode(
                    "SIZE",
                    R.drawable.ic_code_size,
                    R.string.codes_name_text_size
                ),
                PanelListItem.BBCode(
                    "FONT",
                    R.drawable.ic_code_font,
                    R.string.codes_name_font
                ),

                PanelListItem.BBCode(
                    "HIDE",
                    R.drawable.ic_code_hide,
                    R.string.codes_name_hide
                ),
                PanelListItem.BBCode(
                    "BACKGROUND",
                    R.drawable.ic_code_background,
                    R.string.codes_name_bg_color
                ),
                PanelListItem.BBCode(
                    "LIST",
                    R.drawable.ic_code_list,
                    R.string.codes_name_list
                ),
                PanelListItem.BBCode(
                    "NUMLIST",
                    R.drawable.ic_code_numlist,
                    R.string.codes_name_numlist
                ),
                PanelListItem.BBCode(
                    "LEFT",
                    R.drawable.ic_code_left,
                    R.string.codes_name_left
                ),
                PanelListItem.BBCode(
                    "CENTER",
                    R.drawable.ic_code_center,
                    R.string.codes_name_center
                ),
                PanelListItem.BBCode(
                    "RIGHT",
                    R.drawable.ic_code_right,
                    R.string.codes_name_right
                ),
                PanelListItem.BBCode(
                    "SUB",
                    R.drawable.ic_code_sub,
                    R.string.codes_name_sub
                ),
                PanelListItem.BBCode(
                    "SUP",
                    R.drawable.ic_code_sup,
                    R.string.codes_name_sup
                ),
                PanelListItem.BBCode(
                    "CUR",
                    R.drawable.ic_code_cur,
                    R.string.codes_name_curator
                )
            )


            val sorted = otherPreferencesHolder.messagePanelBbCodes.get()
            if (!sorted.isNullOrEmpty()) {
                val sortedArr =
                    TextUtils.split(sorted, ",")
                if (sortedArr.size != tempCodes.size) {
                    otherPreferencesHolder.messagePanelBbCodes.remove()
                    codes.addAll(tempCodes)
                } else {
                    for (code in sortedArr) {
                        for (item in tempCodes) {
                            if (item.text == code) {
                                codes.add(item)
                                break
                            }
                        }
                    }
                }
            } else {
                codes.addAll(tempCodes)
            }
            tempCodes.clear()

            return codes
        }


    private fun getHtmlColor(hexColor: String): String {
        if (colors == null) {
            val colors = HashMap<String, String>()
            Companion.colors = colors
            colors["#000000"] = "black"
            colors["#FFFFFF"] = "white"
            colors["#82CEE8"] = "skyblue"
            colors["#426AE6"] = "royalblue"
            colors["#0000FF"] = "blue"
            colors["#07008C"] = "darkblue"
            colors["#FDA500"] = "orange"
            colors["#FF4300"] = "orangered"
            colors["#E1133A"] = "crimson"
            colors["#FF0000"] = "red"
            colors["#8C0000"] = "darkred"
            colors["#008000"] = "green"
            colors["#41A317"] = "limegreen"
            colors["#4E8975"] = "seagreen"
            colors["#F52887"] = "deeppink"
            colors["#FF6245"] = "tomato"
            colors["#F76541"] = "coral"
            colors["#800080"] = "purple"
            colors["#440087"] = "indigo"
            colors["#E3B382"] = "burlywood"
            colors["#EE9A4D"] = "sandybrown"
            colors["#C35817"] = "sienna"
            colors["#C85A17"] = "chocolate"
            colors["#037F81"] = "teal"
            colors["#C0C0C0"] = "silver"
            colors["#808080"] = "gray"
        }
        val res = colors!![hexColor] ?: return hexColor
        return res
    }

    companion object {
        private var codes: MutableList<PanelListItem.BBCode>? = null
        private var colors: MutableMap<String, String>? = null
    }
}
