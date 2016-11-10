package forpdateam.ru.forpda.data;

import android.support.annotation.NonNull;

import java.util.ArrayList;

import forpdateam.ru.forpda.api.profile.models.ProfileModel;
import forpdateam.ru.forpda.api.qms.models.QmsChatModel;
import forpdateam.ru.forpda.api.qms.models.QmsContact;
import forpdateam.ru.forpda.api.qms.models.QmsThemes;
import forpdateam.ru.forpda.api.theme.models.ThemePage;
import forpdateam.ru.forpda.fragments.news.NewsModel;
import forpdateam.ru.forpda.fragments.theme.ThemeModel;
import io.reactivex.Observable;
import io.realm.RealmResults;

/**
 * Created by isanechek on 11/2/16.
 */

public interface DataSource {


    interface LocalDataSource {
        /*Theme*/
        void deleteThemeLastPostUrl(@NonNull String idTheme);
        void saveThemeLastPostUrl(@NonNull String idTheme, @NonNull String postUrl);
        Observable<String> getThemeLastPostUrl(@NonNull String idTheme);

        /*News*/
        Observable<RealmResults<NewsModel>> getListNewsAll();
        Observable<RealmResults<NewsModel>> getListNewsCategory(@NonNull String category);
        void readNewsState(@NonNull String idNews);
        void saveOfflineNews(@NonNull String idNews, @NonNull String source);
    }

    interface NetworkDataSource {

        /*Theme*/
        Observable<String> changeReputation(@NonNull int postId, @NonNull int userId, @NonNull boolean type, String message);
        Observable<String> votePost(@NonNull int postId, @NonNull boolean type);
        Observable<Boolean> deletePost(@NonNull int postId);
        Observable<String> reportPost(@NonNull int themeId, @NonNull int postId, String message);
        Observable<ThemePage> getPage(@NonNull String url, @NonNull boolean generateHtml);
        Observable<ThemePage> getPage(@NonNull String url);

        /*News*/
        Observable<String> getNews(@NonNull String idNews);
        void refreshNewsList(@NonNull String category);

        /*Profile*/
        Observable<ProfileModel> getProfile(@NonNull String url);
        Observable<Boolean> saveNoteRx(@NonNull String note);

        /*QMS*/
        Observable<String> deleteDialog(@NonNull String mid);
        Observable<String> sendNewTheme(@NonNull String nick, @NonNull String title, @NonNull String mess);
        Observable<String[]> search(@NonNull String nick);
        Observable<QmsChatModel> getChat(@NonNull String userId, @NonNull String themeId);
        Observable<QmsThemes> getThemesList(@NonNull String id);
        Observable<ArrayList<QmsContact>> getContactList();

    }

}
