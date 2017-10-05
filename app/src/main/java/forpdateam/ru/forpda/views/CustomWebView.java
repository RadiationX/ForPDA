package forpdateam.ru.forpda.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v4.view.MotionEventCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.webkit.WebView;

/**
 * Created by radiationx on 05.10.17.
 */

public class CustomWebView extends WebView {
    public CustomWebView(Context context) {
        super(context);
    }

    public CustomWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CustomWebView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public CustomWebView(Context context, AttributeSet attrs, int defStyleAttr, boolean privateBrowsing) {
        super(context, attrs, defStyleAttr, privateBrowsing);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        //Check pointer index to avoid -1 (error)
        if (MotionEventCompat.findPointerIndex(event, 0) == -1) {
            return super.onTouchEvent(event);
        }

        if (event.getPointerCount() >= 2) {
            requestDisallowInterceptTouchEvent(true);
        } else {
            requestDisallowInterceptTouchEvent(false);
        }

        return super.onTouchEvent(event);
    }

    @Override
    protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
        super.onOverScrolled(scrollX, scrollY, clampedX, clampedY);
        requestDisallowInterceptTouchEvent(true);


    }
}
