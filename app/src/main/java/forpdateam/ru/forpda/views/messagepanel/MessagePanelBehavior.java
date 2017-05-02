package forpdateam.ru.forpda.views.messagepanel;

import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.CardView;
import android.view.View;

import forpdateam.ru.forpda.App;

/**
 * Created by radiationx on 07.01.17.
 */

public class MessagePanelBehavior extends CoordinatorLayout.Behavior<CardView> {
    private int scrolled = 0;
    private boolean canScrolling = true;

    public MessagePanelBehavior() {
        super();
    }

    @Override
    public boolean onStartNestedScroll(final CoordinatorLayout coordinatorLayout, final CardView child,
                                       final View directTargetChild, final View target, final int nestedScrollAxes) {
        if (!canScrolling)
            child.setTranslationY(0);
        return canScrolling;
    }

    @Override
    public void onNestedScroll(final CoordinatorLayout coordinatorLayout,
                               final CardView child,
                               final View target, final int dxConsumed, final int dyConsumed,
                               final int dxUnconsumed, final int dyUnconsumed) {
        super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);
        if (!canScrolling) return;
        scrolled += dyConsumed + dyUnconsumed;
        scrolled = Math.max(scrolled, -child.getMeasuredHeight() - (2 * App.px8));
        scrolled = Math.min(scrolled, 0);
        child.setTranslationY(-(float) scrolled);
    }

    public void setCanScrolling(boolean canScrolling) {
        this.canScrolling = canScrolling;
    }
}
