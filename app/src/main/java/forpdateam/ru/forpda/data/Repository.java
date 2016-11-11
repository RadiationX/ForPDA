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
import forpdateam.ru.forpda.fragments.news.NewsModel;
import io.reactivex.Observable;
import io.realm.RealmResults;

import static forpdateam.ru.forpda.Constants.CNBN;
import static forpdateam.ru.forpda.utils.Utils.checkNotNull;

/**
 * Created by isanechek on 11/2/16.
 */

public class Repository implements DataSource.NetworkDataSource, DataSource.LocalDataSource {

    @Nullable
    private static Repository INSTANCE = null;


    @NonNull
    private final DataSource.LocalDataSource mLocalDataSource;

    private Repository(@NonNull DataSource.LocalDataSource localDataSource) {
        this.mLocalDataSource = checkNotNull(localDataSource, "Local Repository " + CNBN);
    }

    public static Repository getInstance(@NonNull DataSource.LocalDataSource localDataSource) {
        if (INSTANCE == null) {
            INSTANCE = new Repository(localDataSource);
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }


    /**
     * News
     */

    @Override
    public Observable<RealmResults<NewsModel>> getListNewsAll() {



        return null;
    }

    private void checkNewNews(RealmResults<NewsModel> results) {

    }

    @Override
    public Observable<RealmResults<NewsModel>> getListNewsCategory(@NonNull String category) {
        checkNotNull(category, "Get List News Category! Category Cannot Be Null");

        return null;
    }

    @Override
    public Observable<String> getNews(@NonNull String newsUrl) {
        checkNotNull(newsUrl);
      return null;
    }

    @Override
    public void refreshNewsList(@NonNull String category) {
        checkNotNull(category);
    }

    @Override
    public void readNewsState(@NonNull String idNews) {
        checkNotNull(idNews, "Read News State! Id News Cannot Be Null");

    }

    @Override
    public void saveOfflineNews(@NonNull String idNews, @NonNull String source) {
        checkNotNull(idNews, "Save Offline! Id News Cannot Be Null");
        checkNotNull(source, "Save Offline! Source News Cannot Be Null");

    }

    /*Theme*/
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
    public void saveThemeLastPostUrl(@NonNull String idTheme, @NonNull String postUrl) {

    }

    @Override
    public Observable<String> getThemeLastPostUrl(@NonNull String idTheme) {
        return null;
    }

    @Override
    public void deleteThemeLastPostUrl(@NonNull String idTheme) {

    }

    /*QMS*/
    @Override
    public Observable<String> deleteDialog(@NonNull int mid) {
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
    public Observable<QmsChatModel> getChat(@NonNull int userId, @NonNull int themeId) {
        checkNotNull(userId, "Get Chat! User Id " + CNBN);
        checkNotNull(themeId, "Get Chat! Theme Id " + CNBN);
        return Api.Qms().getChat(userId, themeId);
    }

    @Override
    public Observable<QmsThemes> getThemesList(@NonNull int id) {
        checkNotNull(id, "Get Theme List! Id " + CNBN);
        return Api.Qms().getThemesList(id);
    }

    @Override
    public Observable<ArrayList<QmsContact>> getContactList() {
        return Api.Qms().getContactList();
    }

    /*Profile*/
    @Override
    public Observable<ProfileModel> getProfile(@NonNull String url) {
        checkNotNull(url);
        return Api.Profile().get(url);
    }

    @Override
    public Observable<Boolean> saveNoteRx(@NonNull String note) {
        checkNotNull(note);
        return Api.Profile().saveNoteRx(note);
    }
}
