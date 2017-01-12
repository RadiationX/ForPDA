package forpdateam.ru.forpda.messagepanel.attachments;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.api.theme.editpost.models.AttachmentItem;
import forpdateam.ru.forpda.api.theme.editpost.models.EditPostForm;
import forpdateam.ru.forpda.api.theme.models.ThemePage;
import forpdateam.ru.forpda.client.RequestFile;
import forpdateam.ru.forpda.fragments.TabFragment;
import forpdateam.ru.forpda.messagepanel.AutoFitRecyclerView;
import forpdateam.ru.forpda.messagepanel.MessagePanel;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

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
        addToSpoiler = (Button) bottomSheet.findViewById(R.id.add_to_spoiler);
        addToText = (Button) bottomSheet.findViewById(R.id.add_to_text);

        recyclerView.setColumnWidth(App.getInstance().dpToPx(112));
        recyclerView.setAdapter(adapter);

        /*addFile.setOnClickListener(v -> {
            uploadFile();
        });*/
        //deleteFile.setOnClickListener(v -> adapter.removeSelected());

        adapter.setOnSelectedListener(this::onSelected);
        adapter.setOnDataChangeListener(this::onDataChange);
        adapter.setReloadOnClickListener(item -> {
            if (item.getLoadState() == AttachmentItem.STATE_NOT_LOADED) {
                item.setLoadState(AttachmentItem.STATE_LOADING);
                adapter.notifyItemLoadResult(item);
                new Handler().postDelayed(() -> {
                    item.setLoadState(AttachmentItem.STATE_LOADED);
                    adapter.notifyItemLoadResult(item);
                }, 3000);
            }
        });
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

        if (textControls.getVisibility() == View.VISIBLE) {
            boolean enabledTextControls = !adapter.containNotLoaded();
            addToSpoiler.setEnabled(enabledTextControls);
            addToText.setEnabled(enabledTextControls);
        }
    }

    public void setAddOnClickListener(View.OnClickListener listener) {
        addFile.setOnClickListener(listener);
    }

    public void setDeleteOnClickListener(View.OnClickListener listener) {
        deleteFile.setOnClickListener(listener);
    }

    public void onLoadAttachments(EditPostForm form) {
        adapter.add(form.getLoadedAttachments());
    }

    public void preUploadFile(String name) {
        AttachmentItem item = new AttachmentItem(name);
        adapter.add(item);
        loadingItems.add(item);
    }

    public void onUploadFile(EditPostForm form) {
        AttachmentItem item = form.getLoadedAttachments().get(0);
        AttachmentItem loadingItem = getItemByName(item.getName());
        if (item.getLoadState() == AttachmentItem.STATE_NOT_LOADED){
            adapter.removeItem(loadingItem);
            //SHOW ERROR
        }else{
            adapter.replaceItem(loadingItem, item);
        }
        loadingItems.remove(loadingItem);
    }

    public void preDeleteFiles(){
        //block ui

    }

    public void onDeleteFiles(EditPostForm form) {
        //unblock ui

    }

    public List<AttachmentItem> getSelected(){
        return adapter.getSelected();
    }

    private AttachmentItem getItemByName(String name) {
        for (AttachmentItem item : loadingItems) {
            if (item.getName().equals(name)) return item;
        }
        return null;
    }


    Subscriber<EditPostForm> mainSubscriber = new Subscriber<>();




    public class Subscriber<T> {
        public Disposable subscribe(@NonNull Observable<T> observable, @NonNull Consumer<T> onNext, @NonNull T onErrorReturn) {
            return subscribe(observable, onNext, onErrorReturn, null);
        }

        public Disposable subscribe(@NonNull Observable<T> observable, @NonNull Consumer<T> onNext, @NonNull T onErrorReturn, View.OnClickListener onErrorAction) {
            return observable.onErrorReturn(throwable -> {
                //handleErrorRx(throwable, onErrorAction);
                throwable.printStackTrace();
                return onErrorReturn;
            })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(onNext/*, throwable -> handleErrorRx(throwable, onErrorAction)*/);
        }
    }
}
