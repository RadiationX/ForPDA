package forpdateam.ru.forpda.fragments.qms;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatAutoCompleteTextView;
import android.support.v7.widget.AppCompatEditText;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import biz.source_code.miniTemplator.MiniTemplator;
import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.RequestFile;
import forpdateam.ru.forpda.api.qms.models.QmsChatModel;
import forpdateam.ru.forpda.api.qms.models.QmsMessage;
import forpdateam.ru.forpda.api.theme.editpost.models.AttachmentItem;
import forpdateam.ru.forpda.fragments.TabFragment;
import forpdateam.ru.forpda.fragments.jsinterfaces.IBase;
import forpdateam.ru.forpda.rxapi.RxApi;
import forpdateam.ru.forpda.rxapi.apiclasses.QmsRx;
import forpdateam.ru.forpda.utils.ExtendedWebView;
import forpdateam.ru.forpda.utils.FilePickHelper;
import forpdateam.ru.forpda.utils.IntentHandler;
import forpdateam.ru.forpda.utils.SimpleTextWatcher;
import forpdateam.ru.forpda.utils.rx.Subscriber;
import forpdateam.ru.forpda.views.messagepanel.MessagePanel;
import forpdateam.ru.forpda.views.messagepanel.attachments.AttachmentsPopup;

/**
 * Created by radiationx on 25.08.16.
 */
public class QmsChatFragment extends TabFragment implements IBase {
    private final static String JS_INTERFACE = "IChat";
    public final static String USER_ID_ARG = "USER_ID_ARG";
    public final static String USER_NICK_ARG = "USER_NICK_ARG";
    public final static String USER_AVATAR_ARG = "USER_AVATAR_ARG";
    public final static String THEME_ID_ARG = "THEME_ID_ARG";
    public final static String THEME_TITLE_ARG = "THEME_TITLE_ARG";

    private int userId = -1, themeId = -1;
    private String avatarUrl, userNick, themeTitle;
    private ExtendedWebView webView;
    private FrameLayout chatContainer;
    private MessagePanel messagePanel;
    private AttachmentsPopup attachmentsPopup;
    private ViewStub viewStub;
    private AppCompatAutoCompleteTextView nickField;
    private AppCompatEditText titleField;
    private MenuItem doneItem, editItem;
    private Subscriber<List<String>> searchUserSubscriber = new Subscriber<>(this);

    private Subscriber<QmsChatModel> mainSubscriber = new Subscriber<>(this);
    private Subscriber<QmsMessage> messageSubscriber = new Subscriber<>(this);

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
    private QmsChatModel currentChat;
    private Timer messageChecker = new Timer();

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

    @SuppressLint({"SetJavaScriptEnabled", "JavascriptInterface"})
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        baseInflateFragment(inflater, R.layout.fragment_qms_chat);
        chatContainer = (FrameLayout) findViewById(R.id.qms_chat_container);
        viewStub = (ViewStub) findViewById(R.id.toolbar_content);
        viewStub.setLayoutResource(R.layout.toolbar_qms_new_theme);
        viewStub.inflate();
        nickField = (AppCompatAutoCompleteTextView) findViewById(R.id.qms_theme_nick_field);
        titleField = (AppCompatEditText) findViewById(R.id.qms_theme_title_field);
        messagePanel = new MessagePanel(getContext(), fragmentContainer, coordinatorLayout, false);
        messagePanel.setHeightChangeListener(newHeight -> webView.evalJs("setPaddingBottom(" + (newHeight / getResources().getDisplayMetrics().density) + ");"));
        if (getMainActivity().getWebViews().size() > 0) {
            webView = getMainActivity().getWebViews().element();
            getMainActivity().getWebViews().remove();
        } else {
            webView = new ExtendedWebView(getContext());
            webView.setTag("WebView_tag ".concat(Long.toString(System.currentTimeMillis())));
        }

        chatContainer.addView(webView);
        webView.addJavascriptInterface(this, JS_INTERFACE);
        webView.getSettings().setJavaScriptEnabled(true);
        registerForContextMenu(webView);
        webView.setWebViewClient(new QmsWebViewClient());
        webView.setWebChromeClient(new QmsChromeClient());
        loadBaseWeb();

        attachmentsPopup = messagePanel.getAttachmentsPopup();
        attachmentsPopup.setAddOnClickListener(v -> tryPickFile());
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
        messageChecker.schedule(new MessagesChecker(), 0, 60000);

        if (userNick != null) {
            setSubtitle(userNick);
        }
        if (themeTitle != null) {
            setTitle(themeTitle);
        }
        initCreatorViews();
        return view;
    }

    private void loadBaseWeb() {
        MiniTemplator t = App.getInstance().getTemplate(App.TEMPLATE_QMS_CHAT);

        /*t.setVariableOpt("chat_title", forpdateam.ru.forpda.api.Utils.htmlEncode(chatModel.getTitle()));
        t.setVariableOpt("chatId", chatModel.getThemeId());
        t.setVariableOpt("userId", chatModel.getUserId());
        t.setVariableOpt("nick", chatModel.getNick());
        t.setVariableOpt("avatarUrl", chatModel.getAvatarUrl());*/

        t.setVariableOpt("body_type", "qms");
        t.setVariableOpt("messages", "");
        t.reset();
        String html = t.generateOutput();
        //Log.e("FORPDA_LOG", "GENERATED " + html);
        webView.loadDataWithBaseURL("http://4pda.ru/forum/", html, "text/html", "utf-8", null);
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
        messageChecker.cancel();
        messageChecker.purge();

        unregisterForContextMenu(webView);
        webView.setActionModeListener(null);
        webView.removeJavascriptInterface(JS_INTERFACE);
        webView.removeJavascriptInterface(JS_BASE_INTERFACE);
        webView.setWebChromeClient(null);
        webView.setWebViewClient(null);
        webView.loadUrl("about:blank");
        webView.clearHistory();
        webView.clearSslPreferences();
        webView.clearDisappearingChildren();
        webView.clearFocus();
        webView.clearFormData();
        webView.clearMatches();
        ((ViewGroup) webView.getParent()).removeAllViews();
        if (getMainActivity().getWebViews().size() < 10) {
            getMainActivity().getWebViews().add(webView);
        }
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

    /* NEW THEME CREATOR */


    private void searchUser(String nick) {
        searchUserSubscriber.subscribe(RxApi.Qms().findUser(nick), this::onShowSearchRes, new ArrayList());
    }

    private void onShowSearchRes(List<String> res) {
        nickField.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, res));
    }

    private void initCreatorViews() {
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
    }

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


    private void onNewThemeCreate(QmsChatModel chat) {
        viewStub.setVisibility(View.GONE);
        editItem.setVisible(false);
        doneItem.setVisible(false);
        messagePanel.clearMessage();
        messagePanel.clearAttachments();
        onLoadChat(chat);
    }

    /* CHAT */
    private boolean baseWebComplete = false;
    private String messagesSrc = null;

    @JavascriptInterface
    @Override
    public void playClickEffect() {
        run(this::tryPlayClickEffect);
    }

    private class QmsWebViewClient extends WebViewClient {

        @SuppressWarnings("deprecation")
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return handleUri(Uri.parse(url));
        }

        @TargetApi(Build.VERSION_CODES.N)
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            return handleUri(request.getUrl());
        }

        private boolean handleUri(Uri uri) {
            IntentHandler.handle(uri.toString());
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
        }
    }

    private class QmsChromeClient extends WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            Log.e("FORPDA_LOG", "progress " + newProgress);
            if (newProgress == 100) {
                if (!baseWebComplete && messagesSrc != null) {
                    Log.e("FORPDA_LOG", "SHOW NEW MESS CLIENT");
                    webView.evalJs("showNewMess('".concat(messagesSrc).concat("', true)"));
                    messagesSrc = null;
                }
                baseWebComplete = true;
            }
        }
    }

    @Override
    public void loadData() {
        if (userId != -1 && themeId != -1) {
            mainSubscriber.subscribe(RxApi.Qms().getChat(userId, themeId), this::onLoadChat, new QmsChatModel(), v -> loadData());
        }
    }

    private void onLoadChat(QmsChatModel chat) {
        themeId = chat.getThemeId();
        themeTitle = chat.getTitle();
        userId = chat.getUserId();
        userNick = chat.getNick();
        long time = System.currentTimeMillis();
        String messagesSrc = null;
        if (currentChat == null) {
            currentChat = chat;
            MiniTemplator t = App.getInstance().getTemplate(App.TEMPLATE_QMS_CHAT_MESS);
            int end = currentChat.getChatItemsList().size();
            int start = Math.max(end - 30, 0);
            currentChat.setShowedMessIndex(start);
            QmsRx.generateMess(t, currentChat.getChatItemsList(), start, end);
            messagesSrc = t.generateOutput();
            t.reset();
        } else {
            int start = currentChat.getChatItemsList().size();
            int end = chat.getChatItemsList().size();
            if (start < end) {
                MiniTemplator t = App.getInstance().getTemplate(App.TEMPLATE_QMS_CHAT_MESS);
                List<QmsMessage> newMessages = new ArrayList<>();
                for (int i = start; i < end; i++) {
                    QmsMessage message = chat.getChatItemsList().get(i);
                    newMessages.add(message);
                    currentChat.getChatItemsList().add(message);
                }
                QmsRx.generateMess(t, newMessages);
                messagesSrc = t.generateOutput();
                t.reset();
            }
        }
        if (messagesSrc != null) {
            messagesSrc = messagesSrc.replaceAll("\n", "").replaceAll("'", "&apos;");
            Log.e("FORPDA_LOG", "TIME " + (System.currentTimeMillis() - time));
            Log.e("FORPDA_LOG", "NEW MESS FINAL");
            /*final String veryLongString = messagesSrc;
            int maxLogSize = 1000;
            for (int i = 0; i <= veryLongString.length() / maxLogSize; i++) {
                int start = i * maxLogSize;
                int end = (i + 1) * maxLogSize;
                end = end > veryLongString.length() ? veryLongString.length() : end;
                Log.e("FORPDA_LOG", veryLongString.substring(start, end));
            }*/
            if (baseWebComplete) {
                Log.e("FORPDA_LOG", "SHOW NEW MESS");
                webView.evalJs("showNewMess('".concat(messagesSrc).concat("', true)"));
            } else {
                QmsChatFragment.this.messagesSrc = messagesSrc;
            }
        }
    }


    private void sendMessage() {
        messagePanel.setProgressState(true);
        messageSubscriber.subscribe(RxApi.Qms().sendMessage(userId, themeId, messagePanel.getMessage()), qmsMessage -> {
            messagePanel.setProgressState(false);
            if (qmsMessage.getContent() != null) {
                MiniTemplator t = App.getInstance().getTemplate(App.TEMPLATE_QMS_CHAT_MESS);
                String messSrc = QmsRx.generateMess(t, qmsMessage).generateOutput();
                messSrc = messSrc.replaceAll("\n", "");
                t.reset();
                webView.evalJs("showNewMess('" + messSrc + "', true)");

                messagePanel.clearMessage();
                messagePanel.clearAttachments();
            }
        }, new QmsMessage());
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

    private class MessagesChecker extends TimerTask {
        public void run() {
            if (isVisible()) {
                loadData();
            }
        }
    }

    @JavascriptInterface
    public void showMoreMess() {
        run(new Runnable() {
            @Override
            public void run() {
                MiniTemplator t = App.getInstance().getTemplate(App.TEMPLATE_QMS_CHAT_MESS);
                int endIndex = currentChat.getShowedMessIndex();
                int startIndex = Math.max(endIndex - 30, 0);
                currentChat.setShowedMessIndex(startIndex);
                QmsRx.generateMess(t, currentChat.getChatItemsList(), startIndex, endIndex);
                String messagesSrc = t.generateOutput();
                messagesSrc = messagesSrc.replaceAll("\n", "");
                t.reset();
                webView.evalJs("showMoreMess('" + messagesSrc + "')");
            }
        });
    }

    public void run(final Runnable runnable) {
        getMainActivity().runOnUiThread(runnable);
    }

    /* ATTACHMENTS LOADER */


    private Subscriber<List<AttachmentItem>> attachmentSubscriber = new Subscriber<>(this);

    public void uploadFiles(List<RequestFile> files) {
        attachmentsPopup.preUploadFiles(files);
        attachmentSubscriber.subscribe(RxApi.Qms().uploadFiles(files), items -> attachmentsPopup.onUploadFiles(items), new ArrayList<>(), null);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PICK_FILE && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                //Display an error
                return;
            }
            uploadFiles(FilePickHelper.onActivityResult(getContext(), data));
        }
    }

    public void tryPickFile() {
        getMainActivity().checkStoragePermission(() -> startActivityForResult(FilePickHelper.pickImage(true), REQUEST_PICK_FILE));
    }
}
