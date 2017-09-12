package forpdateam.ru.forpda.rxapi.apiclasses;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import biz.source_code.miniTemplator.MiniTemplator;
import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.api.RequestFile;
import forpdateam.ru.forpda.api.Utils;
import forpdateam.ru.forpda.api.others.user.ForumUser;
import forpdateam.ru.forpda.api.qms.models.QmsChatModel;
import forpdateam.ru.forpda.api.qms.models.QmsContact;
import forpdateam.ru.forpda.api.qms.models.QmsMessage;
import forpdateam.ru.forpda.api.qms.models.QmsThemes;
import forpdateam.ru.forpda.api.theme.editpost.models.AttachmentItem;
import forpdateam.ru.forpda.api.theme.models.ThemePage;
import forpdateam.ru.forpda.api.theme.models.ThemePost;
import forpdateam.ru.forpda.rxapi.ForumUsersCache;
import io.reactivex.Observable;

/**
 * Created by radiationx on 25.03.17.
 */

public class QmsRx {
    //Common
    public Observable<List<ForumUser>> findUser(final String nick) {
        return Observable.fromCallable(() -> Api.Qms().findUser(nick));
    }

    public Observable<ArrayList<QmsContact>> blockUser(String nick) {
        return Observable.fromCallable(() -> Api.Qms().blockUser(nick));
    }

    public Observable<ArrayList<QmsContact>> unBlockUsers(int[] userIds) {
        return Observable.fromCallable(() -> Api.Qms().unBlockUsers(userIds));
    }

    //Contacts
    public Observable<ArrayList<QmsContact>> getContactList() {
        return Observable.fromCallable(() -> interceptContacts(Api.Qms().getContactList()));
    }

    public Observable<ArrayList<QmsContact>> getBlackList() {
        return Observable.fromCallable(() -> Api.Qms().getBlackList());
    }

    public Observable<String> deleteDialog(int mid) {
        return Observable.fromCallable(() -> Api.Qms().deleteDialog(mid));
    }


    //Themes
    public Observable<QmsThemes> getThemesList(int id) {
        return Observable.fromCallable(() -> Api.Qms().getThemesList(id));
    }

    public Observable<QmsThemes> deleteTheme(int id, int themeId) {
        return Observable.fromCallable(() -> Api.Qms().deleteTheme(id, themeId));
    }


    //Chat
    public Observable<QmsChatModel> getChat(final int userId, final int themeId) {
        return Observable.fromCallable(() -> transform(Api.Qms().getChat(userId, themeId), false));
    }

    public Observable<QmsChatModel> sendNewTheme(String nick, String title, String mess) {
        return Observable.fromCallable(() -> transform(Api.Qms().sendNewTheme(nick, title, mess), false));
    }

    public Observable<ArrayList<QmsMessage>> sendMessage(int userId, int themeId, String text) {
        return Observable.fromCallable(() -> Api.Qms().sendMessage(userId, themeId, text));
    }

    public Observable<ArrayList<QmsMessage>> getMessagesFromWs(int themeId, int messageId, int afterMessageId) {
        return Observable.fromCallable(() -> Api.Qms().getMessagesFromWs(themeId, messageId, afterMessageId));
    }

    public Observable<List<AttachmentItem>> uploadFiles(List<RequestFile> files, List<AttachmentItem> pending) {
        return Observable.fromCallable(() -> Api.Qms().uploadFiles(files, pending));
    }

    public static ArrayList<QmsContact> interceptContacts(ArrayList<QmsContact> contacts) throws Exception {
        List<ForumUser> forumUsers = new ArrayList<>();
        for (QmsContact post : contacts) {
            ForumUser forumUser = new ForumUser();
            forumUser.setId(post.getId());
            forumUser.setNick(post.getNick());
            forumUser.setAvatar(post.getAvatar());
        }
        ForumUsersCache.saveUsers(forumUsers);
        return contacts;
    }

    public static QmsChatModel transform(QmsChatModel chatModel, boolean withHtml) throws Exception {
        if (withHtml) {
            MiniTemplator t = App.getInstance().getTemplate(App.TEMPLATE_QMS_CHAT);
            App.setTemplateResStrings(t);
            t.setVariableOpt("style_type", App.getInstance().getCssStyleType());
            t.setVariableOpt("chat_title", Utils.htmlEncode(chatModel.getTitle()));
            t.setVariableOpt("chatId", chatModel.getThemeId());
            t.setVariableOpt("userId", chatModel.getUserId());
            t.setVariableOpt("nick", chatModel.getNick());
            t.setVariableOpt("avatarUrl", chatModel.getAvatarUrl());

            int endIndex = chatModel.getMessages().size();
            int startIndex = Math.max(endIndex - 30, 0);
            chatModel.setShowedMessIndex(startIndex);
            MiniTemplator messTemp = App.getInstance().getTemplate(App.TEMPLATE_QMS_CHAT_MESS);
            App.setTemplateResStrings(t);
            generateMess(messTemp, chatModel.getMessages(), startIndex, endIndex);
            t.setVariableOpt("messages", messTemp.generateOutput());
            messTemp.reset();
            chatModel.setHtml(t.generateOutput());
            t.reset();
        }
        return chatModel;
    }

    public static MiniTemplator generateMess(MiniTemplator t, List<QmsMessage> messages) {
        return generateMess(t, messages, 0, messages.size());
    }

    public static MiniTemplator generateMess(MiniTemplator t, List<QmsMessage> messages, int start, int end) {
        for (int i = start; i < end; i++) {
            QmsMessage mess = messages.get(i);
            generateMess(t, mess);
        }
        return t;
    }

    public static MiniTemplator generateMess(MiniTemplator t, QmsMessage mess) {
        if (mess.isDate()) {
            t.setVariableOpt("date", mess.getDate());
            t.addBlockOpt("date");
        } else {
            t.setVariableOpt("from_class", mess.isMyMessage() ? "our" : "his");
            t.setVariableOpt("unread_class", mess.getReadStatus() ? "" : "unread");
            t.setVariableOpt("mess_id", mess.getId());
            t.setVariableOpt("content", mess.getContent());
            t.setVariableOpt("time", mess.getTime());
            t.addBlockOpt("mess");
        }
        t.addBlockOpt("item");

        return t;
    }

    public static String transformMessageSrc(String messagesSrc) {
        messagesSrc = messagesSrc.replaceAll("\n", "").replaceAll("'", "&apos;");
        messagesSrc = JSONObject.quote(messagesSrc);
        messagesSrc = messagesSrc.substring(1, messagesSrc.length() - 1);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("src", messagesSrc);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return messagesSrc;
    }
}
