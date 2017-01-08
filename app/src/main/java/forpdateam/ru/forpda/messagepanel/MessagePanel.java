package forpdateam.ru.forpda.messagepanel;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.CardView;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import java.util.ArrayList;
import java.util.List;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.messagepanel.advanced.AdvancedPopupWindow;
import forpdateam.ru.forpda.utils.SimpleTextWatcher;

/**
 * Created by radiationx on 07.01.17.
 */

@SuppressLint("ViewConstructor")
public class MessagePanel extends CardView {
    private ImageButton advancedButton, attachmentsButton, sendButton;
    private List<View.OnClickListener> advancedListeners = new ArrayList<>(), attachmentsListeners = new ArrayList<>(), sendListeners = new ArrayList<>();
    private EditText messageField;
    private MessagePanelBehavior panelBehavior;
    private AdvancedPopupWindow advancedPopupWindow;
    private ViewGroup fragmentContainer;
    private int lastHeight = 0;
    private HeightChangeListener heightChangeListener;
    public int primaryColor = Color.parseColor("#0277bd");

    public MessagePanel(Context context, ViewGroup fragmentContainer) {
        super(context);
        this.fragmentContainer = fragmentContainer;
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.quick_message_panel, this);
        advancedButton = (ImageButton) findViewById(R.id.button_advanced_input);
        attachmentsButton = (ImageButton) findViewById(R.id.button_attachments);
        sendButton = (ImageButton) findViewById(R.id.button_send);
        messageField = (EditText) findViewById(R.id.message_field);
        panelBehavior = new MessagePanelBehavior();
        CoordinatorLayout.LayoutParams params = new CoordinatorLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setBehavior(panelBehavior);
        params.gravity = Gravity.BOTTOM;
        params.setMargins(App.px8, App.px8, App.px8, App.px8);
        setLayoutParams(params);
        setClipToPadding(true);
        setRadius(App.px24);
        //На случай, когда добавляются несколько слушателей
        advancedButton.setOnClickListener(v -> {
            for (OnClickListener listener : advancedListeners)
                listener.onClick(v);
        });
        attachmentsButton.setOnClickListener(v -> {
            for (OnClickListener listener : attachmentsListeners)
                listener.onClick(v);
        });
        sendButton.setOnClickListener(v -> {
            for (OnClickListener listener : sendListeners)
                listener.onClick(v);
        });

        /*BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());

        addAttachmentsOnClickListener(v -> {
            bottomSheetDialog.setContentView(R.layout.test_bottomsheet);
            bottomSheetDialog.show();
        });*/
        advancedPopupWindow = new AdvancedPopupWindow(getContext(), this);
        lastHeight = getHeight() + App.px16;
        addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            if (heightChangeListener == null) return;
            int newHeight = getHeight() + App.px16;
            if (newHeight != lastHeight) {
                heightChangeListener.onChangedHeight(newHeight);
            }
        });

        messageField.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    if (sendButton.getColorFilter() == null)
                        sendButton.setColorFilter(primaryColor);
                } else {
                    if (sendButton.getColorFilter() != null)
                        sendButton.clearColorFilter();
                }
            }
        });
    }


    public void addAdvancedOnClickListener(View.OnClickListener listener) {
        advancedListeners.add(listener);
    }

    public void addAttachmentsOnClickListener(View.OnClickListener listener) {
        attachmentsListeners.add(listener);
    }

    public void addSendOnClickListener(View.OnClickListener listener) {
        sendListeners.add(listener);
    }

    public void removeAdvancedOnClickListener(View.OnClickListener listener) {
        advancedListeners.remove(listener);
    }

    public void removeAttachmentsOnClickListener(View.OnClickListener listener) {
        attachmentsListeners.remove(listener);
    }

    public void removeSendOnClickListener(View.OnClickListener listener) {
        sendListeners.remove(listener);
    }

    public ImageButton getAdvancedButton() {
        return advancedButton;
    }

    public ImageButton getAttachmentsButton() {
        return attachmentsButton;
    }

    public ImageButton getSendButton() {
        return sendButton;
    }

    public EditText getMessageField() {
        return messageField;
    }

    public void setCanScrolling(boolean canScrolling) {
        panelBehavior.setCanScrolling(canScrolling);
    }

    public ViewGroup getFragmentContainer() {
        return fragmentContainer;
    }

    public interface HeightChangeListener {
        void onChangedHeight(int newHeight);
    }

    public void setHeightChangeListener(HeightChangeListener heightChangeListener) {
        this.heightChangeListener = heightChangeListener;
    }

    public boolean onBackPressed() {
        return advancedPopupWindow.onBackPressed();
    }

    public void onResume() {
        advancedPopupWindow.onResume();
    }

    public void onDestroy() {
        advancedPopupWindow.onDestroy();
    }

    public void onPause() {
        advancedPopupWindow.onPause();
    }

    public void hidePopupWindows() {
        advancedPopupWindow.hidePopupWindows();
    }
}
