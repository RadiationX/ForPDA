package forpdateam.ru.forpda.fragments.qms.chat;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import biz.source_code.miniTemplator.MiniTemplator;
import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.api.RequestFile;
import forpdateam.ru.forpda.api.qms.models.QmsChatModel;
import forpdateam.ru.forpda.api.qms.models.QmsMessage;
import forpdateam.ru.forpda.api.theme.editpost.models.AttachmentItem;
import forpdateam.ru.forpda.client.Client;
import forpdateam.ru.forpda.client.ClientHelper;
import forpdateam.ru.forpda.fragments.TabFragment;
import forpdateam.ru.forpda.fragments.jsinterfaces.IBase;
import forpdateam.ru.forpda.rxapi.RxApi;
import forpdateam.ru.forpda.rxapi.apiclasses.QmsRx;
import forpdateam.ru.forpda.utils.ExtendedWebView;
import forpdateam.ru.forpda.utils.FilePickHelper;
import forpdateam.ru.forpda.utils.IntentHandler;
import forpdateam.ru.forpda.utils.rx.Subscriber;
import forpdateam.ru.forpda.views.messagepanel.MessagePanel;
import forpdateam.ru.forpda.views.messagepanel.attachments.AttachmentsPopup;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

/**
 * Created by radiationx on 25.08.16.
 */
public class QmsChatFragment extends TabFragment implements IBase, ChatThemeCreator.ThemeCreatorInterface {
    private final static String JS_INTERFACE = "IChat";
    public final static String USER_ID_ARG = "USER_ID_ARG";
    public final static String USER_NICK_ARG = "USER_NICK_ARG";
    public final static String USER_AVATAR_ARG = "USER_AVATAR_ARG";
    public final static String THEME_ID_ARG = "THEME_ID_ARG";
    public final static String THEME_TITLE_ARG = "THEME_TITLE_ARG";
    private final static int NOT_CREATED = -1;

    final QmsChatModel currentChat = new QmsChatModel();
    /*int userId = -1;
    private int themeId = -1;
    private String avatarUrl;
    String userNick;
    String themeTitle;*/
    private ExtendedWebView webView;
    private FrameLayout chatContainer;
    private MessagePanel messagePanel;

    private AttachmentsPopup attachmentsPopup;
    private Subscriber<QmsChatModel> mainSubscriber = new Subscriber<>(this);

    private Subscriber<ArrayList<QmsMessage>> messageSubscriber = new Subscriber<>(this);
    private boolean baseWebComplete = false;
    private String messagesSrc = null;

    public QmsChatFragment() {
        configuration.setDefaultTitle("Чат");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            currentChat.setUserId(getArguments().getInt(USER_ID_ARG, NOT_CREATED));
            currentChat.setThemeId(getArguments().getInt(THEME_ID_ARG, NOT_CREATED));
            currentChat.setTitle(getArguments().getString(THEME_TITLE_ARG));
            currentChat.setAvatarUrl(getArguments().getString(USER_AVATAR_ARG));
            currentChat.setNick(getArguments().getString(USER_NICK_ARG));
        }
    }

    private ChatThemeCreator themeCreator;

    @SuppressLint({"SetJavaScriptEnabled", "JavascriptInterface"})
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        baseInflateFragment(inflater, R.layout.fragment_qms_chat);
        chatContainer = (FrameLayout) findViewById(R.id.qms_chat_container);
        messagePanel = new MessagePanel(getContext(), fragmentContainer, coordinatorLayout, false);
        messagePanel.setHeightChangeListener(newHeight -> webView.evalJs("setPaddingBottom(" + (newHeight / getResources().getDisplayMetrics().density) + ");"));

        webView = getMainActivity().getWebViewsProvider().pull(getContext());

        chatContainer.addView(webView);
        webView.addJavascriptInterface(this, JS_INTERFACE);
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
            if (currentChat.getThemeId() == NOT_CREATED) {
                themeCreator.sendNewTheme();
            } else {
                sendMessage();
            }
        });


        viewsReady();
        tryShowAvatar();

        if (currentChat.getNick() != null) {
            setSubtitle(currentChat.getNick());
        }
        if (currentChat.getTitle() != null) {
            setTitle(currentChat.getTitle());
        }
        if (currentChat.getThemeId() == NOT_CREATED) {
            themeCreator = new ChatThemeCreator(this);
        }
        return view;
    }

    public MessagePanel getMessagePanel() {
        return messagePanel;
    }

    private void loadBaseWeb() {
        MiniTemplator t = App.getInstance().getTemplate(App.TEMPLATE_QMS_CHAT);
        t.setVariableOpt("body_type", "qms");
        t.setVariableOpt("messages", "");
        t.reset();
        webView.loadDataWithBaseURL("http://4pda.ru/forum/", t.generateOutput(), "text/html", "utf-8", null);
    }


    @Override
    public void onCreateNewTheme(String nick, String title, String message) {
        mainSubscriber.subscribe(RxApi.Qms().sendNewTheme(nick, title, message), this::onNewThemeCreate, new QmsChatModel());
    }

    private void onNewThemeCreate(QmsChatModel chat) {
        themeCreator.onNewThemeCreate();
        messagePanel.clearMessage();
        messagePanel.clearAttachments();
        onLoadChat(chat);
    }

    /* CHAT */

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
        if (currentChat.getUserId() != NOT_CREATED && currentChat.getThemeId() != NOT_CREATED) {
            mainSubscriber.subscribe(RxApi.Qms().getChat(currentChat.getUserId(), currentChat.getThemeId()), this::onLoadChat, new QmsChatModel(), v -> loadData());
        }
    }

    WebSocket webSocket;

    WebSocketListener webSocketListener = new WebSocketListener() {
        Pattern pattern = Pattern.compile("\\[(\\d+),(\\d+),\"q(\\d+)\",(\\d+),(\\d+)\\]");

        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            Log.d("WS_CHAT", "ON OPEN: " + response.toString());
            Log.d("WS_CHAT", "ON OPEN HEADERS: " + response.headers().toString());
            webSocket.send("[0,\"sv\"]");
            webSocket.send("[0, \"ea\", \"u" + ClientHelper.getUserId() + "\"]");
                /*webSocket.send("Hello...");
                webSocket.send("...World!");
                webSocket.send(ByteString.decodeHex("deadbeef"));
                webSocket.close(1000, "Goodbye, World!");*/
        }


        @Override
        public void onMessage(WebSocket webSocket, String text) {
            Log.d("WS_CHAT", "ON T MESSAGE: " + text);
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                int themeId = Integer.parseInt(matcher.group(3));
                int messId = Integer.parseInt(matcher.group(5));
                Log.d("WS_CHAT", "NEW QMS MESSAGE " + themeId + " : " + messId);
                if (themeId == currentChat.getThemeId()) {
                    onNewWsMessage(themeId, messId);
                }
            }
        }

        @Override
        public void onMessage(WebSocket webSocket, ByteString bytes) {
            Log.d("WS_CHAT", "ON B MESSAGE: " + bytes.hex());
        }

        @Override
        public void onClosing(WebSocket webSocket, int code, String reason) {
            Log.d("WS_CHAT", "ON CLOSING: " + code + " " + reason);
            webSocket.close(1000, null);

        }

        @Override
        public void onClosed(WebSocket webSocket, int code, String reason) {
            Log.d("WS_CHAT", "ON CLOSED: " + code + " " + reason);
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            Log.d("WS_CHAT", "ON FAILURE: " + t.getMessage() + " " + response.toString());
            t.printStackTrace();
        }
    };

    private void onNewWsMessage(int themeId, int messageId) {
        messageSubscriber.subscribe(RxApi.Qms().getMessagesFromWs(themeId, messageId, currentChat.getChatItemsList().get(currentChat.getChatItemsList().size() - 1).getId()), qmsMessage -> {
            Log.d("WS_CHAT", "RETURNED MESSAGE " + qmsMessage.size());
            if (qmsMessage.size() > 0) {
                MiniTemplator t = App.getInstance().getTemplate(App.TEMPLATE_QMS_CHAT_MESS);


                for (int i = 0; i < qmsMessage.size(); i++) {
                    QmsMessage message = qmsMessage.get(i);
                    boolean contains = false;
                    for (QmsMessage viewmessage : currentChat.getChatItemsList()) {
                        if (viewmessage.getId() == message.getId()) {
                            contains = true;
                            break;
                        }
                    }
                    if (contains) {
                        break;
                    }
                    currentChat.addChatItem(message);
                    QmsRx.generateMess(t, message);
                }
                String messagesSrc = t.generateOutput();
                t.reset();
                messagesSrc = messagesSrc.replaceAll("\n", "").replaceAll("'", "&apos;");
                webView.evalJs("showNewMess('".concat(messagesSrc).concat("', true)"));
            }

        }, new ArrayList<>());
    }

    private void onLoadChat(QmsChatModel loadedChat) {
        currentChat.setThemeId(loadedChat.getThemeId());
        currentChat.setTitle(loadedChat.getTitle());
        currentChat.setUserId(loadedChat.getUserId());
        currentChat.setNick(loadedChat.getNick());
        long time = System.currentTimeMillis();
        String messagesSrc = null;
        if (currentChat.getChatItemsList().size() == 0) {
            webSocket = Client.getInstance().createWebSocketConnection(webSocketListener);
            currentChat.getChatItemsList().addAll(loadedChat.getChatItemsList());
            MiniTemplator t = App.getInstance().getTemplate(App.TEMPLATE_QMS_CHAT_MESS);
            int end = currentChat.getChatItemsList().size();
            int start = Math.max(end - 30, 0);
            QmsRx.generateMess(t, currentChat.getChatItemsList(), start, end);
            messagesSrc = t.generateOutput();
            t.reset();
            currentChat.setShowedMessIndex(start);
        } else {
            int start = currentChat.getChatItemsList().size();
            int end = loadedChat.getChatItemsList().size();
            if (start < end) {
                MiniTemplator t = App.getInstance().getTemplate(App.TEMPLATE_QMS_CHAT_MESS);
                List<QmsMessage> newMessages = new ArrayList<>();
                for (int i = start; i < end; i++) {
                    QmsMessage message = loadedChat.getChatItemsList().get(i);
                    newMessages.add(message);
                    currentChat.addChatItem(message);
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
        messageSubscriber.subscribe(RxApi.Qms().sendMessage(currentChat.getUserId(), currentChat.getThemeId(), messagePanel.getMessage()), qmsMessage -> {
            messagePanel.setProgressState(false);
            if (qmsMessage.size() > 0 && qmsMessage.get(0).getContent() != null) {
                QmsMessage message = qmsMessage.get(0);
                boolean contains = false;
                for (QmsMessage viewmessage : currentChat.getChatItemsList()) {
                    if (viewmessage.getId() == message.getId()) {
                        contains = true;
                        break;
                    }
                }
                if (!contains) {
                    currentChat.addChatItem(message);
                    MiniTemplator t = App.getInstance().getTemplate(App.TEMPLATE_QMS_CHAT_MESS);
                    String messSrc = QmsRx.generateMess(t, message).generateOutput();
                    messSrc = messSrc.replaceAll("\n", "").replaceAll("'", "&apos;");
                    t.reset();
                    webView.evalJs("showNewMess('" + messSrc + "', true)");

                    messagePanel.clearMessage();
                    messagePanel.clearAttachments();
                }


            }
        }, new ArrayList<>());
    }


    private void tryShowAvatar() {
        if (currentChat.getAvatarUrl() != null && currentChat.getUserId() != NOT_CREATED) {
            ImageLoader.getInstance().displayImage(currentChat.getAvatarUrl(), toolbarImageView);
            toolbarImageView.setVisibility(View.VISIBLE);
            toolbarImageView.setOnClickListener(view1 -> IntentHandler.handle("http://4pda.ru/forum/index.php?showuser=" + currentChat.getUserId()));
        } else {
            toolbarImageView.setVisibility(View.GONE);
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

        unregisterForContextMenu(webView);
        webView.removeJavascriptInterface(JS_INTERFACE);
        webView.removeJavascriptInterface(JS_BASE_INTERFACE);
        webView.destroy();
        ((ViewGroup) webView.getParent()).removeAllViews();
        getMainActivity().getWebViewsProvider().push(webView);
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
}
