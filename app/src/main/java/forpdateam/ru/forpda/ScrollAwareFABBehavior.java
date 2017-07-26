package forpdateam.ru.forpda;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
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