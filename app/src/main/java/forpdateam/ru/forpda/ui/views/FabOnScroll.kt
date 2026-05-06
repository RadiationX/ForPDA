package forpdateam.ru.forpda.ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Interpolator
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import forpdateam.ru.forpda.App
import forpdateam.ru.forpda.App.Companion.getVecDrawable
import forpdateam.ru.forpda.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * Created by radiationx on 26.07.17.
 */
class FabOnScroll : FloatingActionButton.Behavior {

    private var animationJob: Job? = null

    private val interpolator: Interpolator = AccelerateDecelerateInterpolator()

    constructor(context: Context) : super(context, null)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    override fun onStartNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: FloatingActionButton,
        directTargetChild: View,
        target: View,
        nestedScrollAxes: Int
    ): Boolean {
        //Log.d("SUKA", "FabOnScroll onStartNestedScroll " + nestedScrollAxes);
        return nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL
    }

    override fun onNestedPreScroll(
        coordinatorLayout: CoordinatorLayout,
        child: FloatingActionButton,
        target: View,
        dx: Int,
        dy: Int,
        consumed: IntArray
    ) {
        super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed)
        //Log.d("SUKA", "FabOnScroll onNestedPreScroll" + consumed[1] + " : " + dy);
        if (child.alpha == 0.0f && abs(dy.toDouble()) > App.px24) {
            child.setImageDrawable(
                getVecDrawable(
                    child.context,
                    if (dy > 0) R.drawable.ic_arrow_down else R.drawable.ic_arrow_up
                )
            )
            child.clearAnimation()
            child.animate()
                .scaleX(1.0f)
                .scaleY(1.0f)
                .alpha(1.0f)
                .setInterpolator(interpolator)
                .start()
            child.isClickable = true
        }
    }

    override fun onNestedFling(
        coordinatorLayout: CoordinatorLayout,
        child: FloatingActionButton,
        target: View,
        velocityX: Float,
        velocityY: Float,
        consumed: Boolean
    ): Boolean {
        //Log.d("SUKA", "FabOnScroll onNestedFling" + velocityY + " : " + consumed);
        return super.onNestedFling(coordinatorLayout, child, target, velocityX, velocityY, consumed)
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
        //Log.d("SUKA", "FabOnScroll onNestedScroll " + dyConsumed + " : " + dyUnconsumed + " : " + App.px24);
        if (child.alpha == 0.0f && abs(dyUnconsumed.toDouble()) > App.px24) {
            child.clearAnimation()
            child.animate()
                .scaleX(1.0f)
                .scaleY(1.0f)
                .alpha(1.0f)
                .setInterpolator(interpolator)
                .start()
            child.isClickable = true
        }
    }


    override fun onStopNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: FloatingActionButton,
        target: View
    ) {
        super.onStopNestedScroll(coordinatorLayout, child, target)
        animationJob?.cancel()
        animationJob = GlobalScope.launch(Dispatchers.Main) {
            delay(1000)
            child.clearAnimation()
            child.animate()
                .scaleX(0.0f)
                .scaleY(0.0f)
                .alpha(0.0f)
                .setInterpolator(interpolator)
                .start()
            child.isClickable = false
        }
    }
}
