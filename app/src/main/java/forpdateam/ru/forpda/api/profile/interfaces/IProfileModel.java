package forpdateam.ru.forpda.api.profile.interfaces;

import android.text.Spanned;
import android.util.Pair;

import java.util.ArrayList;

import forpdateam.ru.forpda.api.profile.models.ProfileModel;

/**
 * Created by radiationx on 03.08.16.
 */
public interface IProfileModel {
    String getAvatar();

    String getNick();

    String getStatus();

    String getGroup();

    String getRegDate();

    String getAlerts();

    String getOnlineDate();

    Spanned getSign();

    String getGender();

    String getBirthDay();

    String getUserTime();

    ArrayList<Pair<String, String>> getContacts();

    ArrayList<ProfileModel.Device> getDevices();

    Pair<String, String> getKarma();

    Pair<String, String> getSitePosts();

    Pair<String, String> getComments();

    Pair<String, String> getReputation();

    Pair<String, String> getTopics();

    Pair<String, String> getPosts();

    String getNote();

    Spanned getAbout();

    void setAvatar(String arg);

    void setNick(String arg);

    void setStatus(String arg);

    void setGroup(String arg);

    void setRegDate(String arg);

    void setAlerts(String arg);

    void setOnlineDate(String arg);

    void setSign(Spanned arg);

    void setGender(String arg);

    void setBirthDay(String arg);

    void setUserTime(String arg);

    void addContact(Pair<String, String> arg);

    void addDevice(Pair<String, String> arg);

    void setKarma(Pair<String, String> arg);

    void setSitePosts(Pair<String, String> arg);

    void setComments(Pair<String, String> arg);

    void setReputation(Pair<String, String> arg);

    void setTopics(Pair<String, String> arg);

    void setPosts(Pair<String, String> arg);

    void setNote(String arg);

    void setAbout(Spanned arg);
}
