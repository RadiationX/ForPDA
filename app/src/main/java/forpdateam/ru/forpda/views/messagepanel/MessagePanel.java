package forpdateam.ru.forpda.views.messagepanel;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.CardView;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.List;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.theme.editpost.models.AttachmentItem;
import forpdateam.ru.forpda.utils.SimpleTextWatcher;
import forpdateam.ru.forpda.views.messagepanel.advanced.AdvancedPopup;
import forpdateam.ru.forpda.views.messagepanel.attachments.AttachmentsPopup;

/**
 * Created by radiationx on 07.01.17.
 */

@SuppressLint("ViewConstructor")
public class MessagePanel extends CardView {
    private ImageButton advancedButton, attachmentsButton, sendButton;
    private List<View.OnClickListener> advancedListeners = new ArrayList<>(), attachmentsListeners = new ArrayList<>(), sendListeners = new ArrayList<>();
    private EditText messageField;
    private MessagePanelBehavior panelBehavior;
    private AdvancedPopup advancedPopup;
    private AttachmentsPopup attachmentsPopup;
    private ViewGroup fragmentContainer;
    private ProgressBar sendProgress;
    private ProgressBar formProgress;
    private int lastHeight = 0;
    private HeightChangeListener heightChangeListener;
    public int primaryColor = Color.parseColor("#0277bd");
    private boolean fullForm = false;

    public MessagePanel(Context context, ViewGroup fragmentContainer, ViewGroup targetContainer, boolean fullForm) {
        super(context);
        this.fragmentContainer = fragmentContainer;
        this.fullForm = fullForm;
        init();
        targetContainer.addView(this, targetContainer.getChildCount() - 1);
        onCreatePanel();
    }

    private void init() {
        inflate(getContext(), fullForm ? R.layout.message_panel_full : R.layout.message_panel_quick, this);
        advancedButton = (ImageButton) findViewById(R.id.button_advanced_input);
        attachmentsButton = (ImageButton) findViewById(R.id.button_attachments);
        sendButton = (ImageButton) findViewById(R.id.button_send);
        messageField = (EditText) findViewById(R.id.message_field);
        sendProgress = (ProgressBar) findViewById(R.id.send_progress);
        formProgress = (ProgressBar) findViewById(R.id.form_load_progress);
        panelBehavior = new MessagePanelBehavior();
        CoordinatorLayout.LayoutParams params = new CoordinatorLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, fullForm ? ViewGroup.LayoutParams.MATCH_PARENT : ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setBehavior(panelBehavior);
        params.gravity = Gravity.BOTTOM;
        if (!fullForm)
            params.setMargins(App.px8, App.px8, App.px8, App.px8);
        setLayoutParams(params);
        setClipToPadding(true);
        setRadius(fullForm ? 0 : App.px24);
        setPreventCornerOverlap(false);
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
                    if (sendButton.getColorFilter() == null) {
                        sendButton.setColorFilter(primaryColor);
                    }
                } else {
                    if (sendButton.getColorFilter() != null) {
                        sendButton.clearColorFilter();
                    }
                }
            }
        });
        messageField.setTypeface(Typeface.MONOSPACE);
    }

    public ProgressBar getFormProgress(){
        return formProgress;
    }

    public void setProgressState(boolean state) {
        sendProgress.setVisibility(state ? VISIBLE : GONE);
        sendButton.setVisibility(state ? GONE : VISIBLE);
    }

    public void show() {
        this.setTranslationY(0);
    }

    public void setText(String text) {
        messageField.setText(text);
    }

    public boolean insertText(String text) {
        return insertText(text, null);
    }

    public boolean insertText(String startText, String endText) {
        show();
        int selectionStart = messageField.getSelectionStart();
        int selectionEnd = messageField.getSelectionEnd();
        if (selectionEnd < selectionStart && selectionEnd != -1) {
            int c = selectionStart;
            selectionStart = selectionEnd;
            selectionEnd = c;
        }
        if (endText != null && selectionStart != -1 && selectionStart != selectionEnd) {
            messageField.getText().insert(selectionStart, startText);
            messageField.getText().insert(selectionEnd + endText.length() - 1, endText);
            return true;
        }
        messageField.getText().insert(selectionStart, startText);
        return false;
    }

    public String getMessage() {
        return messageField.getText().toString();
    }

    public void clearMessage() {
        messageField.setText("");
    }

    public void clearAttachments() {
        attachmentsPopup.clearAttachments();
    }

    public List<AttachmentItem> getAttachments() {
        return attachmentsPopup.getAttachments();
    }

    private void onCreatePanel() {
        attachmentsPopup = new AttachmentsPopup(getContext(), this);
        advancedPopup = new AdvancedPopup(getContext(), this);
    }

    public AttachmentsPopup getAttachmentsPopup() {
        return attachmentsPopup;
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
        return advancedPopup == null || advancedPopup.onBackPressed();
    }

    public void onResume() {
        if (advancedPopup != null)
            advancedPopup.onResume();
    }

    public void onDestroy() {
        if (advancedPopup != null)
            advancedPopup.onDestroy();
    }

    public void onPause() {
        if (advancedPopup != null)
            advancedPopup.onPause();
    }

    public void hidePopupWindows() {
        if (advancedPopup != null)
            advancedPopup.hidePopupWindows();
    }
}
