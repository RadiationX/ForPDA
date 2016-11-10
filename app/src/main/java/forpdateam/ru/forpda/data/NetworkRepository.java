package forpdateam.ru.forpda.data;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;

import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.api.profile.models.ProfileModel;
import forpdateam.ru.forpda.api.qms.models.QmsChatModel;
import forpdateam.ru.forpda.api.qms.models.QmsContact;
import forpdateam.ru.forpda.api.qms.models.QmsThemes;
import forpdateam.ru.forpda.api.theme.models.ThemePage;
import io.reactivex.Observable;

import static forpdateam.ru.forpda.Constants.CNBN;
import static forpdateam.ru.forpda.utils.Utils.checkNotNull;

/**
 * Created by isanechek on 11/10/16.
 */

public class NetworkRepository implements DataSource.NetworkDataSource {

    @Nullable
    private static NetworkRepository INSTANCE = null;

    public static NetworkRepository getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new NetworkRepository();
        }
        return INSTANCE;
    }

    private NetworkRepository() {
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }

    @Override
    public Observable<String> changeReputation(@NonNull int postId, @NonNull int userId, @NonNull boolean type, String message) {
        return null;
    }

    @Override
    public Observable<String> votePost(@NonNull int postId, @NonNull boolean type) {
        return null;
    }

    @Override
    public Observable<Boolean> deletePost(@NonNull int postId) {
        return null;
    }

    @Override
    public Observable<String> reportPost(@NonNull int themeId, @NonNull int postId, String message) {
        return null;
    }

    @Override
    public Observable<ThemePage> getPage(@NonNull String url, @NonNull boolean generateHtml) {
        return null;
    }

    @Override
    public Observable<ThemePage> getPage(@NonNull String url) {
        return null;
    }

    @Override
    public Observable<String> getNews(@NonNull String idNews) {
        return null;
    }

    @Override
    public void refreshNewsList(@NonNull String category) {
    }

    @Override
    public Observable<ProfileModel> getProfile(@NonNull String url) {
        return null;
    }

    @Override
    public Observable<Boolean> saveNoteRx(@NonNull String note) {
        return null;
    }

    /*QMS*/
    @Override
    public Observable<String> deleteDialog(@NonNull String mid) {
        checkNotNull(mid, "Delete Dialog! Mid " + CNBN);
        return Api.Qms().deleteDialog(mid);
    }

    @Override
    public Observable<String> sendNewTheme(@NonNull String nick, @NonNull String title, @NonNull String mess) {
        checkNotNull(nick, "Send New Theme! Nick " + CNBN);
        checkNotNull(title, "Send New Theme! Title " + CNBN);
        checkNotNull(mess, "Send New Theme! Mess " + CNBN);
        return Api.Qms().sendNewTheme(nick, title, mess);
    }

    @Override
    public Observable<String[]> search(@NonNull String nick) {
        checkNotNull(nick, "Search! Nick " + CNBN);
        return Api.Qms().search(nick);
    }

    @Override
    public Observable<QmsChatModel> getChat(@NonNull String userId, @NonNull String themeId) {
        checkNotNull(userId, "Get Chat! User Id " + CNBN);
        checkNotNull(themeId, "Get Chat! Theme Id " + CNBN);
        return Api.Qms().getChat(userId, themeId);
    }

    @Override
    public Observable<QmsThemes> getThemesList(@NonNull String id) {
        checkNotNull(id, "Get Theme List! Id " + CNBN);
        return Api.Qms().getThemesList(id);
    }

    @Override
    public Observable<ArrayList<QmsContact>> getContactList() {
        return Api.Qms().getContactList();
    }
}
