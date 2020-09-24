package forpdateam.ru.forpda.ui.views.messagepanel.advanced;

import android.content.Context;

import com.google.android.material.tabs.TabLayout;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.PopupWindow;

import java.util.ArrayList;
import java.util.List;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.ui.DimensionHelper;
import forpdateam.ru.forpda.ui.DimensionsProvider;
import forpdateam.ru.forpda.ui.views.messagepanel.MessagePanel;
import io.reactivex.disposables.CompositeDisposable;

/**
 * Created by radiationx on 07.01.17.
 */

public class AdvancedPopup {
    private PopupWindow popupWindow;
    private ViewGroup fragmentContainer;
    private boolean isShowingKeyboard = false;
    private StateListener stateListener;
    private MessagePanel messagePanel;
    private Context context;

    private DimensionsProvider dimensionsProvider = App.get().Di().getDimensionsProvider();
    private CompositeDisposable disposables = new CompositeDisposable();

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

        popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.MATCH_PARENT, dimensionsProvider.getDimensions().getSavedKeyboardHeight(), false);
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            popupWindow.setElevation(App.px2);
        }*/

        popupWindow.setOnDismissListener(() -> {
            dimensionsProvider.getDimensions().setFakeKeyboardShow(false);
            dimensionsProvider.update(dimensionsProvider.getDimensions());
        });

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
        disposables.add(
                dimensionsProvider
                        .observeDimensions()
                        .subscribe(dimensions -> {
                            if (messagePanel != null) {
                                messagePanel.post(() -> {
                                    if (messagePanel != null) {
                                        updateDimens(dimensions);
                                    }
                                });
                            }
                            updateDimens(dimensions);
                        })
        );
    }

    private void updateDimens(DimensionHelper.Dimensions dimensions) {
        if (popupWindow == null || messagePanel == null) {
            return;
        }
        if (dimensions.isKeyboardShow()) {
            popupWindow.setHeight(dimensions.getSavedKeyboardHeight());
            popupWindow.update();
            if (!isShowingKeyboard && popupWindow.isShowing()) {
                hidePopup();
            }
            isShowingKeyboard = true;
        } else if (isShowingKeyboard) {
            if (popupWindow.isShowing()) {
                hidePopup();
            }
            isShowingKeyboard = false;
        }
        messagePanel.setCanScrolling(!(isShowingKeyboard || popupWindow.isShowing()));
    }

    private void hidePopup() {
        DimensionHelper.Dimensions localDimensions = dimensionsProvider.getDimensions();
        messagePanel.getAdvancedButton().setImageDrawable(App.getVecDrawable(context, R.drawable.ic_add));

        if (popupWindow.isShowing()) {
            if (localDimensions.isFakeKeyboardShow()) {
                localDimensions.setFakeKeyboardShow(false);
                dimensionsProvider.update(localDimensions);
            }
            popupWindow.dismiss();
        }

        if (fragmentContainer.getPaddingBottom() != 0) {
            Log.d("SUKA", "hidePopup SET PADDING 0");
            fragmentContainer.setPadding(
                    fragmentContainer.getPaddingLeft(),
                    fragmentContainer.getPaddingTop(),
                    fragmentContainer.getPaddingRight(),
                    0
            );
        }

        if (stateListener != null)
            stateListener.onHide();

        messagePanel.setCanScrolling(true);
    }

    private void showPopup() {
        DimensionHelper.Dimensions localDimensions = dimensionsProvider.getDimensions();
        messagePanel.getAdvancedButton().setImageDrawable(App.getVecDrawable(context, R.drawable.ic_keyboard));

        if (!popupWindow.isShowing()) {
            if(!localDimensions.isFakeKeyboardShow()){
                localDimensions.setFakeKeyboardShow(true);
                dimensionsProvider.update(localDimensions);
            }
            popupWindow.showAtLocation(fragmentContainer, Gravity.BOTTOM, 0, 0);
        }

        Log.d("FORPDA_LOG", "showPopup " + localDimensions.getSavedKeyboardHeight() + " : " + fragmentContainer.getPaddingBottom() + " : " + isShowingKeyboard);
        //fragmentContainer.setPadding(0, 0, 0, App.getKeyboardHeight());

        if (!isShowingKeyboard) {
            if (fragmentContainer.getPaddingBottom() != localDimensions.getSavedKeyboardHeight()) {
                Log.d("SUKA", "showPopup SET PADDING " + localDimensions.getSavedKeyboardHeight());
                fragmentContainer.setPadding(
                        fragmentContainer.getPaddingLeft(),
                        fragmentContainer.getPaddingTop(),
                        fragmentContainer.getPaddingRight(),
                        localDimensions.getSavedKeyboardHeight()
                );
            }
        } else {
            Log.d("SUKA", "showPopup SET PADDING " + 0);
            fragmentContainer.setPadding(
                    fragmentContainer.getPaddingLeft(),
                    fragmentContainer.getPaddingTop(),
                    fragmentContainer.getPaddingRight(),
                    0
            );
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
        //fragmentContainer.getViewTreeObserver().addOnGlobalLayoutListener(globalLayoutListener);
    }

    public void onPause() {
        //fragmentContainer.getViewTreeObserver().removeOnGlobalLayoutListener(globalLayoutListener);
        hidePopup();
    }

    public void onDestroy() {
        //fragmentContainer.getViewTreeObserver().removeOnGlobalLayoutListener(globalLayoutListener);
        disposables.dispose();
        hidePopup();
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
