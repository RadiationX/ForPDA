package forpdateam.ru.forpda.fragments.qms.chat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Observer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import biz.source_code.miniTemplator.MiniTemplator;
import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.TabManager;
import forpdateam.ru.forpda.api.RequestFile;
import forpdateam.ru.forpda.api.others.user.ForumUser;
import forpdateam.ru.forpda.api.qms.models.QmsChatModel;
import forpdateam.ru.forpda.api.qms.models.QmsContact;
import forpdateam.ru.forpda.api.qms.models.QmsMessage;
import forpdateam.ru.forpda.api.theme.editpost.models.AttachmentItem;
import forpdateam.ru.forpda.data.models.TabNotification;
import forpdateam.ru.forpda.fragments.TabFragment;
import forpdateam.ru.forpda.fragments.notes.NotesAddPopup;
import forpdateam.ru.forpda.fragments.qms.QmsThemesFragment;
import forpdateam.ru.forpda.rxapi.ForumUsersCache;
import forpdateam.ru.forpda.rxapi.RxApi;
import forpdateam.ru.forpda.rxapi.apiclasses.QmsRx;
import forpdateam.ru.forpda.settings.Preferences;
import forpdateam.ru.forpda.utils.CustomWebChromeClient;
import forpdateam.ru.forpda.utils.CustomWebViewClient;
import forpdateam.ru.forpda.utils.FilePickHelper;
import forpdateam.ru.forpda.utils.IntentHandler;
import forpdateam.ru.forpda.utils.rx.Subscriber;
import forpdateam.ru.forpda.views.ExtendedWebView;
import forpdateam.ru.forpda.views.messagepanel.MessagePanel;
import forpdateam.ru.forpda.views.messagepanel.attachments.AttachmentsPopup;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by radiationx on 25.08.16.
 */
public class QmsChatFragment extends TabFragment implements ChatThemeCreator.ThemeCreatorInterface, ExtendedWebView.JsLifeCycleListener {
    private final static String LOG_TAG = QmsChatFragment.class.getSimpleName();
    private final static String JS_INTERFACE = "IChat";
    public final static String USER_ID_ARG = "USER_ID_ARG";
    public final static String USER_NICK_ARG = "USER_NICK_ARG";
    public final static String USER_AVATAR_ARG = "USER_AVATAR_ARG";
    public final static String THEME_ID_ARG = "THEME_ID_ARG";
    public final static String THEME_TITLE_ARG = "THEME_TITLE_ARG";
    private final static Pattern attachmentPattern = Pattern.compile("\\[url=https?:\\/\\/savepic\\.net\\/(\\d+)\\.[^\\]]*?\\]");

    private MenuItem blackListMenuItem;
    private MenuItem noteMenuItem;
    private MenuItem toDialogsMenuItem;
    final QmsChatModel currentChat = new QmsChatModel();
    private ChatThemeCreator themeCreator;
    private ExtendedWebView webView;
    private FrameLayout chatContainer;
    private ProgressBar progressBar;
    private MessagePanel messagePanel;
    private AttachmentsPopup attachmentsPopup;

    private Subscriber<QmsChatModel> mainSubscriber = new Subscriber<>(this);
    private Subscriber<ArrayList<QmsMessage>> messageSubscriber = new Subscriber<>(this);
    private Subscriber<ArrayList<QmsContact>> contactsSubscriber = new Subscriber<>(this);

    private Observer chatPreferenceObserver = (observable, o) -> {
        if (o == null) return;
        String key = (String) o;
        switch (key) {
            case Preferences.Main.WEBVIEW_FONT_SIZE: {
                webView.setRelativeFontSize(Preferences.Main.getWebViewSize(getContext()));
            }
        }
    };

    private Observer notification = (observable, o) -> {
        if (o == null) return;
        TabNotification event = (TabNotification) o;
        runInUiThread(() -> handleEvent(event));
    };

    public QmsChatFragment() {
        configuration.setDefaultTitle(App.get().getString(R.string.fragment_title_chat));
    }

    public MessagePanel getMessagePanel() {
        return messagePanel;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            currentChat.setUserId(getArguments().getInt(USER_ID_ARG, QmsChatModel.NOT_CREATED));
            currentChat.setThemeId(getArguments().getInt(THEME_ID_ARG, QmsChatModel.NOT_CREATED));
            currentChat.setTitle(getArguments().getString(THEME_TITLE_ARG));
            currentChat.setAvatarUrl(getArguments().getString(USER_AVATAR_ARG));
            currentChat.setNick(getArguments().getString(USER_NICK_ARG));
        }
    }

    @SuppressLint({"SetJavaScriptEnabled", "JavascriptInterface"})
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        baseInflateFragment(inflater, R.layout.fragment_qms_chat);
        chatContainer = (FrameLayout) findViewById(R.id.qms_chat_container);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        messagePanel = new MessagePanel(getContext(), fragmentContainer, coordinatorLayout, false);
        webView = getMainActivity().getWebViewsProvider().pull(getContext());
        attachWebView(webView);
        chatContainer.addView(webView, 0);
        attachmentsPopup = messagePanel.getAttachmentsPopup();
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        webView.setJsLifeCycleListener(this);
        webView.addJavascriptInterface(this, JS_INTERFACE);
        registerForContextMenu(webView);
        webView.setWebViewClient(new CustomWebViewClient());
        webView.setWebChromeClient(new CustomWebChromeClient());
        loadBaseWebContainer();

        viewsReady();

        attachmentsPopup.setAddOnClickListener(v -> tryPickFile());
        attachmentsPopup.setDeleteOnClickListener(v -> {
            attachmentsPopup.preDeleteFiles();
            List<AttachmentItem> selectedFiles = attachmentsPopup.getSelected();
            for (AttachmentItem item : selectedFiles) {
                item.setStatus(AttachmentItem.STATUS_REMOVED);
            }
            attachmentsPopup.onDeleteFiles(selectedFiles);
        });
        attachmentsPopup.setInsertAttachmentListener(item -> String.format(Locale.getDefault(),
                "\n[url=http://savepic.net/%d.%s]Файл: %s, Размер: %s, ID: %d[/url]\n",
                item.getId(),
                item.getExtension(),
                item.getName(),
                item.getWeight(),
                item.getId()));
        messagePanel.addSendOnClickListener(v -> {
            if (currentChat.getThemeId() == QmsChatModel.NOT_CREATED) {
                themeCreator.sendNewTheme();
            } else {
                sendMessage();
            }
        });



        messagePanel.setHeightChangeListener(newHeight -> {
            webView.setPaddingBottom(newHeight);
        });
        App.get().addPreferenceChangeObserver(chatPreferenceObserver);
        tryShowAvatar();

        if (currentChat.getNick() != null) {
            setSubtitle(currentChat.getNick());
        }
        if (currentChat.getTitle() != null) {
            setTitle(currentChat.getTitle());
            setTabTitle(String.format(getString(R.string.fragment_tab_title_chat), currentChat.getTitle(), currentChat.getNick()));
        }
        if (currentChat.getThemeId() == QmsChatModel.NOT_CREATED) {
            themeCreator = new ChatThemeCreator(this);
        }
    }

    private void addUnusedAttachments() {
        try {
            Matcher matcher = attachmentPattern.matcher(messagePanel.getMessage());
            ArrayList<Integer> attachmentsIds = new ArrayList<>();
            while (matcher.find()) {
                int id = Integer.parseInt(matcher.group(1));
                attachmentsIds.add(id);
            }
            ArrayList<AttachmentItem> notAttached = new ArrayList<>();
            for (AttachmentItem item : attachmentsPopup.getAttachments()) {
                if (!attachmentsIds.contains(item.getId())) {
                    notAttached.add(item);
                }
            }
            messagePanel.getMessageField().setSelection(messagePanel.getMessageField().getText().length());
            attachmentsPopup.insertAttachment(notAttached, false);
        } catch (Exception ignore) {
        }
    }

    @Override
    protected void addBaseToolbarMenu() {
        super.addBaseToolbarMenu();
        blackListMenuItem = getMenu().add(R.string.add_to_blacklist)
                .setOnMenuItemClickListener(item -> {
                    contactsSubscriber.subscribe(RxApi.Qms().blockUser(currentChat.getNick()), qmsContacts -> {
                        if (!qmsContacts.isEmpty()) {
                            Toast.makeText(getContext(), R.string.user_added_to_blacklist, Toast.LENGTH_SHORT).show();
                        }
                    }, new ArrayList<>());
                    return false;
                });
        noteMenuItem = getMenu().add(R.string.create_note)
                .setOnMenuItemClickListener(item -> {
                    String title = String.format(getString(R.string.dialog_Title_Nick), currentChat.getTitle(), currentChat.getNick());
                    String url = "https://4pda.ru/forum/index.php?act=qms&mid=" + currentChat.getUserId() + "&t=" + currentChat.getThemeId();
                    NotesAddPopup.showAddNoteDialog(getContext(), title, url);
                    return true;
                });
        toDialogsMenuItem = getMenu().add(R.string.to_dialogs)
                .setOnMenuItemClickListener(item -> {
                    Bundle args = new Bundle();
                    args.putInt(QmsThemesFragment.USER_ID_ARG, currentChat.getUserId());
                    args.putString(QmsThemesFragment.USER_AVATAR_ARG, currentChat.getAvatarUrl());
                    TabManager.get().add(QmsThemesFragment.class, args);
                    return true;
                });
        refreshToolbarMenuItems(false);
    }

    @Override
    protected void refreshToolbarMenuItems(boolean enable) {
        super.refreshToolbarMenuItems(enable);
        if (enable) {
            blackListMenuItem.setEnabled(true);
            noteMenuItem.setEnabled(true);
            toDialogsMenuItem.setEnabled(true);
        } else {
            blackListMenuItem.setEnabled(false);
            noteMenuItem.setEnabled(false);
            toDialogsMenuItem.setEnabled(false);
        }
    }

    //From theme creator
    @Override
    public void onCreateNewTheme(String nick, String title, String message) {
        addUnusedAttachments();
        refreshToolbarMenuItems(false);
        progressBar.setVisibility(View.VISIBLE);
        mainSubscriber.subscribe(RxApi.Qms().sendNewTheme(nick, title, message), this::onNewThemeCreate, new QmsChatModel());
    }

    @Override
    public boolean loadData() {
        if (!super.loadData()) {
            return false;
        }
        if (currentChat.getUserId() != QmsChatModel.NOT_CREATED && currentChat.getThemeId() != QmsChatModel.NOT_CREATED) {
            refreshToolbarMenuItems(false);
            progressBar.setVisibility(View.VISIBLE);
            mainSubscriber.subscribe(RxApi.Qms().getChat(currentChat.getUserId(), currentChat.getThemeId()), this::onLoadChat, new QmsChatModel(), v -> loadData());
        }
        return true;
    }

    private void onNewThemeCreate(QmsChatModel chat) {
        themeCreator.onNewThemeCreate();
        messagePanel.clearMessage();
        messagePanel.clearAttachments();
        onLoadChat(chat);
    }

    //Chat
    private void loadBaseWebContainer() {
        MiniTemplator t = App.get().getTemplate(App.TEMPLATE_QMS_CHAT);
        App.setTemplateResStrings(t);
        t.setVariableOpt("style_type", App.get().getCssStyleType());
        t.setVariableOpt("body_type", "qms");
        t.setVariableOpt("messages", "");
        String html = t.generateOutput();
        t.reset();
        webView.loadDataWithBaseURL("https://4pda.ru/forum/", html, "text/html", "utf-8", null);
    }

    private void handleEvent(TabNotification event) {
        int themeId = event.getEvent().getSourceId();
        int messageId = event.getEvent().getMessageId();
        if (themeId == currentChat.getThemeId()) {
            switch (event.getType()) {
                case NEW:
                    Log.d(LOG_TAG, "NEW QMS MESSAGE " + themeId + " : " + messageId);
                    onNewWsMessage(themeId, messageId);
                    break;
                case READ:
                    Log.d(LOG_TAG, "THREAD READED");
                    webView.evalJs("makeAllRead();");
                    break;
            }
        }
    }

    private void onLoadChat(QmsChatModel loadedChat) {
        App.get().subscribeQms(notification);
        progressBar.setVisibility(View.GONE);
        currentChat.setThemeId(loadedChat.getThemeId());
        currentChat.setTitle(loadedChat.getTitle());
        currentChat.setUserId(loadedChat.getUserId());
        currentChat.setNick(loadedChat.getNick());
        currentChat.getMessages().addAll(loadedChat.getMessages());
        tryShowAvatar();

        MiniTemplator t = App.get().getTemplate(App.TEMPLATE_QMS_CHAT_MESS);
        App.setTemplateResStrings(t);
        int end = currentChat.getMessages().size();
        int start = Math.max(end - 30, 0);
        QmsRx.generateMess(t, currentChat.getMessages(), start, end);
        String messagesSrc = t.generateOutput();
        t.reset();
        currentChat.setShowedMessIndex(start);

        messagesSrc = QmsRx.transformMessageSrc(messagesSrc);

        Log.d(LOG_TAG, "showNewMess");
        webView.evalJs("showNewMess('".concat(messagesSrc).concat("', true)"));

        refreshToolbarMenuItems(true);
        if (currentChat.getNick() != null) {
            setSubtitle(currentChat.getNick());
        }
        if (currentChat.getTitle() != null) {
            setTitle(currentChat.getTitle());
            setTabTitle(String.format(getString(R.string.fragment_tab_title_chat), currentChat.getTitle(), currentChat.getNick()));
        }
    }


    private void onNewWsMessage(int themeId, int messageId) {
        int lastMessId = 0;
        if (!currentChat.getMessages().isEmpty()) {
            lastMessId = currentChat.getMessages().get(currentChat.getMessages().size() - 1).getId();
        }
        messageSubscriber.subscribe(
                RxApi.Qms().getMessagesFromWs(themeId, messageId, lastMessId),
                this::onNewMessages,
                new ArrayList<>());
    }

    private void checkNewMessages() {
        if (!currentChat.getMessages().isEmpty()) {
            int userId = currentChat.getUserId();
            int themeId = currentChat.getThemeId();
            int lastMessId = 0;
            if (!currentChat.getMessages().isEmpty()) {
                lastMessId = currentChat.getMessages().get(currentChat.getMessages().size() - 1).getId();
            }
            messageSubscriber.subscribe(
                    RxApi.Qms().getMessagesAfter(userId, themeId, lastMessId),
                    this::onNewMessages,
                    new ArrayList<>());
        }
    }

    private void onNewMessages(ArrayList<QmsMessage> qmsMessage) {
        Log.d(LOG_TAG, "Returned messages " + qmsMessage.size());
        if (!qmsMessage.isEmpty()) {
            MiniTemplator t = App.get().getTemplate(App.TEMPLATE_QMS_CHAT_MESS);
            App.setTemplateResStrings(t);
            for (int i = 0; i < qmsMessage.size(); i++) {
                QmsMessage message = qmsMessage.get(i);
                for (QmsMessage viewmessage : currentChat.getMessages()) {
                    if (viewmessage.getId() == message.getId()) {
                        return;
                    }
                }
                currentChat.addMessage(message);
                QmsRx.generateMess(t, message);
            }
            String messagesSrc = t.generateOutput();
            t.reset();
            messagesSrc = QmsRx.transformMessageSrc(messagesSrc);
            webView.evalJs("showNewMess('".concat(messagesSrc).concat("', true)"));
        }
    }


    private void sendMessage() {
        messagePanel.setProgressState(true);
        addUnusedAttachments();
        messageSubscriber.subscribe(RxApi.Qms().sendMessage(currentChat.getUserId(), currentChat.getThemeId(), messagePanel.getMessage()), qmsMessage -> {
            messagePanel.setProgressState(false);
            if (!qmsMessage.isEmpty() && qmsMessage.get(0).getContent() != null) {
                //Empty because result returned from websocket
                messagePanel.clearMessage();
                messagePanel.clearAttachments();
            }
        }, new ArrayList<>());
    }


    private void tryShowAvatar() {
        toolbarImageView.setContentDescription(getString(R.string.user_avatar));
        if (currentChat.getUserId() != QmsChatModel.NOT_CREATED) {
            toolbarImageView.setOnClickListener(view1 -> IntentHandler.handle("https://4pda.ru/forum/index.php?showuser=" + currentChat.getUserId()));
        }
        if (currentChat.getAvatarUrl() != null) {
            ImageLoader.getInstance().displayImage(currentChat.getAvatarUrl(), toolbarImageView);
            toolbarImageView.setVisibility(View.VISIBLE);
        } else if (currentChat.getNick() != null) {
            Observable.fromCallable(() -> ForumUsersCache.loadUserByNick(currentChat.getNick()))
                    .onErrorReturn(throwable -> new ForumUser())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(forumUser -> {
                        if (forumUser.getAvatar() != null && !forumUser.getAvatar().isEmpty()) {
                            ImageLoader.getInstance().displayImage(forumUser.getAvatar(), toolbarImageView);
                            toolbarImageView.setVisibility(View.VISIBLE);
                        }
                    });
        } else {
            toolbarImageView.setVisibility(View.GONE);
        }
    }

    @JavascriptInterface
    public void showMoreMess() {
        if (getContext() == null)
            return;
        MiniTemplator t = App.get().getTemplate(App.TEMPLATE_QMS_CHAT_MESS);
        App.setTemplateResStrings(t);
        int endIndex = currentChat.getShowedMessIndex();
        int startIndex = Math.max(endIndex - 30, 0);
        currentChat.setShowedMessIndex(startIndex);
        QmsRx.generateMess(t, currentChat.getMessages(), startIndex, endIndex);
        String messagesSrc = t.generateOutput();
        t.reset();
        messagesSrc = QmsRx.transformMessageSrc(messagesSrc);
        webView.evalJs("showMoreMess('" + messagesSrc + "')");
    }

    @Override
    public void onDomContentComplete(final ArrayList<String> actions) {
    }

    @Override
    public void onPageComplete(final ArrayList<String> actions) {
    }

    /* ATTACHMENTS LOADER */

    private Subscriber<List<AttachmentItem>> attachmentSubscriber = new Subscriber<>(this);

    public void uploadFiles(List<RequestFile> files) {
        List<AttachmentItem> pending = attachmentsPopup.preUploadFiles(files);
        attachmentSubscriber.subscribe(RxApi.Qms().uploadFiles(files, pending), items -> attachmentsPopup.onUploadFiles(items), new ArrayList<>(), null);
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
        App.get().checkStoragePermission(() -> startActivityForResult(FilePickHelper.pickFile(true), REQUEST_PICK_FILE), App.getActivity());
    }

    @Override
    public boolean onBackPressed() {
        super.onBackPressed();
        return messagePanel.onBackPressed();
    }

    @Override
    public void onResume() {
        super.onResume();
        messagePanel.onResume();
        if (currentChat.getUserId() != QmsChatModel.NOT_CREATED && currentChat.getThemeId() != QmsChatModel.NOT_CREATED) {
            App.get().subscribeQms(notification);
            checkNewMessages();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        App.get().removePreferenceChangeObserver(chatPreferenceObserver);
        App.get().unSubscribeQms(notification);
        messagePanel.onDestroy();
        unregisterForContextMenu(webView);
        webView.removeJavascriptInterface(JS_INTERFACE);
        webView.setJsLifeCycleListener(null);
        webView.endWork();
        getMainActivity().getWebViewsProvider().push(webView);
    }

    @Override
    public void onPause() {
        super.onPause();
        App.get().unSubscribeQms(notification);
        messagePanel.onPause();
    }

    @Override
    public void hidePopupWindows() {
        super.hidePopupWindows();
        messagePanel.hidePopupWindows();
    }
}
