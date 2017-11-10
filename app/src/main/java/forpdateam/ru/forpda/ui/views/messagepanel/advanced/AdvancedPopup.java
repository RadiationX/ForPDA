package forpdateam.ru.forpda.ui.views.messagepanel.advanced;

import android.content.Context;
import android.os.Build;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.PopupWindow;

import java.util.ArrayList;
import java.util.List;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.ui.views.messagepanel.MessagePanel;

/**
 * Created by radiationx on 07.01.17.
 */

public class AdvancedPopup {
    private PopupWindow popupWindow;
    private ViewGroup fragmentContainer;
    private boolean isShowingKeyboard = false;
    private StateListener stateListener;
    private int newKeyboardHeight = 0;
    private int lastKeyboardHeight = 0;
    private MessagePanel messagePanel;
    private Context context;


    private ViewTreeObserver.OnGlobalLayoutListener globalLayoutListener = () -> {

        if (messagePanel == null || popupWindow == null) {
            return;
        }
        int windowHeight = fragmentContainer.getRootView().getHeight();
        int fragmentContainerHeight = fragmentContainer.getRootView().findViewById(R.id.view_for_measure).getHeight();
        int statusBarHeight = App.getStatusBarHeight();
        int navigationBarHeight = App.getNavigationBarHeight();

        newKeyboardHeight = windowHeight - fragmentContainerHeight - statusBarHeight - navigationBarHeight;


        /*if (lastKeyboardHeight == newKeyboardHeight)
            return;*/
        lastKeyboardHeight = newKeyboardHeight;

        Log.d("FORPDA_LOG", "TREE OBSERVER " + newKeyboardHeight + " = " + windowHeight + " - " + fragmentContainerHeight + " - " + statusBarHeight + " - " + navigationBarHeight+" : "+isShowingKeyboard+" : "+popupWindow.isShowing());
        if (newKeyboardHeight > 100) {
            App.setKeyboardHeight(newKeyboardHeight);
            popupWindow.setHeight(newKeyboardHeight);
            popupWindow.update();


            if (!isShowingKeyboard && popupWindow.isShowing())
                hidePopup();
            isShowingKeyboard = true;
            //Log.d("FORPDA_LOG", "isShowingKeyboard = TRUE, SetPadding " + newKeyboardHeight);
        } else if(isShowingKeyboard){
            if (popupWindow.isShowing()) {
                hidePopup();
            }
            isShowingKeyboard = false;
            //Log.d("FORPDA_LOG", "isShowingKeyboard = false");
        }
        //Log.d("SUKA", "OBSERVER SET PADDING 0");
        //fragmentContainer.setPadding(0, 0, 0, 0);
        /*if (newKeyboardHeight > 100) {
            int last = App.getKeyboardHeight();
            App.setKeyboardHeight(Math.max(last, newKeyboardHeight));
            if (App.getKeyboardHeight() != last) {
                popupWindow.setHeight(App.getKeyboardHeight());
                popupWindow.update();
            }
            if (popupWindow.isShowing() && fragmentContainer.getPaddingBottom() != 0)
                fragmentContainer.setPadding(0, 0, 0, 0);

            if (!isShowingKeyboard && popupWindow.isShowing())
                hidePopup();

            isShowingKeyboard = true;
            Log.d("FORPDA_LOG", "isShowingKeyboard = true");
            if(newKeyboardHeight>0&&newKeyboardHeight==App.getKeyboardHeight()){
                fragmentContainer.setPadding(0, 0, 0, newKeyboardHeight);
            }else if(newKeyboardHeight==0){
                fragmentContainer.setPadding(0, 0, 0, 0);
            }
        } else if (isShowingKeyboard) {
            if (popupWindow.isShowing())
                hidePopup();

            isShowingKeyboard = false;
            Log.d("FORPDA_LOG", "isShowingKeyboard = false");
        }*/

        messagePanel.setCanScrolling(!(isShowingKeyboard || popupWindow.isShowing()));
        //Log.d("FORPDA_LOG", "AFTER " + fragmentContainer.getPaddingBottom());
    };

    public AdvancedPopup(Context context, MessagePanel panel) {
        this.context = context;
        fragmentContainer = panel.getFragmentContainer();
        messagePanel = panel;

        View popupView = View.inflate(context, R.layout.message_panel_advanced, null);
        ViewPager viewPager = (ViewPager) popupView.findViewById(R.id.pager);

        List<BasePanelItem> viewList = new ArrayList<>();
        viewList.add(new CodesPanelItem(context, messagePanel));
        viewList.add(new SmilesPanelItem(context, messagePanel));
        viewPager.setAdapter(new MyPagerAdapter(viewList));

        ((TabLayout) popupView.findViewById(R.id.tab_layout)).setupWithViewPager(viewPager);

        popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.MATCH_PARENT, App.getKeyboardHeight(), false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            popupWindow.setElevation(App.px2);
        }

        popupView.findViewById(R.id.delete_button).setOnClickListener(v -> {
            EditText messageField = messagePanel.getMessageField();
            int selectionStart = messageField.getSelectionStart();
            int selectionEnd = messageField.getSelectionEnd();
            if (selectionEnd < selectionStart && selectionEnd != -1) {
                int c = selectionStart;
                selectionStart = selectionEnd;
                selectionEnd = c;
            }
            if (selectionStart != -1 && selectionStart != selectionEnd) {
                messageField.getText().delete(selectionStart, selectionEnd);
                return;
            }
            if (selectionStart > 0) {
                messageField.getText().delete(selectionStart - 1, selectionStart);
            }
            /*int length = messagePanel.getMessageField().getText().length();
            if (length > 0) {
                messagePanel.getMessageField().getText().delete(length - 1, length);
            }*/
        });

        messagePanel.addAdvancedOnClickListener(v -> {
            if (popupWindow.isShowing())
                hidePopup();
            else
                showPopup();
        });
    }

    private void hidePopup() {
        messagePanel.getAdvancedButton().setImageDrawable(App.getVecDrawable(context, R.drawable.ic_add));

        if (popupWindow.isShowing())
            popupWindow.dismiss();

        //Log.d("FORPDA_LOG", "hidePopup " + App.getKeyboardHeight() + " : " + lastKeyboardHeight + " : " + newKeyboardHeight);
        //fragmentContainer.setPadding(0, 0, 0, 0);
        if (fragmentContainer.getPaddingBottom() != 0){
            Log.d("SUKA", "hidePopup SET PADDING 0");
            //fragmentContainer.setPadding(0, 0, 0, 0);
            fragmentContainer.setPadding(fragmentContainer.getPaddingLeft(),
                    fragmentContainer.getPaddingTop(),
                    fragmentContainer.getPaddingRight(),
                    0);
        }

        if (stateListener != null)
            stateListener.onHide();

        messagePanel.setCanScrolling(true);
    }

    private void showPopup() {
        messagePanel.getAdvancedButton().setImageDrawable(App.getVecDrawable(context, R.drawable.ic_keyboard));

        if (!popupWindow.isShowing())
            popupWindow.showAtLocation(fragmentContainer, Gravity.BOTTOM, 0, 0);

        Log.d("FORPDA_LOG", "showPopup " + App.getKeyboardHeight()+" : "+fragmentContainer.getPaddingBottom()+" : "+isShowingKeyboard);
        //fragmentContainer.setPadding(0, 0, 0, App.getKeyboardHeight());
        if (!isShowingKeyboard){
            if(fragmentContainer.getPaddingBottom()!=App.getKeyboardHeight()){
                Log.d("SUKA", "showPopup SET PADDING "+ App.getKeyboardHeight());
                fragmentContainer.setPadding(fragmentContainer.getPaddingLeft(),
                        fragmentContainer.getPaddingTop(),
                        fragmentContainer.getPaddingRight(),
                        App.getKeyboardHeight());
            }
        }
        else {
            Log.d("SUKA", "showPopup SET PADDING "+ 0);
            fragmentContainer.setPadding(fragmentContainer.getPaddingLeft(),
                    fragmentContainer.getPaddingTop(),
                    fragmentContainer.getPaddingRight(),
                    0);
        }

        if (stateListener != null)
            stateListener.onShow();

        messagePanel.setCanScrolling(false);
    }


    public boolean onBackPressed() {
        if (!popupWindow.isShowing()) return false;
        hidePopup();
        return true;
    }

    public void onResume() {
        fragmentContainer.getViewTreeObserver().addOnGlobalLayoutListener(globalLayoutListener);
    }

    public void onPause() {
        fragmentContainer.getViewTreeObserver().removeOnGlobalLayoutListener(globalLayoutListener);
        hidePopup();
    }

    public void onDestroy() {
        fragmentContainer.getViewTreeObserver().removeOnGlobalLayoutListener(globalLayoutListener);
        hidePopup();
        popupWindow = null;
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

    private class MyPagerAdapter extends PagerAdapter {
        List<BasePanelItem> pages = null;

        MyPagerAdapter(List<BasePanelItem> pages) {
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
}
