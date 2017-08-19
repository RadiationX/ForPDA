package forpdateam.ru.forpda.utils;

import android.view.View;
import android.view.animation.AlphaAnimation;

/**
 * Created by isanechek on 8/11/17.
 */

public class AnimationHelper {
    public static void startAlphaAnimation(View v, long duration, int visibility) {
        AlphaAnimation alphaAnimation = (visibility == View.VISIBLE)
                ? new AlphaAnimation(0f, 1f)
                : new AlphaAnimation(1f, 0f);
        alphaAnimation.setDuration(duration);
        alphaAnimation.setFillAfter(true);
        v.startAnimation(alphaAnimation);
    }
}
