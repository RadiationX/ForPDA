package forpdateam.ru.forpda.fragments.qms;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.api.qms.models.QmsChatModel;
import forpdateam.ru.forpda.api.theme.editpost.models.AttachmentItem;
import forpdateam.ru.forpda.api.theme.editpost.models.EditPostForm;
import forpdateam.ru.forpda.client.RequestFile;
import forpdateam.ru.forpda.fragments.TabFragment;
import forpdateam.ru.forpda.fragments.qms.adapters.QmsChatAdapter;
import forpdateam.ru.forpda.messagepanel.MessagePanel;
import forpdateam.ru.forpda.messagepanel.attachments.AttachmentsPopup;
import forpdateam.ru.forpda.utils.IntentHandler;

/**
 * Created by radiationx on 25.08.16.
 */
public class QmsChatFragment extends TabFragment {
    public final static String USER_ID_ARG = "USER_ID_ARG";
    public final static String USER_AVATAR_ARG = "USER_AVATAR_ARG";
    public final static String THEME_ID_ARG = "THEME_ID_ARG";
    public final static String TAB_TAG_FOR_REMOVE = "TAB_TAG_FOR_REMOVE";
    private static final int PICK_IMAGE = 1228;
    private int userId;
    private String avatarUrl;
    private int themeId;
    private RecyclerView recyclerView;
    private MessagePanel messagePanel;
    private AttachmentsPopup attachmentsPopup;

    private QmsChatAdapter.OnItemClickListener onItemClickListener = message -> {
        Toast.makeText(getContext(), "ONCLICK " + message.getId(), Toast.LENGTH_SHORT).show();
    };
    private QmsChatAdapter.OnLongItemClickListener onLongItemClickListener = message -> {
        Toast.makeText(getContext(), "ON LONG CLICK " + message.getId(), Toast.LENGTH_SHORT).show();
    };

    private Subscriber<QmsChatModel> mainSubscriber = new Subscriber<>();
    private Subscriber<EditPostForm> formSubscriber = new Subscriber<>();
    private Subscriber<List<AttachmentItem>> attachmentSubscriber = new Subscriber<>();

    @Override
    public String getDefaultTitle() {
        return "Чат";
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userId = getArguments().getInt(USER_ID_ARG);
            themeId = getArguments().getInt(THEME_ID_ARG);
            avatarUrl = getArguments().getString(USER_AVATAR_ARG);
        }
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        initBaseView(inflater, container);
        baseInflateFragment(inflater, R.layout.fragment_qms_chat);
        recyclerView = (RecyclerView) findViewById(R.id.qms_chat);
        messagePanel = new MessagePanel(getContext(), (ViewGroup) findViewById(R.id.fragment_container), coordinatorLayout, false);
        messagePanel.setHeightChangeListener(newHeight -> recyclerView.setPadding(0, 0, 0, newHeight));
        attachmentsPopup = messagePanel.getAttachmentsPopup();

        attachmentsPopup.setAddOnClickListener(v -> pickImage());
        attachmentsPopup.setDeleteOnClickListener(v -> removeFiles());
        loadAttaches();


        viewsReady();
        tryShowAvatar();
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        //llm.setStackFromEnd(true);
        recyclerView.setLayoutManager(llm);

        return view;
    }


    @Override
    public boolean onBackPressed() {
        return messagePanel.onBackPressed();
    }

    @Override
    public void onResume() {
        super.onResume();
        messagePanel.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        messagePanel.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();
        messagePanel.onPause();
    }

    @Override
    public void hidePopupWindows() {
        super.hidePopupWindows();
        messagePanel.hidePopupWindows();
    }

    private void tryShowAvatar() {
        if (avatarUrl != null) {
            ImageLoader.getInstance().displayImage(avatarUrl, toolbarImageView);
            toolbarImageView.setVisibility(View.VISIBLE);
            toolbarImageView.setOnClickListener(view1 -> IntentHandler.handle("http://4pda.ru/forum/index.php?showuser=" + userId));
        } else {
            toolbarImageView.setVisibility(View.GONE);
        }
    }

    @Override
    public void loadData() {
        mainSubscriber.subscribe(Api.Qms().getChat(userId, themeId), this::onLoadChat, new QmsChatModel(), v -> loadData());
    }


    private void onLoadChat(QmsChatModel chat) {
        QmsChatAdapter adapter = new QmsChatAdapter(chat.getChatItemsList(), getContext());
        adapter.setOnItemClickListener(onItemClickListener);
        adapter.setOnLongItemClickListener(onLongItemClickListener);
        recyclerView.setAdapter(adapter);
        setTitle(chat.getTitle());
        setSubtitle(chat.getNick());
        if (avatarUrl == null) {
            avatarUrl = chat.getAvatarUrl();
            tryShowAvatar();
            //TabManager.getInstance().remove(getParentTag());
        }
        recyclerView.scrollToPosition(adapter.getItemCount() - 1);
    }

    public void pickImage() {
        /*Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image*//*");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        }
        startActivityForResult(intent, PICK_IMAGE)*/
        ;

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                //Display an error
                return;
            }
            Uri uri = data.getData();
            Log.d("SUKA", "DATA URI " + getFileName(data.getData()) + " : " + getContext().getContentResolver().getType(data.getData()));
            try {
                InputStream inputStream = null;
                String name = getFileName(data.getData());
                String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(name));

                if (uri.getScheme().equals("content")) {
                    inputStream = getContext().getContentResolver().openInputStream(uri);
                } else if (uri.getScheme().equals("file")) {
                    inputStream = new FileInputStream(new File(uri.getPath()));
                }

                List<RequestFile> files = new ArrayList<>();
                files.add(new RequestFile(name, mimeType, inputStream));
                uploadFiles(files);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            /*try {
                InputStream inputStream = getContext().getContentResolver().openInputStream(data.getData());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }*/
            //Now you can do whatever you want with your inpustream, save it as file, upload to a server, decode a bitmap...
        }
    }

    public String getFileName(Uri uri) {
        Log.d("suka", uri.getScheme() + " : " + getContext().getContentResolver().getType(uri));
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContext().getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            Log.d("suka", "res " + uri.getPath());
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private void loadAttaches() {
        formSubscriber.subscribe(Api.EditPost().loadForm(56965580), form -> attachmentsPopup.onLoadAttachments(form), new EditPostForm(), null);
    }

    public void uploadFiles(List<RequestFile> files) {
        attachmentsPopup.preUploadFiles(files);
        attachmentSubscriber.subscribe(Api.EditPost().uploadFiles(56965580, files), items -> attachmentsPopup.onUploadFiles(items), new ArrayList<>(), null);
    }

    public void removeFiles() {
        attachmentsPopup.preDeleteFiles();
        List<AttachmentItem> selectedFiles = attachmentsPopup.getSelected();
        attachmentSubscriber.subscribe(Api.EditPost().deleteFiles(56965580, selectedFiles), item -> attachmentsPopup.onDeleteFiles(item), selectedFiles, null);
    }
}
