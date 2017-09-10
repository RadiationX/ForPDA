package forpdateam.ru.forpda;

import android.app.Activity;
import android.graphics.Rect;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;

/**
 * Created by radiationx on 26.08.17.
 */

public class KeyboardUtil {
    private View decorView;
    private View contentView;
    private int suka = Build.VERSION_CODES.LOLLIPOP;

    public KeyboardUtil(Activity act, View contentView) {
        this.decorView = act.getWindow().getDecorView();
        this.contentView = contentView;

        //only required on newer android versions. it was working on API level 19
        if (Build.VERSION.SDK_INT >= suka) {
            decorView.getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener);
        }
    }

    public void enable() {
        if (Build.VERSION.SDK_INT >= suka) {
            decorView.getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener);
        }
    }

    public void disable() {
        if (Build.VERSION.SDK_INT >= suka) {
            decorView.getViewTreeObserver().removeOnGlobalLayoutListener(onGlobalLayoutListener);
        }
    }

    //a small helper to allow showing the editText focus
    ViewTreeObserver.OnGlobalLayoutListener onGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            int windowHeight = contentView.getRootView().getHeight();
            int fragmentContainerHeight = contentView.getRootView().findViewById(R.id.view_for_measure).getHeight();
            int statusBarHeight = App.getStatusBarHeight();
            int navigationBarHeight = App.getNavigationBarHeight();

            int newKeyboardHeight = windowHeight - fragmentContainerHeight - statusBarHeight - navigationBarHeight;


        /*if (lastKeyboardHeight == newKeyboardHeight)
            return;*/

            Log.d("FORPDA_LOG", "KeyboardUtil " + newKeyboardHeight + " = " + windowHeight + " - " + fragmentContainerHeight + " - " + statusBarHeight + " - " + navigationBarHeight);
            if (newKeyboardHeight !=0) {
                App.setKeyboardHeight(newKeyboardHeight);
                if (contentView.getPaddingBottom() != newKeyboardHeight) {
                    //set the padding of the contentView for the keyboard
                    Log.e("SUKA", "KeyboardUtil " + newKeyboardHeight);
                    contentView.setPadding(0, 0, 0, newKeyboardHeight);
                }
            } else {
                if (contentView.getPaddingBottom() != 0) {
                    //reset the padding of the contentView
                    Log.e("SUKA", "KeyboardUtil h " + 0);
                    contentView.setPadding(0, 0, 0, 0);
                }
            }

            /*Rect r = new Rect();
            //r will be populated with the coordinates of your view that area still visible.
            decorView.getWindowVisibleDisplayFrame(r);

            //get screen height and calculate the difference with the useable area from the r
            int height = decorView.getContext().getResources().getDisplayMetrics().heightPixels;
            int diff = height - r.bottom;
            //Log.e("SUKA", "GLL " + height + " : " + diff);

            //Фикс для мультиоконности
            if (diff < 0) {
                diff += r.top;
            }
            Log.e("SUKA", "new diff " + diff + "= " + height + " : " + r.bottom + " : " + r.top);
            if (diff < 0) {
                return;
            }
            //if it could be a keyboard add the padding to the view
            if (diff != 0) {
                // if the use-able screen height differs from the total screen height we assume that it shows a keyboard now
                //check if the padding is 0 (if yes set the padding for the keyboard)
                if (contentView.getPaddingBottom() != diff) {
                    //set the padding of the contentView for the keyboard
                    Log.e("SUKA", "KeyboardUtil " + diff);
                    contentView.setPadding(0, 0, 0, diff);
                }
            } else {
                //check if the padding is != 0 (if yes reset the padding)
                if (contentView.getPaddingBottom() != 0) {
                    //reset the padding of the contentView
                    Log.e("SUKA", "KeyboardUtil h " + 0);
                    contentView.setPadding(0, 0, 0, 0);
                }
            }*/
        }
    };


    /**
     * Helper to hide the keyboard
     *
     * @param act
     */
    public static void hideKeyboard(Activity act) {
        if (act != null && act.getCurrentFocus() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) act.getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(act.getCurrentFocus().getWindowToken(), 0);
        }
    }
}
