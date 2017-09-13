package forpdateam.ru.forpda.rxapi;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.api.others.user.ForumUser;
import forpdateam.ru.forpda.data.realm.ForumUserBd;
import io.realm.Realm;

/**
 * Created by radiationx on 08.07.17.
 */

public class ForumUsersCache {
    public static void saveUsers(ForumUser forumUser) {
        List<ForumUser> forumUsers = new ArrayList<>();
        forumUsers.add(forumUser);
        saveUsers(forumUsers);
    }

    public static void saveUsers(List<ForumUser> forumUsers) {
        Realm realmInstance = Realm.getDefaultInstance();
        realmInstance.executeTransaction(realm -> {
            List<ForumUserBd> bdList = new ArrayList<>();
            for (ForumUser item : forumUsers) {
                bdList.add(new ForumUserBd(item));
            }
            realm.insertOrUpdate(bdList);
        });
        realmInstance.close();
    }

    public static ForumUser getUserById(int id) {
        ForumUser resultUser = null;
        Realm realmInstance = Realm.getDefaultInstance();
        ForumUserBd realmResult = realmInstance.where(ForumUserBd.class).equalTo("id", id).findFirst();
        if (realmResult != null) {
            resultUser = new ForumUser(realmResult);
        }
        realmInstance.close();
        return resultUser;
    }

    public static ForumUser loadUserByNick(String nick) throws Exception {
        ForumUser resultUser = null;

        Realm realmInstance = Realm.getDefaultInstance();
        ForumUserBd realmResult = realmInstance.where(ForumUserBd.class).equalTo("nick", nick).findFirst();
        if (realmResult != null) {
            resultUser = new ForumUser(realmResult);
        }
        realmInstance.close();
        if (realmResult != null) {
            return resultUser;
        }

        List<ForumUser> loadedForumUsers = Api.Qms().findUser(nick);
        for (ForumUser user : loadedForumUsers) {
            Log.d("SUKA", "COMPARE " + nick + " : " + user.getNick());
            if (nick.equals(user.getNick())) {
                Log.d("SUKA", "COMPARE YEEEEAAA" + nick + " : " + user.getNick());
                resultUser = user;
                break;
            }
        }
        if (resultUser != null) {
            loadedForumUsers.clear();
            loadedForumUsers.add(resultUser);
            saveUsers(loadedForumUsers);
        }

        return resultUser;
    }
}
