package forpdateam.ru.forpda.ui

import android.util.Log
import android.view.View

/**
 * Created by radiationx on 30.12.17.
 */
class DimensionHelper(
        measurer: View,
        private val container: View,
        private val listener: DimensionsListener,
        private val defaultStatusBarHeight: Int = 0,
        private val defaultKeyboardHeight: Int = 0
) {

    private val dimension = Dimensions()

    private var lastSb = 0
    private var lastNb = 0
    private var lastCh = 0
    private var lastKh = 0

    init {
        dimension.also {
            it.statusBar = defaultStatusBarHeight
            it.savedKeyboardHeight = defaultKeyboardHeight
            listener.onDimensionsChange(it)
        }
        measurer.addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            Log.e("S_DEF_LOG", "OnLayoutChange $left $top $right $bottom ||| $oldLeft $oldTop $oldRight $oldBottom")
            var anyChanges = false
            if (dimension.contentHeight == 0) {
                dimension.statusBar = v.top
                dimension.navigationBar = container.bottom - v.bottom
            }

            dimension.contentHeight = v.height
            dimension.keyboardHeight = container.height - dimension.contentHeight - dimension.statusBar - dimension.navigationBar

            if (dimension.isKeyboardShow()) {
                dimension.savedKeyboardHeight = dimension.keyboardHeight
            }

            dimension.also {
                if (it.statusBar != lastSb
                        || it.navigationBar != lastNb
                        || it.contentHeight != lastCh
                        || it.keyboardHeight != lastKh) {

                    lastSb = it.statusBar
                    lastNb = it.navigationBar
                    lastCh = it.contentHeight
                    lastKh = it.keyboardHeight
                    listener.onDimensionsChange(it)
                }
            }
        }
    }

    class Dimensions {
        var statusBar = 0
        var navigationBar = 0
        var contentHeight = 0
        var keyboardHeight = 0
        var savedKeyboardHeight = 0
        var isFakeKeyboardShow = false

        fun isKeyboardShow(): Boolean = keyboardHeight > 100

        override fun toString(): String {
            return "Dimensions: to=$statusBar, bo=$navigationBar, ch=$contentHeight, kh=$keyboardHeight, skh=$savedKeyboardHeight, ifks=$isFakeKeyboardShow, iks=${isKeyboardShow()}"
        }
    }

    interface DimensionsListener {
        fun onDimensionsChange(dimensions: Dimensions)
    }
}