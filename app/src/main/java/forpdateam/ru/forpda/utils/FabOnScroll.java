package forpdateam.ru.forpda.utils;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

import forpdateam.ru.forpda.App;

/**
 * Created by radiationx on 26.07.17.
 */

public class FabOnScroll extends FloatingActionButton.Behavior {
    private Handler handler = new Handler();
    private Runnable currentRunnable;
    private Interpolator interpolator = new AccelerateDecelerateInterpolator();

    public FabOnScroll(Context context, AttributeSet attrs) {
        super();
    }

    @Override
    public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, FloatingActionButton child, View directTargetChild, View target, int nestedScrollAxes) {
        return nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL;
    }

    @Override
    public void onNestedScroll(CoordinatorLayout coordinatorLayout, FloatingActionButton child, View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);
        if (child.getAlpha() == 0.0f && Math.abs(dyConsumed) > App.px24) {
            child.clearAnimation();
            child.animate()
                    .scaleX(1.0f)
                    .scaleY(1.0f)
                    .alpha(1.0f)
                    .setInterpolator(interpolator)
                    .start();
            child.setClickable(true);
        }
    }


    @Override
    public void onStopNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull FloatingActionButton child, @NonNull View target) {
        super.onStopNestedScroll(coordinatorLayout, child, target);
        if (currentRunnable != null) {
            handler.removeCallbacks(currentRunnable);
        }
        currentRunnable = () -> {
            child.clearAnimation();
            child.animate()
                    .scaleX(0.0f)
                    .scaleY(0.0f)
                    .alpha(0.0f)
                    .setInterpolator(interpolator)
                    .start();
            child.setClickable(false);
        };
        handler.postDelayed(currentRunnable, 1000);
    }

}
