package forpdateam.ru.forpda.ui.activities.imageviewer

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.widget.FrameLayout
import androidx.annotation.IntDef
import androidx.core.view.ViewCompat
import androidx.customview.widget.ViewDragHelper
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Created by radiationx on 24.05.17.
 */
class PullBackLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val dragger: ViewDragHelper

    private val minimumFlingVelocity: Int

    /**
     * @return Allowed pulling direction
     */
    /**
     * Sets pulling directions allowed
     *
     * @param direction Directions allowed
     * @see .DIRECTION_UP
     *
     * @see .DIRECTION_DOWN
     */
    @get:Direction
    @Direction
    var direction: Int = DIRECTION_UP or DIRECTION_DOWN

    private var callback: Callback? = null

    init {
        dragger = ViewDragHelper.create(this, 1f / 8f, ViewDragCallback())
        minimumFlingVelocity = ViewConfiguration.get(context).scaledMinimumFlingVelocity
    }

    fun setCallback(callback: Callback?) {
        this.callback = callback
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return try {
            dragger.shouldInterceptTouchEvent(ev)
        } catch (e: Exception) {
            false
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        try {
            dragger.processTouchEvent(event)
            return true
        } catch (e: Exception) {
            return false
        }
    }

    override fun computeScroll() {
        if (dragger.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this)
        }
    }

    private fun onPullStart() {
        if (callback != null) {
            callback!!.onPullStart()
        }
    }

    private fun onPull(@Direction direction: Int, progress: Float) {
        if (callback != null) {
            callback!!.onPull(direction, progress)
        }
    }

    private fun onPullCancel(@Direction direction: Int) {
        if (callback != null) {
            callback!!.onPullCancel(direction)
        }
    }

    private fun onPullComplete(@Direction direction: Int) {
        if (callback != null) {
            callback!!.onPullComplete(direction)
        }
    }

    private fun reset() {
        dragger.settleCapturedViewAt(0, 0)
        invalidate()
    }

    @Retention(AnnotationRetention.SOURCE)
    @IntDef(value = [DIRECTION_UP, DIRECTION_DOWN], flag = true)
    annotation class Direction

    interface Callback {
        fun onPullStart()

        fun onPull(@Direction direction: Int, progress: Float)

        fun onPullCancel(@Direction direction: Int)

        fun onPullComplete(@Direction direction: Int)
    }

    private inner class ViewDragCallback : ViewDragHelper.Callback() {
        override fun tryCaptureView(child: View, pointerId: Int): Boolean {
            return true
        }

        override fun clampViewPositionHorizontal(child: View, left: Int, dx: Int): Int {
            return 0
        }

        override fun clampViewPositionVertical(child: View, top: Int, dy: Int): Int {
            return if ((direction and (DIRECTION_UP or DIRECTION_DOWN)) != 0) {
                top
            } else if ((direction and DIRECTION_UP) != 0) {
                min(0.0, top.toDouble()).toInt()
            } else if ((direction and DIRECTION_DOWN) != 0) {
                max(0.0, top.toDouble()).toInt()
            } else {
                0
            }
        }

        override fun getViewHorizontalDragRange(child: View): Int {
            return 0
        }

        override fun getViewVerticalDragRange(child: View): Int {
            return if (direction == 0) {
                0
            } else if ((direction and (DIRECTION_UP or DIRECTION_DOWN)) != 0) {
                height * 2
            } else {
                height
            }
        }

        override fun onViewCaptured(capturedChild: View, activePointerId: Int) {
            onPullStart()
        }

        override fun onViewPositionChanged(
            changedView: View,
            left: Int,
            top: Int,
            dx: Int,
            dy: Int
        ) {
            if (top > 0) {
                onPull(DIRECTION_DOWN, top.toFloat() / height.toFloat())
            } else if (top < 0) {
                onPull(DIRECTION_UP, -top.toFloat() / height.toFloat())
            }
        }

        override fun onViewReleased(releasedChild: View, xvel: Float, yvel: Float) {
            val top = releasedChild.top
            val slop = if (abs(yvel.toDouble()) > minimumFlingVelocity) height / 6 else height / 3

            if (top > 0) {
                if (top > slop) {
                    onPullComplete(DIRECTION_DOWN)
                } else {
                    onPullCancel(DIRECTION_DOWN)
                    reset()
                }
            } else if (top < 0) {
                if (top < -slop) {
                    onPullComplete(DIRECTION_UP)
                } else {
                    onPullCancel(DIRECTION_UP)
                    reset()
                }
            }
        }
    }

    companion object {
        /**
         * Flag indicated pulling up is allowed
         *
         * @see .setDirection
         */
        const val DIRECTION_UP: Int = 1

        /**
         * Flag indicated pulling down is allowed
         *
         * @see .setDirection
         */
        const val DIRECTION_DOWN: Int = 1 shl 1
    }
}
