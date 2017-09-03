package forpdateam.ru.forpda.utils;

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

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;

/**
 * Created by radiationx on 26.07.17.
 */

public class FabOnScroll extends FloatingActionButton.Behavior {
    private Handler handler = new Handler();
    private Runnable currentRunnable;
    private Interpolator interpolator = new AccelerateDecelerateInterpolator();

    public FabOnScroll(Context context) {
        super(context, null);
    }

    public FabOnScroll(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, FloatingActionButton child, View directTargetChild, View target, int nestedScrollAxes) {
        //Log.d("SUKA", "FabOnScroll onStartNestedScroll " + nestedScrollAxes);
        return nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL;
    }

    @Override
    public void onNestedPreScroll(CoordinatorLayout coordinatorLayout, FloatingActionButton child, View target, int dx, int dy, int[] consumed) {
        super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed);
        //Log.d("SUKA", "FabOnScroll onNestedPreScroll" + consumed[1] + " : " + dy);
        if (child.getAlpha() == 0.0f && Math.abs(dy) > App.px24) {
            child.setImageDrawable(App.getAppDrawable(child.getContext(), dy > 0 ? R.drawable.ic_arrow_down : R.drawable.ic_arrow_up));
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
    public boolean onNestedFling(CoordinatorLayout coordinatorLayout, FloatingActionButton child, View target, float velocityX, float velocityY, boolean consumed) {
        //Log.d("SUKA", "FabOnScroll onNestedFling" + velocityY + " : " + consumed);
        return super.onNestedFling(coordinatorLayout, child, target, velocityX, velocityY, consumed);
    }

    @Override
    public void onNestedScroll(CoordinatorLayout coordinatorLayout, FloatingActionButton child, View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);
        //Log.d("SUKA", "FabOnScroll onNestedScroll " + dyConsumed + " : " + dyUnconsumed + " : " + App.px24);
        if (child.getAlpha() == 0.0f && Math.abs(dyUnconsumed) > App.px24) {
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
