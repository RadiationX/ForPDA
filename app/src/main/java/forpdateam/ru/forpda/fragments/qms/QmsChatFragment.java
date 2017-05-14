package forpdateam.ru.forpda.fragments.qms;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.AppCompatAutoCompleteTextView;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.webkit.JavascriptInterface;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;

import biz.source_code.miniTemplator.MiniTemplator;
import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.RequestFile;
import forpdateam.ru.forpda.api.qms.models.QmsChatModel;
import forpdateam.ru.forpda.api.qms.models.QmsMessage;
import forpdateam.ru.forpda.api.theme.editpost.models.AttachmentItem;
import forpdateam.ru.forpda.fragments.TabFragment;
import forpdateam.ru.forpda.fragments.qms.adapters.QmsChatAdapter;
import forpdateam.ru.forpda.rxapi.RxApi;
import forpdateam.ru.forpda.utils.ExtendedWebView;
import forpdateam.ru.forpda.utils.FilePickHelper;
import forpdateam.ru.forpda.utils.IntentHandler;
import forpdateam.ru.forpda.utils.SimpleTextWatcher;
import forpdateam.ru.forpda.utils.Utils;
import forpdateam.ru.forpda.utils.rx.Subscriber;
import forpdateam.ru.forpda.views.messagepanel.MessagePanel;
import forpdateam.ru.forpda.views.messagepanel.attachments.AttachmentsPopup;

/**
 * Created by radiationx on 25.08.16.
 */
public class QmsChatFragment extends TabFragment {
    private final static String JS_INTERFACE = "IChat";
    public final static String USER_ID_ARG = "USER_ID_ARG";
    public final static String USER_NICK_ARG = "USER_NICK_ARG";
    public final static String USER_AVATAR_ARG = "USER_AVATAR_ARG";
    public final static String THEME_ID_ARG = "THEME_ID_ARG";
    public final static String THEME_TITLE_ARG = "THEME_TITLE_ARG";

    private int userId = -1, themeId = -1;
    private String avatarUrl, userNick, themeTitle;
    private ExtendedWebView webView;
    private SwipeRefreshLayout refreshLayout;
    //private RecyclerView recyclerView;
    private MessagePanel messagePanel;
    private AttachmentsPopup attachmentsPopup;

    private QmsChatAdapter.OnItemClickListener onItemClickListener = message -> {
        Toast.makeText(getContext(), "ONCLICK " + message.getId(), Toast.LENGTH_SHORT).show();
    };
    private QmsChatAdapter.OnItemClickListener onLongItemClickListener = message -> {
        Toast.makeText(getContext(), "ON LONG CLICK " + message.getId(), Toast.LENGTH_SHORT).show();
    };

    private Subscriber<QmsChatModel> mainSubscriber = new Subscriber<>(this);
    private Subscriber<QmsMessage> messageSubscriber = new Subscriber<>(this);

    public QmsChatFragment() {
        configuration.setDefaultTitle("Чат");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userId = getArguments().getInt(USER_ID_ARG, -1);
            themeId = getArguments().getInt(THEME_ID_ARG, -1);
            themeTitle = getArguments().getString(THEME_TITLE_ARG);
            avatarUrl = getArguments().getString(USER_AVATAR_ARG);
            userNick = getArguments().getString(USER_NICK_ARG);
        }
    }

    private ViewStub viewStub;
    private AppCompatAutoCompleteTextView nickField;
    private AppCompatEditText titleField;

    @SuppressLint({"SetJavaScriptEnabled", "JavascriptInterface"})
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        baseInflateFragment(inflater, R.layout.fragment_qms_chat);
        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_list);
        viewStub = (ViewStub) findViewById(R.id.toolbar_content);
        viewStub.setLayoutResource(R.layout.toolbar_qms_new_theme);
        viewStub.inflate();
        nickField = (AppCompatAutoCompleteTextView) findViewById(R.id.qms_theme_nick_field);
        titleField = (AppCompatEditText) findViewById(R.id.qms_theme_title_field);
        //recyclerView = (RecyclerView) findViewById(R.id.qms_chat);
        messagePanel = new MessagePanel(getContext(), fragmentContainer, coordinatorLayout, false);
        messagePanel.setHeightChangeListener(newHeight -> webView.evalJs("setPaddingBottom(" + (newHeight / getResources().getDisplayMetrics().density) + ");"));
        if (getMainActivity().getWebViews().size() > 0) {
            webView = getMainActivity().getWebViews().element();
            getMainActivity().getWebViews().remove();
        } else {
            webView = new ExtendedWebView(getContext());
            webView.setTag("WebView_tag ".concat(Long.toString(System.currentTimeMillis())));
        }
        /*webView.setClipToPadding(false);
        webView.setPadding(0, 0, 0, App.px64);*/
        webView.loadUrl("about:blank");
        refreshLayout.addView(webView);
        refreshLayout.setOnRefreshListener(() -> refreshLayout.setRefreshing(false));
        webView.addJavascriptInterface(this, JS_INTERFACE);
        //webView.addJavascriptInterface(this, JS_POSTS_FUNCTIONS);
        webView.getSettings().setJavaScriptEnabled(true);
        registerForContextMenu(webView);

        //messagePanel.setHeightChangeListener(newHeight -> recyclerView.setPadding(0, 0, 0, newHeight));
        attachmentsPopup = messagePanel.getAttachmentsPopup();
        attachmentsPopup.setAddOnClickListener(v -> pickImage());
        attachmentsPopup.setDeleteOnClickListener(v -> {
            attachmentsPopup.preDeleteFiles();
            List<AttachmentItem> selectedFiles = attachmentsPopup.getSelected();
            for (AttachmentItem item : selectedFiles) {
                item.setStatus(AttachmentItem.STATUS_REMOVED);
            }
            attachmentsPopup.onDeleteFiles(selectedFiles);
        });
        attachmentsPopup.setInsertAttachmentListener(item -> "[url=http://savepic.ru/" + item.getId() + "." + item.getFormat() + "]" +
                "Файл: " + item.getName() + ", Размер: " + item.getWeight() + ", ID: " + item.getId() + "[/url]");
        messagePanel.addSendOnClickListener(v -> {
            if (themeId == -1) {
                sendNewTheme();
            } else {
                sendMessage();
            }
        });


        viewsReady();
        tryShowAvatar();
        //recyclerView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        //recyclerView.setLayoutManager(llm);

        if (userNick != null) {
            setSubtitle(userNick);
        }
        if (themeTitle != null) {
            setTitle(themeTitle);
        }
        titleField.addTextChangedListener(textWatcher);
        nickField.addTextChangedListener(textWatcher);
        editItem = toolbar.getMenu().add("Изменить").setIcon(App.getAppDrawable(R.drawable.ic_create_gray_24dp)).setOnMenuItemClickListener(menuItem -> {
            viewStub.setVisibility(View.VISIBLE);
            doneItem.setVisible(true);
            editItem.setVisible(false);
            return false;
        });
        doneItem = toolbar.getMenu().add("Ок").setIcon(App.getAppDrawable(R.drawable.ic_done_gray_24dp)).setOnMenuItemClickListener(menuItem -> {
            viewStub.setVisibility(View.GONE);
            editItem.setVisible(true);
            doneItem.setVisible(false);
            return false;
        });
        doneItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        editItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        editItem.setVisible(false);
        doneItem.setVisible(false);
        if (themeId != -1) {
            viewStub.setVisibility(View.GONE);
        } else {
            if (userNick != null) {
                nickField.setVisibility(View.GONE);
            } else {
                nickField.addTextChangedListener(new SimpleTextWatcher() {
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        userNick = s.toString();
                        searchUser(userNick);
                        setSubtitle(userNick);
                    }
                });
            }
            titleField.addTextChangedListener(new SimpleTextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    themeTitle = s.toString();
                    setTitle(themeTitle);
                }
            });
        }

        return view;
    }

    private TextWatcher textWatcher = new SimpleTextWatcher() {
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if ((userId != 0 || userNick.length() > 0) && titleField.getText().length() > 0) {
                doneItem.setVisible(true);
            } else {
                doneItem.setVisible(false);
            }
        }
    };

    private void sendNewTheme() {
        if (userNick == null || userNick.isEmpty()) {
            Toast.makeText(getContext(), "Введите ник пользователя", Toast.LENGTH_SHORT).show();
        } else if (titleField.getText().toString().isEmpty()) {
            Toast.makeText(getContext(), "Введите название темы", Toast.LENGTH_SHORT).show();
        } else if (messagePanel.getMessage().isEmpty()) {
            Toast.makeText(getContext(), "Введите сообщение", Toast.LENGTH_SHORT).show();
        } else {
            mainSubscriber.subscribe(RxApi.Qms().sendNewTheme(userNick, titleField.getText().toString(), messagePanel.getMessage()), this::onNewThemeCreate, new QmsChatModel());
        }
    }

    private void sendMessage() {
        messagePanel.setProgressState(true);
        messageSubscriber.subscribe(RxApi.Qms().sendMessage(userId, themeId, messagePanel.getMessage()), qmsMessage -> {
            messagePanel.setProgressState(false);
            if (qmsMessage.getContent() != null) {
                //adapter.addMessage(qmsMessage);
                messagePanel.clearMessage();
                messagePanel.clearAttachments();
            }
        }, new QmsMessage());
    }

    private void onNewThemeCreate(QmsChatModel chat) {
        viewStub.setVisibility(View.GONE);
        editItem.setVisible(false);
        doneItem.setVisible(false);
        messagePanel.clearMessage();
        messagePanel.clearAttachments();
        onLoadChat(chat);
    }

    private MenuItem doneItem, editItem;
    private Subscriber<String[]> searchUserSubscriber = new Subscriber<>(this);

    private void searchUser(String nick) {
        searchUserSubscriber.subscribe(RxApi.Qms().findUser(nick), this::onShowSearchRes, new String[]{});
    }

    private void onShowSearchRes(String[] res) {
        nickField.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, res));
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
        if (avatarUrl != null && userId != -1) {
            ImageLoader.getInstance().displayImage(avatarUrl, toolbarImageView);
            toolbarImageView.setVisibility(View.VISIBLE);
            toolbarImageView.setOnClickListener(view1 -> IntentHandler.handle("http://4pda.ru/forum/index.php?showuser=" + userId));
        } else {
            toolbarImageView.setVisibility(View.GONE);
        }
    }

    @Override
    public void loadData() {
        if (userId != -1 && themeId != -1) {
            mainSubscriber.subscribe(RxApi.Qms().getChat(userId, themeId), this::onLoadChat, new QmsChatModel(), v -> loadData());
        }
    }

    QmsChatModel currentChat;

    private void onLoadChat(QmsChatModel chat) {
        themeId = chat.getThemeId();
        themeTitle = chat.getTitle();
        userId = chat.getUserId();
        userNick = chat.getNick();

        currentChat = chat;
        webView.loadDataWithBaseURL("http://4pda.ru/forum/", chat.getHtml(), "text/html", "utf-8", null);

    }

    private Subscriber<List<AttachmentItem>> attachmentSubscriber = new Subscriber<>(this);

    public void uploadFiles(List<RequestFile> files) {
        attachmentsPopup.preUploadFiles(files);
        attachmentSubscriber.subscribe(RxApi.Qms().uploadFiles(files), items -> attachmentsPopup.onUploadFiles(items), new ArrayList<>(), null);
    }

    private static final int PICK_IMAGE = 1228;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                //Display an error
                return;
            }
            uploadFiles(FilePickHelper.onActivityResult(getContext(), data));
        }
    }

    public void pickImage() {
        startActivityForResult(FilePickHelper.pickImage(PICK_IMAGE), PICK_IMAGE);
    }

    @JavascriptInterface
    public void showMorePosts() {
        run(new Runnable() {
            @Override
            public void run() {
                MiniTemplator t = App.getInstance().getTemplate(App.TEMPLATE_QMS_CHAT_MESS);
                String posts = "";
                int size = currentChat.getChatItemsList().size();
                int lastIndex = currentChat.getChatItemsList().lastIndexOf(currentChat.getLastShowedMess());
                Log.e("SUKA", "LAST INDEX SUKA "+lastIndex);
                currentChat.setLastShowedMess(currentChat.getChatItemsList().get(Math.max(lastIndex - 20, 0)));
                for (int i = Math.max(lastIndex - 20, 0); i < lastIndex; i++) {
                    QmsMessage mess = currentChat.getChatItemsList().get(i);
                    if (mess.isDate()) continue;
                    t.setVariableOpt("from_class", mess.isMyMessage() ? "our" : "his");
                    t.setVariableOpt("mess_id", mess.getId());
                    t.setVariableOpt("content", mess.getContent());
                    t.setVariableOpt("date", mess.getDate());

                    t.addBlockOpt("mess");
                }
                posts = t.generateOutput();
                posts = posts.replaceAll("\n","");
                Log.e("SUKA", "POSTS "+posts);
                t.reset();
                webView.evalJs("showMorePosts('"+posts+"')");
            }
        });
    }

    public void run(final Runnable runnable) {
        getMainActivity().runOnUiThread(runnable);
    }
}
