package forpdateam.ru.forpda.rxapi.apiclasses;

import java.util.ArrayList;

import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.api.qms.models.QmsChatModel;
import forpdateam.ru.forpda.api.qms.models.QmsContact;
import forpdateam.ru.forpda.api.qms.models.QmsMessage;
import forpdateam.ru.forpda.api.qms.models.QmsThemes;
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
        return Observable.fromCallable(() -> Api.Qms().getChat(userId, themeId));
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
}
