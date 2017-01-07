package forpdateam.ru.forpda;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.PopupWindow;

/**
 * Created by radiationx on 07.01.17.
 */

public class AdvancedInputWindow {
    private PopupWindow popupWindow;
    private ViewGroup fragmentContainer;
    private boolean isShowingKeyboard = false;
    private StateListener stateListener;
    private int newKeyboardHeight = 0;
    private QuickMessagePanel quickMessagePanel;

    private ViewTreeObserver.OnGlobalLayoutListener globalLayoutListener = () -> {
        newKeyboardHeight = fragmentContainer.getRootView().getHeight() - fragmentContainer.getHeight() - App.getStatusBarHeight();
        Log.d("SUKA", "TREE OBSERVER " + newKeyboardHeight + " : " + fragmentContainer.getRootView().getHeight() + " : " + fragmentContainer.getHeight() + " : " + App.getStatusBarHeight());
        if (newKeyboardHeight > 100) {
            int last = App.getKeyboardHeight();
            App.setKeyboardHeight(newKeyboardHeight);
            if (App.getKeyboardHeight() != last) {
                popupWindow.setHeight(App.getKeyboardHeight());
                popupWindow.update();
            }
            if (popupWindow.isShowing() && fragmentContainer.getPaddingBottom() != 0)
                fragmentContainer.setPadding(0, 0, 0, 0);

            if (!isShowingKeyboard && popupWindow.isShowing())
                hidePopup();

            isShowingKeyboard = true;
        } else if (isShowingKeyboard) {
            if (popupWindow.isShowing())
                hidePopup();

            isShowingKeyboard = false;
        }
        quickMessagePanel.setCanScrolling(!(isShowingKeyboard || popupWindow.isShowing()));
        Log.d("SUKA", "AFTER " + fragmentContainer.getPaddingBottom());
    };

    public AdvancedInputWindow(Context context, ViewGroup container, QuickMessagePanel qmp) {
        fragmentContainer = container;
        quickMessagePanel = qmp;
        quickMessagePanel.addAdvancedOnClickListener(v -> {
            if (popupWindow.isShowing())
                hidePopup();
            else
                showPopup();
        });
        View popupView = View.inflate(context, R.layout.testpopup, null);
        popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.MATCH_PARENT, App.getKeyboardHeight(), false);
    }

    private void hidePopup() {
        if (popupWindow.isShowing())
            popupWindow.dismiss();

        if (fragmentContainer.getPaddingBottom() != 0)
            fragmentContainer.setPadding(0, 0, 0, 0);

        if (stateListener != null)
            stateListener.onHide();

        quickMessagePanel.setCanScrolling(true);
    }

    private void showPopup() {
        if (!popupWindow.isShowing())
            popupWindow.showAtLocation(fragmentContainer, Gravity.BOTTOM, 0, 0);

        if (!isShowingKeyboard && fragmentContainer.getPaddingBottom() != App.getKeyboardHeight())
            fragmentContainer.setPadding(0, 0, 0, App.getKeyboardHeight());

        if (stateListener != null)
            stateListener.onShow();

        quickMessagePanel.setCanScrolling(false);
    }


    public boolean onBackPressed() {
        if (!popupWindow.isShowing()) return false;
        hidePopup();
        return true;
    }

    public void onResume() {
        fragmentContainer.getViewTreeObserver().addOnGlobalLayoutListener(globalLayoutListener);
    }

    public void onDestroy() {
        hidePopup();
        popupWindow = null;
        fragmentContainer.getViewTreeObserver().removeOnGlobalLayoutListener(globalLayoutListener);
    }

    public void onPause() {
        hidePopup();
        fragmentContainer.getViewTreeObserver().removeOnGlobalLayoutListener(globalLayoutListener);
    }

    public void hidePopupWindows() {
        hidePopup();
    }

    public void setStateListener(StateListener stateListener) {
        this.stateListener = stateListener;
    }

    public interface StateListener {
        void onShow();

        void onHide();
    }
}
