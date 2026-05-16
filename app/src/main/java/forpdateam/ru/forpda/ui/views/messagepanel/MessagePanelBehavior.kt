package forpdateam.ru.forpda.ui.views.messagepanel

import android.content.Context
import android.view.View
import androidx.cardview.widget.CardView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.appbar.AppBarLayout
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.extensions.getDimenPx

/**
 * Created by radiationx on 07.01.17.
 */
class MessagePanelBehavior(private val context: Context) : CoordinatorLayout.Behavior<CardView>() {
    private var canScrolling = true
    private val dp8 = context.getDimenPx(R.dimen.dp8)
    override fun onStartNestedScroll(
        coordinatorLayout: CoordinatorLayout, child: CardView,
        directTargetChild: View, target: View, nestedScrollAxes: Int
    ): Boolean {
        if (!canScrolling) child.translationY = 0f
        return canScrolling
    }


    fun setCanScrolling(canScrolling: Boolean) {
        this.canScrolling = canScrolling
    }


    override fun layoutDependsOn(
        parent: CoordinatorLayout,
        child: CardView,
        dependency: View
    ): Boolean {
        return dependency is AppBarLayout
    }

    override fun onDependentViewChanged(
        parent: CoordinatorLayout,
        child: CardView,
        dependency: View
    ): Boolean {
        if (!canScrolling) return false
        val percent = 1.0f - (-dependency.top.toFloat() / dependency.measuredHeight.toFloat())

        val scrolled = ((child.measuredHeight + (2 * dp8)) * percent).toInt()
        child.translationY = scrolled.toFloat()
        return true
    }
}
