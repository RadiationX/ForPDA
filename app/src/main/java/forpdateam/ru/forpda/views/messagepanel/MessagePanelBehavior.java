package forpdateam.ru.forpda.views.messagepanel;

import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.CardView;
import android.view.View;

import forpdateam.ru.forpda.App;

/**
 * Created by radiationx on 07.01.17.
 */

public class MessagePanelBehavior extends CoordinatorLayout.Behavior<CardView> {
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


    public void setCanScrolling(boolean canScrolling) {
        this.canScrolling = canScrolling;
    }


    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, CardView child, View dependency) {
        return dependency instanceof AppBarLayout;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, CardView child, View dependency) {
        if (!canScrolling) return false;
        float percent = 1.0f - ((float) -dependency.getTop() / (float) dependency.getMeasuredHeight());
        int scrolled = (int) ((child.getMeasuredHeight() + (2 * App.px8)) * percent);
        child.setTranslationY(scrolled);
        return true;
    }
}
