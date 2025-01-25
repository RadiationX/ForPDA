package forpdateam.ru.forpda.ui.views

import android.content.Context
import android.os.Handler
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Interpolator
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton

/**
 * Created by radiationx on 20.08.16.
 */
class ScrollAwareFABBehavior(
    context: Context?,
    attrs: AttributeSet?
) : CoordinatorLayout.Behavior<FloatingActionButton>(context, attrs) {
    private val handler = Handler()
    private val currentRunnable: Runnable? = null
    private val interpolator: Interpolator = AccelerateDecelerateInterpolator()


    override fun onStartNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: FloatingActionButton,
        directTargetChild: View,
        target: View,
        nestedScrollAxes: Int
    ): Boolean {
        return nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL
    }

    override fun onNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: FloatingActionButton,
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int
    ) {
        super.onNestedScroll(
            coordinatorLayout,
            child,
            target,
            dxConsumed,
            dyConsumed,
            dxUnconsumed,
            dyUnconsumed
        )

        if (dyConsumed > 0 && child.alpha == 1.0f) {
            child.clearAnimation()
            child.animate()
                .scaleX(0.0f)
                .scaleY(0.0f)
                .alpha(0.0f)
                .setInterpolator(interpolator)
                .start()
        } else if (dyConsumed < 0 && child.alpha == 0.0f) {
            child.clearAnimation()
            child.animate()
                .scaleX(1.0f)
                .scaleY(1.0f)
                .alpha(1.0f)
                .setInterpolator(interpolator)
                .start()
        }
    }
}