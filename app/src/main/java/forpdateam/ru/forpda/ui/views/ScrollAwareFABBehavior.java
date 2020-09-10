package forpdateam.ru.forpda.ui.views;

import android.content.Context;
import android.os.Handler;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.core.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

/**
 * Created by radiationx on 20.08.16.
 */
public class ScrollAwareFABBehavior extends CoordinatorLayout.Behavior<FloatingActionButton> {
    public ScrollAwareFABBehavior(Context context, AttributeSet attrs) {
        super();
    }

    private Handler handler = new Handler();
    private Runnable currentRunnable;
    private Interpolator interpolator = new AccelerateDecelerateInterpolator();


    @Override
    public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, FloatingActionButton child, View directTargetChild, View target, int nestedScrollAxes) {
        return nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL;
    }

    @Override
    public void onNestedScroll(CoordinatorLayout coordinatorLayout, FloatingActionButton child, View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);

        if (dyConsumed > 0 && child.getAlpha() == 1.0f) {
            child.clearAnimation();
            child.animate()
                    .scaleX(0.0f)
                    .scaleY(0.0f)
                    .alpha(0.0f)
                    .setInterpolator(interpolator)
                    .start();
        } else if (dyConsumed < 0 && child.getAlpha() == 0.0f) {
            child.clearAnimation();
            child.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .alpha(1.0f)
                    .setInterpolator(interpolator)
                    .start();
        }
    }

}