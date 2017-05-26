package forpdateam.ru.forpda.views.messagepanel.attachments;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.RequestFile;
import forpdateam.ru.forpda.api.theme.editpost.models.AttachmentItem;
import forpdateam.ru.forpda.api.theme.editpost.models.EditPostForm;
import forpdateam.ru.forpda.views.messagepanel.AutoFitRecyclerView;
import forpdateam.ru.forpda.views.messagepanel.MessagePanel;

/**
 * Created by radiationx on 09.01.17.
 */

public class AttachmentsPopup {
    private CustomBottomSheetDialog dialog;
    private MessagePanel messagePanel;
    private View bottomSheet;
    private AutoFitRecyclerView recyclerView;
    private AttachmentAdapter adapter = new AttachmentAdapter();
    private List<AttachmentItem> loadingItems = new ArrayList<>();

    private TextView noAttachments;
    private RelativeLayout textControls;
    private ImageButton addFile, deleteFile;
    private Button addToSpoiler, addToText;
    private FrameLayout progressOverlay;

    private OnInsertAttachmentListener insertAttachmentListener;

    public AttachmentsPopup(Context context, MessagePanel panel) {
        messagePanel = panel;
        dialog = new CustomBottomSheetDialog(context);
        dialog.setPeekHeight(App.getKeyboardHeight());

        bottomSheet = View.inflate(context, R.layout.message_panel_attachments, null);
        recyclerView = (AutoFitRecyclerView) bottomSheet.findViewById(R.id.auto_fit_recycler_view);
        progressOverlay = (FrameLayout) bottomSheet.findViewById(R.id.progress_overlay);

        noAttachments = (TextView) bottomSheet.findViewById(R.id.no_attachments_text);
        textControls = (RelativeLayout) bottomSheet.findViewById(R.id.text_controls);
        addFile = (ImageButton) bottomSheet.findViewById(R.id.add_file);
        deleteFile = (ImageButton) bottomSheet.findViewById(R.id.delete_file);
        addToSpoiler = (Button) bottomSheet.findViewById(R.id.add_to_spoiler);
        addToText = (Button) bottomSheet.findViewById(R.id.add_to_text);

        recyclerView.setColumnWidth(App.getInstance().dpToPx(112));
        recyclerView.setAdapter(adapter);

        /*addFile.setOnClickListener(v -> {
            uploadFiles();
        });*/
        //deleteFile.setOnClickListener(v -> adapter.deleteSelected());

        adapter.setOnSelectedListener(this::onSelected);
        adapter.setOnDataChangeListener(this::onDataChange);
        /*adapter.setReloadOnClickListener(item -> {
            if (item.getLoadState() == AttachmentItem.STATE_NOT_LOADED) {
                item.setLoadState(AttachmentItem.STATE_LOADING);
                adapter.notifyItemLoadResult(item);
                new Handler().postDelayed(() -> {
                    item.setLoadState(AttachmentItem.STATE_LOADED);
                    adapter.notifyItemLoadResult(item);
                }, 3000);
            }
        });*/
        onDataChange(0);

        addToText.setOnClickListener(v -> insertAttachment(false));
        addToSpoiler.setOnClickListener(v -> insertAttachment(true));

        messagePanel.addAttachmentsOnClickListener(v -> {
            if (bottomSheet != null && bottomSheet.getParent() != null && bottomSheet.getParent() instanceof ViewGroup) {
                ((ViewGroup) bottomSheet.getParent()).removeView(bottomSheet);
            }
            dialog.setContentView(bottomSheet);
            dialog.show();
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window window = dialog.getWindow();
            if (window != null) {
                window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                }
            }
        }
    }

    private void insertAttachment(boolean toSpoiler) {
        StringBuilder text = new StringBuilder();
        if (toSpoiler)
            text.append("[spoiler]");
        for (AttachmentItem item : adapter.getSelected()) {
            if (insertAttachmentListener != null) {
                text.append(insertAttachmentListener.onInsert(item));
            } else {
                text.append("[attachment=").append(item.getId()).append(":").append(item.getName()).append("]");
            }
        }
        if (toSpoiler)
            text.append("[/spoiler]");
        messagePanel.insertText(text.toString());
        adapter.unSelectItems();
    }

    private void onDataChange(int count) {
        if (count > 0) {
            noAttachments.setText("Вложений: " + count);
            //dialog.setPeekHeight(App.getKeyboardHeight());
        } else {
            noAttachments.setText("Нет вложений");
            //dialog.setPeekHeight(App.px48);
        }
    }

    private void onSelected(AttachmentItem item, int index, int selected) {
        int firstGroup = selected > 0 ? View.GONE : View.VISIBLE;
        int secondGroup = selected > 0 ? View.VISIBLE : View.GONE;

        if (noAttachments.getVisibility() != firstGroup)
            noAttachments.setVisibility(firstGroup);
        if (addFile.getVisibility() != firstGroup)
            addFile.setVisibility(firstGroup);
        if (textControls.getVisibility() != secondGroup)
            textControls.setVisibility(secondGroup);
        if (deleteFile.getVisibility() != secondGroup)
            deleteFile.setVisibility(secondGroup);

        tryLockControls(!adapter.containNotLoaded());
    }

    private void tryLockControls(boolean enable) {
        if (textControls.getVisibility() == View.VISIBLE) {
            addToSpoiler.setEnabled(enable);
            addToText.setEnabled(enable);
            deleteFile.setEnabled(enable);
        }
    }

    public void setAddOnClickListener(View.OnClickListener listener) {
        addFile.setOnClickListener(listener);
    }

    public void setDeleteOnClickListener(View.OnClickListener listener) {
        deleteFile.setOnClickListener(listener);
    }

    public void onLoadAttachments(EditPostForm form) {
        adapter.add(form.getAttachments());
    }

    public void preUploadFiles(List<RequestFile> files) {
        for (RequestFile file : files) {
            AttachmentItem item = new AttachmentItem(file.getFileName());
            Log.e("FORPDA_LOG", "ADD LOADING ITEM " + item);
            adapter.add(item);
            loadingItems.add(item);
        }
    }

    public void onUploadFiles(List<AttachmentItem> items) {
        for (AttachmentItem item : items) {
            AttachmentItem loadingItem = getItemByName(item.getName());
            Log.e("FORPDA_LOG", "LOADING ITEM " + loadingItem + " : " + item);
            if (item.getLoadState() == AttachmentItem.STATE_NOT_LOADED) {
                adapter.removeItem(loadingItem);
                //SHOW ERROR
            } else {
                adapter.replaceItem(loadingItem, item);
            }
            loadingItems.remove(loadingItem);
        }
    }

    public void preDeleteFiles() {
        //block ui
        progressOverlay.setVisibility(View.VISIBLE);
        tryLockControls(false);
    }

    public void setAttachments(Collection<AttachmentItem> items) {
        adapter.clear();
        adapter.add(items);
    }

    public void clearAttachments() {
        adapter.clear();
    }


    public void onDeleteFiles(List<AttachmentItem> deletedItems) {
        //unblock ui
        Log.e("SUKA", "ON DELETE FILES "+deletedItems);
        for (AttachmentItem item : deletedItems) {
            Log.e("SUKA", "DELETED FILE "+item);
            messagePanel.setText(messagePanel.getMessage().replaceAll("\\[attachment=['\"]?" + item.getId() + ":[^\\]]*?]",""));
        }
        progressOverlay.setVisibility(View.GONE);
        adapter.deleteSelected();
    }

    public List<AttachmentItem> getAttachments() {
        return adapter.getItems();
    }

    public List<AttachmentItem> getSelected() {
        return adapter.getSelected();
    }

    private AttachmentItem getItemByName(String name) {
        for (AttachmentItem item : loadingItems) {
            if (item.getName().equals(name)) return item;
        }
        return null;
    }

    public void setInsertAttachmentListener(OnInsertAttachmentListener insertAttachmentListener) {
        this.insertAttachmentListener = insertAttachmentListener;
    }

    public interface OnInsertAttachmentListener {
        String onInsert(AttachmentItem item);
    }
}
