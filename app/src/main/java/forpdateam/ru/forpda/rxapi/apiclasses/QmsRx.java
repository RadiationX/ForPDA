package forpdateam.ru.forpda.rxapi.apiclasses;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import biz.source_code.miniTemplator.MiniTemplator;
import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.api.RequestFile;
import forpdateam.ru.forpda.api.Utils;
import forpdateam.ru.forpda.api.qms.models.QmsChatModel;
import forpdateam.ru.forpda.api.qms.models.QmsContact;
import forpdateam.ru.forpda.api.qms.models.QmsMessage;
import forpdateam.ru.forpda.api.qms.models.QmsThemes;
import forpdateam.ru.forpda.api.search.models.SearchItem;
import forpdateam.ru.forpda.api.search.models.SearchResult;
import forpdateam.ru.forpda.api.theme.editpost.models.AttachmentItem;
import forpdateam.ru.forpda.client.ClientHelper;
import io.reactivex.Observable;

/**
 * Created by radiationx on 25.03.17.
 */

public class QmsRx {
    public Observable<ArrayList<QmsContact>> getContactList() {
        return Observable.fromCallable(() -> Api.Qms().getContactList());
    }

    public Observable<QmsThemes> getThemesList(final int id) {
        return Observable.fromCallable(() -> Api.Qms().getThemesList(id));
    }

    public Observable<QmsChatModel> getChat(final int userId, final int themeId) {
        return Observable.fromCallable(() -> transform(Api.Qms().getChat(userId, themeId), true));
    }

    public Observable<String[]> findUser(final String nick) {
        return Observable.fromCallable(() -> Api.Qms().findUser(nick));
    }

    public Observable<QmsChatModel> sendNewTheme(String nick, String title, String mess) {
        return Observable.fromCallable(() -> Api.Qms().sendNewTheme(nick, title, mess));
    }

    public Observable<QmsMessage> sendMessage(int userId, int themeID, String text) {
        return Observable.fromCallable(() -> Api.Qms().sendMessage(userId, themeID, text));
    }

    public Observable<String> deleteDialog(int mid) {
        return Observable.fromCallable(() -> Api.Qms().deleteDialog(mid));
    }

    public Observable<ArrayList<QmsContact>> getBlackList() {
        return Observable.fromCallable(() -> Api.Qms().getBlackList());
    }

    public Observable<ArrayList<QmsContact>> blockUser(String nick) {
        return Observable.fromCallable(() -> Api.Qms().blockUser(nick));
    }

    public Observable<ArrayList<QmsContact>> unBlockUsers(int[] userIds) {
        return Observable.fromCallable(() -> Api.Qms().unBlockUsers(userIds));
    }

    public Observable<List<AttachmentItem>> uploadFiles(List<RequestFile> files) {
        return Observable.fromCallable(() -> Api.Qms().uploadFiles(files));
    }

    public static QmsChatModel transform(QmsChatModel chatModel, boolean withHtml) throws Exception {
        if (withHtml) {
            long time = System.currentTimeMillis();
            MiniTemplator t = App.getInstance().getTemplate(App.TEMPLATE_QMS_CHAT);

            t.setVariableOpt("chat_title", Utils.htmlEncode(chatModel.getTitle()));
            t.setVariableOpt("chatId", chatModel.getThemeId());
            t.setVariableOpt("userId", chatModel.getUserId());
            t.setVariableOpt("nick", chatModel.getNick());
            t.setVariableOpt("avatarUrl", chatModel.getAvatarUrl());


            Log.d("FORPDA_LOG", "template check 2 " + (System.currentTimeMillis() - time));
            int size = chatModel.getChatItemsList().size();
            chatModel.setLastShowedMess(chatModel.getChatItemsList().get(Math.max(size - 20, 0)));
            for (int i = Math.max(size - 20, 0); i < size; i++) {
                QmsMessage mess = chatModel.getChatItemsList().get(i);
                if (mess.isDate()) continue;
                t.setVariableOpt("from_class", mess.isMyMessage() ? "our" : "his");
                t.setVariableOpt("mess_id", mess.getId());
                t.setVariableOpt("content", mess.getContent());
                t.setVariableOpt("date", mess.getDate());

                t.addBlockOpt("mess");
            }

            Log.d("FORPDA_LOG", "template check 3 " + (System.currentTimeMillis() - time));
            chatModel.setHtml(t.generateOutput());
            Log.d("FORPDA_LOG", "template check 4 " + (System.currentTimeMillis() - time));
            t.reset();
            Log.d("FORPDA_LOG", "template check 5 " + (System.currentTimeMillis() - time));
        }
        return chatModel;
    }
}
