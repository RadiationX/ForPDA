package forpdateam.ru.forpda.ui.activities.imageviewer

import android.content.Context
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.MotionEvent

/**
 * Created by radiationx on 24.05.17.
 */

class HackyViewPager : ViewPager {

    var isLocked: Boolean = false

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (!isLocked) {
            try {
                return super.onInterceptTouchEvent(ev)
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
                return false
            }

        }
        return false
    }

    override fun onTouchEvent(event: MotionEvent): Boolean = try {
        super.onTouchEvent(event)
    } catch (e: Exception) {
        false
    }

    fun toggleLock() {
        isLocked = !isLocked
    }

}
