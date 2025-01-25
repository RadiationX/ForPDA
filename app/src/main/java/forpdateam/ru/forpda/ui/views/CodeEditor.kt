package forpdateam.ru.forpda.ui.views

import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.text.Editable
import android.text.Spannable
import android.text.TextWatcher
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.ScrollView
import androidx.appcompat.widget.AppCompatEditText
import forpdateam.ru.forpda.common.Html
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.math.max
import kotlin.math.min

/**
 * Created by radiationx on 27.07.17.
 */

/*
 * ORIGINAL: https://github.com/markusfisch/CodeEditor/blob/master/app/src/main/java/de/markusfisch/android/CodeEditor/widget/CodeEditor.java
 * */
class CodeEditor : AppCompatEditText {
    private object ForumCodes {
        val ELEMENT: Pattern = Pattern.compile(
            "(\\[(?:\\/)?((?:attachment|background|nomergetime|mergetime|snapback|numlist|spoiler|offtop|center|color|right|quote|code|font|hide|left|list|size|img|sub|sup|cur|url|b|i|u|s|\\*)))=?\\s?([^\\]\\[]+?)?(\\])",
            Pattern.CASE_INSENSITIVE
        )
        val ATTRIBUTE: Pattern = Pattern.compile(
            "(name|date|post)?=?([\\s\\S]+?)\\s?(?=(?:name|date|post)=|\\z)",
            Pattern.CASE_INSENSITIVE
        )
    }

    private val updateHandler = Handler()
    private val updateRunnable = Runnable {
        val e = text
        highlightWithoutChange(e!!)
    }

    private var updateDelay = 500
    private var modified = true
    private var colorTag = 0
    private var colorAttrName = 0
    private var colorAttrValue = 0
    private var scrollView: ScrollView? = null

    private var scrollerTask: Runnable? = null
    private var initialPosition = 0
    private val newCheck = 100

    fun attachToScrollView(sv: ScrollView?) {
        scrollView = sv

        scrollView!!.addOnLayoutChangeListener { v: View, left: Int, top: Int, right: Int, bottom: Int, oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int ->
            val heightWas = oldBottom - oldTop
            if (v.height != heightWas) {
                smartUpdateHighlighting()
            }
        }

        scrollView!!.setOnTouchListener { v: View?, event: MotionEvent ->
            if (event.action == MotionEvent.ACTION_UP) {
                initialPosition = scrollView!!.scrollY
                scrollView!!.postDelayed(scrollerTask, newCheck.toLong())
            }
            false
        }

        scrollerTask = Runnable {
            val newPosition = scrollView!!.scrollY
            if (initialPosition - newPosition == 0) {
                smartUpdateHighlighting()
            } else {
                initialPosition = scrollView!!.scrollY
                scrollView!!.postDelayed(scrollerTask, newCheck.toLong())
            }
        }
    }

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }


    fun setUpdateDelay(ms: Int) {
        updateDelay = ms
    }


    fun updateHighlighting() {
        highlightWithoutChange(text!!)
    }

    private fun init() {
        //setHorizontallyScrolling(true);

        addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(
                s: CharSequence,
                start: Int,
                before: Int,
                count: Int
            ) {
            }

            override fun beforeTextChanged(
                s: CharSequence,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun afterTextChanged(e: Editable) {
                smartUpdateHighlighting()
            }
        })

        setSyntaxColors()
        setUpdateDelay(500)
        updateHighlighting()
    }

    private fun smartUpdateHighlighting() {
        cancelUpdate()
        if (!modified) {
            return
        }
        updateHandler.postDelayed(updateRunnable, updateDelay.toLong())
    }

    private fun setSyntaxColors() {
        colorTag = Color.parseColor("#446FBD")
        colorAttrName = Color.parseColor("#6D8600")
        colorAttrValue = Color.parseColor("#e88501")
    }

    private fun cancelUpdate() {
        updateHandler.removeCallbacks(updateRunnable)
    }

    private fun highlightWithoutChange(e: Editable) {
        modified = false
        highlight(e)
        modified = true
    }

    private fun highlight(e: Editable): Editable? {
        val time = System.currentTimeMillis()
        try {
            // don't use e.clearSpans() because it will
            // remove too much
            clearSpans(e)

            if (e.length == 0) {
                return e
            }

            val scrollY = scrollView!!.scrollY
            val scrollViewHeight = scrollView!!.height


            var visibleStart = getOffsetForPosition(0f, scrollY.toFloat())
            var visibleEnd = getOffsetForPosition(0f, (scrollY + scrollViewHeight).toFloat())
            visibleStart = max(0.0, (visibleStart - 100).toDouble()).toInt()
            visibleEnd = min(e.length.toDouble(), (visibleEnd + 100).toDouble()).toInt()

            val hlText = e.subSequence(visibleStart, visibleEnd)
            //visibleStart = 0;
            //visibleEnd = 0;
            val m = ForumCodes.ELEMENT.matcher(hlText)
            var attributes: Matcher? = null
            while (m.find()) {
                val attrsSrc = m.group(3)
                if (attrsSrc != null) {
                    val tagName = m.group(2)
                    val eg3s = m.start(3)
                    val eg3e = m.end(3)
                    var color = colorAttrValue

                    if (tagName.equals("quote", ignoreCase = true)) {
                        attributes = if (attributes == null) {
                            ForumCodes.ATTRIBUTE.matcher(attrsSrc)
                        } else {
                            attributes.reset(attrsSrc)
                        }

                        while (attributes.find()) {
                            val attrName = attributes.group(1)
                            if (attrName != null) {
                                val ag1s = attributes.start(1)
                                val ag1e = attributes.end(1)
                                e.setSpan(
                                    ForegroundColorSpan(colorAttrName),
                                    visibleStart + eg3s + ag1s,
                                    visibleStart + eg3s + ag1e,
                                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                                )
                            }

                            val attrValue = attributes.group(2)
                            if (attrValue != null) {
                                val ag2s = attributes.start(2)
                                val ag2e = attributes.end(2)

                                e.setSpan(
                                    ForegroundColorSpan(color),
                                    visibleStart + eg3s + ag2s,
                                    visibleStart + eg3s + ag2e,
                                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                                )
                            }
                        }
                    } else {
                        if (tagName.equals(
                                "color",
                                ignoreCase = true
                            ) || tagName.equals("background", ignoreCase = true)
                        ) {
                            try {
                                if (attrsSrc[0] != '#') {
                                    val htmlColor = Html.getColorMap()[attrsSrc]
                                    if (htmlColor != null) {
                                        color = htmlColor
                                    }
                                } else {
                                    color = Color.parseColor(attrsSrc)
                                }
                            } catch (ignore: Exception) {
                            }
                        }
                        e.setSpan(
                            ForegroundColorSpan(color),
                            visibleStart + eg3s,
                            visibleStart + eg3e,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }
                }

                val eg1s = m.start(1)
                val eg1e = m.end(1)
                e.setSpan(
                    ForegroundColorSpan(colorTag),
                    visibleStart + eg1s,
                    visibleStart + eg1e,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                val eg4s = m.start(4)
                val eg4e = m.end(4)
                e.setSpan(
                    ForegroundColorSpan(colorTag),
                    visibleStart + eg4s,
                    visibleStart + eg4e,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        } catch (ex: IllegalStateException) {
            // raised by Matcher.start()/.end() when
            // no successful match has been made what
            // shouldn't ever happen because of find()
        }

        Log.d("CodeEditor", "Time: " + (System.currentTimeMillis() - time))
        return e
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        cancelUpdate()
    }

    companion object {
        private fun clearSpans(e: Editable) {
            // remove foreground color spans
            run {
                val spans = e.getSpans(
                    0,
                    e.length,
                    ForegroundColorSpan::class.java
                )
                var i = spans.size
                while (i-- > 0) {
                    e.removeSpan(spans[i])
                }
            }

            // remove background color spans
            run {
                val spans = e.getSpans(
                    0,
                    e.length,
                    BackgroundColorSpan::class.java
                )
                var i = spans.size
                while (i-- > 0) {
                    e.removeSpan(spans[i])
                }
            }
        }
    }
}