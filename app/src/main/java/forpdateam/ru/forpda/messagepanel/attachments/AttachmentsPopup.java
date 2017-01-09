package forpdateam.ru.forpda.messagepanel.attachments;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.messagepanel.AutoFitRecyclerView;
import forpdateam.ru.forpda.messagepanel.MessagePanel;

/**
 * Created by radiationx on 09.01.17.
 */

public class AttachmentsPopup {
    private CustomBottomSheetDialog dialog;
    private MessagePanel messagePanel;
    private View bottomSheet;
    private AutoFitRecyclerView recyclerView;
    AttachmentAdapter adapter = new AttachmentAdapter();

    private TextView noAttachments;
    private RelativeLayout textControls;
    private ImageButton addFile, deleteFile;

    public AttachmentsPopup(Context context, MessagePanel panel) {
        messagePanel = panel;
        dialog = new CustomBottomSheetDialog(context);
        dialog.setPeekHeight(App.getKeyboardHeight());

        bottomSheet = View.inflate(context, R.layout.test_bottomsheet, null);
        recyclerView = (AutoFitRecyclerView) bottomSheet.findViewById(R.id.auto_fit_recycler_view);

        noAttachments = (TextView) bottomSheet.findViewById(R.id.no_attachments_text);
        textControls = (RelativeLayout) bottomSheet.findViewById(R.id.text_controls);
        addFile = (ImageButton) bottomSheet.findViewById(R.id.add_file);
        deleteFile = (ImageButton) bottomSheet.findViewById(R.id.delete_file);

        recyclerView.setColumnWidth(App.getInstance().dpToPx(96));
        recyclerView.setAdapter(adapter);

        addFile.setOnClickListener(v -> adapter.add(new AttachmentItem("")));
        deleteFile.setOnClickListener(v -> adapter.removeSelected());

        adapter.setOnSelectedListener(this::onSelected);
        adapter.setOnDataChangeListener(this::onDataChange);
        onDataChange(0);


        messagePanel.addAttachmentsOnClickListener(v -> {
            if (bottomSheet != null && bottomSheet.getParent() != null && bottomSheet.getParent() instanceof ViewGroup) {
                ((ViewGroup) bottomSheet.getParent()).removeView(bottomSheet);
            }
            dialog.setContentView(bottomSheet);
            dialog.show();
        });
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
    }
}
