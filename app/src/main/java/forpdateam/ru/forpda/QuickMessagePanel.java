package forpdateam.ru.forpda;

import android.animation.LayoutTransition;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.EditText;
import android.widget.ImageButton;

import java.util.ArrayList;
import java.util.List;

import forpdateam.ru.forpda.utils.MessagePanelBehavior;

/**
 * Created by radiationx on 07.01.17.
 */

public class QuickMessagePanel extends CardView {
    private ImageButton advancedButton, attachmentsButton, sendButton;
    private List<View.OnClickListener> advancedListeners = new ArrayList<>(), attachmentsListeners = new ArrayList<>(), sendListeners = new ArrayList<>();
    private EditText messageField;
    private MessagePanelBehavior panelBehavior;

    public QuickMessagePanel(Context context) {
        super(context);
        init();
    }

    public QuickMessagePanel(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public QuickMessagePanel(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.quick_message_panel, this);
        advancedButton = (ImageButton) findViewById(R.id.button_advanced_input);
        attachmentsButton = (ImageButton) findViewById(R.id.button_attachments);
        sendButton = (ImageButton) findViewById(R.id.button_send);
        messageField = (EditText) findViewById(R.id.message_field);
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
        ValueAnimator va = new ValueAnimator();
        va.setDuration(335);
        va.setInterpolator(new AccelerateDecelerateInterpolator());
        va.addUpdateListener(animation -> {
            setRadius((Integer) animation.getAnimatedValue());
        });
        addSendOnClickListener(v -> {
            if (va.isRunning()) return;
            if (getRadius() == 0) {
                va.setIntValues(0, App.px24);
            } else {
                va.setIntValues(App.px24, 0);
            }
            va.start();
        });
        panelBehavior = new MessagePanelBehavior();
        setClipToPadding(true);
        setRadius(App.px24);
        CoordinatorLayout.LayoutParams params = new CoordinatorLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setBehavior(panelBehavior);
        params.gravity = Gravity.BOTTOM;
        params.setMargins(App.px8, App.px8, App.px8, App.px8);
        setLayoutParams(params);


        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());

        addAttachmentsOnClickListener(v -> {
            bottomSheetDialog.setContentView(R.layout.test_bottomsheet);
            bottomSheetDialog.show();
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
}
