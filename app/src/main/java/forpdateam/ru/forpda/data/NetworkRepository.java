package forpdateam.ru.forpda.data;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;

import forpdateam.ru.forpda.api.profile.models.ProfileModel;
import forpdateam.ru.forpda.api.qms.Qms;
import forpdateam.ru.forpda.api.qms.models.QmsChatModel;
import forpdateam.ru.forpda.api.qms.models.QmsContact;
import forpdateam.ru.forpda.api.qms.models.QmsThemes;
import forpdateam.ru.forpda.api.theme.models.ThemePage;
import io.reactivex.Observable;

/**
 * Created by isanechek on 11/10/16.
 */

public class NetworkRepository implements DataSource.NetworkDataSource {

    @Nullable
    private static NetworkRepository INSTANCE = null;

    @Nullable
    private final Qms qms;


    public NetworkRepository() {
        qms = new Qms();

    }

    public static NetworkRepository getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new NetworkRepository();
        }
        return INSTANCE;
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

    @Override
    public Observable<String> deleteDialog(@NonNull String mid) {
        return null;
    }

    @Override
    public Observable<String> sendNewTheme(@NonNull String nick, @NonNull String title, @NonNull String mess) {
        return null;
    }

    @Override
    public Observable<String[]> search(@NonNull String nick) {
        return null;
    }

    @Override
    public Observable<QmsChatModel> getChat(@NonNull String userId, @NonNull String themeId) {
        return null;
    }

    @Override
    public Observable<QmsThemes> getThemesList(@NonNull String id) {
        return null;
    }

    @Override
    public Observable<ArrayList<QmsContact>> getContactList() {
        return null;
    }
}
