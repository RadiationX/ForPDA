package forpdateam.ru.forpda.tools;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.PopupWindow;

import java.util.ArrayList;
import java.util.List;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;

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
    private int primaryColor = Color.parseColor("#0277bd");

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
        popupView.findViewById(R.id.delete_button).setOnClickListener(v -> {
            int length = quickMessagePanel.getMessageField().getText().length();
            if (length > 0)
                quickMessagePanel.getMessageField().getText().delete(length - 1, length);
        });
        ViewPager viewPager = (ViewPager) popupView.findViewById(R.id.pager);
        List<BasePanelItem> viewList = new ArrayList<>();
        viewList.add(new CodesPanelItem(context, quickMessagePanel.getMessageField()));
        viewList.add(new SmilesPanelItem(context, quickMessagePanel.getMessageField()));
        viewPager.setAdapter(new MyPagerAdapter(viewList));
        TabLayout tabLayout = (TabLayout) popupView.findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager);
        popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.MATCH_PARENT, App.getKeyboardHeight(), false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            popupWindow.setElevation(App.px2);
        }
    }

    private class MyPagerAdapter extends PagerAdapter {
        List<BasePanelItem> pages = null;

        public MyPagerAdapter(List<BasePanelItem> pages) {
            this.pages = pages;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View v = pages.get(position);
            container.addView(v, 0);
            return v;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public int getCount() {
            return pages.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view.equals(object);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return pages.get(position).getTitle();
        }
    }

    private void hidePopup() {
        quickMessagePanel.getAdvancedButton().clearColorFilter();

        if (popupWindow.isShowing())
            popupWindow.dismiss();

        if (fragmentContainer.getPaddingBottom() != 0)
            fragmentContainer.setPadding(0, 0, 0, 0);

        if (stateListener != null)
            stateListener.onHide();

        quickMessagePanel.setCanScrolling(true);
    }

    private void showPopup() {
        quickMessagePanel.getAdvancedButton().setColorFilter(primaryColor);

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
