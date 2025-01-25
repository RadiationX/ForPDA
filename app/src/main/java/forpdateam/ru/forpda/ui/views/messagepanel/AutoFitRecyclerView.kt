package forpdateam.ru.forpda.ui.views.messagepanel

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import forpdateam.ru.forpda.App
import kotlin.math.max

/**
 * Created by radiationx on 08.01.17.
 */
class AutoFitRecyclerView : RecyclerView {
    var manager: GridLayoutManager? = null
        private set
    private var columnWidth = App.px48 //default value
    private var isLinear = false

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init()
    }

    private fun init() {
        manager = GridLayoutManager(context, 1)
        layoutManager = manager
    }

    fun setColumnWidth(columnWidth: Int) {
        this.columnWidth = columnWidth
        invalidate()
    }

    fun setFakeLinear(linear: Boolean) {
        isLinear = linear
        invalidate()
    }

    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        super.onMeasure(widthSpec, heightSpec)
        if (isLinear || columnWidth <= 0) {
            manager!!.spanCount = 1
        } else {
            val spanCount =
                max(1.0, (measuredWidth / columnWidth).toDouble()).toInt()
            manager!!.spanCount = spanCount
        }
    }
}
