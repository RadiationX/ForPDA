package forpdateam.ru.forpda.api.profile.models;

import android.text.Spanned;
import android.util.Pair;

import java.util.ArrayList;

import forpdateam.ru.forpda.api.profile.interfaces.IProfileModel;

/**
 * Created by radiationx on 03.08.16.
 */
public class ProfileModel {
    public enum ContactType {
        QMS, WEBSITE, ICQ, TWITTER, VKONTAKTE, GOOGLE_PLUS, FACEBOOK, INSTAGRAM, TELEGRAM, MAIL_RU, JABBER, WINDOWS_LIVE
    }

    public enum InfoType {
        AVATAR, NICK, STATUS, GROUP, REG_DATE, ALERTS, ONLINDE_DATA, GENDER, BIRTHDAY, USER_TIME, CITY, NOTE
    }

    public enum StatType {
        SITE_KARMA, SITE_POSTS, SITE_COMMENTS, FORUM_REPUTATION, FORUM_TOPICS, FORUM_POSTS;
    }

    private String avatar, nick, status, group, regDate, alerts, onlineDate, gender, birthday, userTime, note, city;
    private Stat karma, sitePosts, comments, reputation, topics, posts;
    private Spanned sign, about;
    private ArrayList<Contact> contacts = new ArrayList<>();
    private ArrayList<Device> devices = new ArrayList<>();
    private ArrayList<Info> info = new ArrayList<>();
    private ArrayList<Stat> stats = new ArrayList<>();

    public void addInfo(Info info) {
        this.info.add(info);
    }

    public void addStat(Stat stat) {
        this.stats.add(stat);
    }

    public String getAvatar() {
        return avatar;
    }

    public String getNick() {
        return nick;
    }

    public String getStatus() {
        return status;
    }

    public String getGroup() {
        return group;
    }

    public String getRegDate() {
        return regDate;
    }

    public String getAlerts() {
        return alerts;
    }

    public String getOnlineDate() {
        return onlineDate;
    }

    public Spanned getSign() {
        return sign;
    }

    public String getGender() {
        return gender;
    }

    public String getBirthDay() {
        return birthday;
    }

    public String getUserTime() {
        return userTime;
    }

    public ArrayList<Contact> getContacts() {
        return contacts;
    }

    public ArrayList<Device> getDevices() {
        return devices;
    }

    public Stat getKarma() {
        return karma;
    }

    public Stat getSitePosts() {
        return sitePosts;
    }

    public Stat getComments() {
        return comments;
    }

    public Stat getReputation() {
        return reputation;
    }

    public Stat getTopics() {
        return topics;
    }

    public Stat getPosts() {
        return posts;
    }

    public String getNote() {
        return note;
    }

    public Spanned getAbout() {
        return about;
    }

    public void setAvatar(String arg) {
        avatar = arg;
    }

    public void setNick(String arg) {
        nick = arg;
    }

    public void setStatus(String arg) {
        status = arg;
    }

    public void setGroup(String arg) {
        group = arg;
    }

    public void setRegDate(String arg) {
        regDate = arg;
    }

    public void setAlerts(String arg) {
        alerts = arg;
    }

    public void setOnlineDate(String arg) {
        onlineDate = arg;
    }

    public void setSign(Spanned arg) {
        sign = arg;
    }

    public void setGender(String arg) {
        gender = arg;
    }

    public void setBirthDay(String arg) {
        birthday = arg;
    }

    public void setUserTime(String arg) {
        userTime = arg;
    }

    public void addContact(Contact arg) {
        contacts.add(arg);
    }

    public void addDevice(Device arg) {
        devices.add(arg);
    }

    public void setKarma(Stat arg) {
        karma = arg;
    }

    public void setSitePosts(Stat arg) {
        sitePosts = arg;
    }

    public void setComments(Stat arg) {
        comments = arg;
    }

    public void setReputation(Stat arg) {
        reputation = arg;
    }

    public void setTopics(Stat arg) {
        topics = arg;
    }

    public void setPosts(Stat arg) {
        posts = arg;
    }

    public void setNote(String arg) {
        note = arg;
    }

    public void setAbout(Spanned arg) {
        about = arg;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public static class Info {
        private InfoType type;
        private String value;

        public InfoType getType() {
            return type;
        }

        public void setType(InfoType type) {
            this.type = type;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    public static class Contact {
        private ContactType type = ContactType.WEBSITE;
        private String url;
        private String title;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public ContactType getType() {
            return type;
        }

        public void setType(ContactType type) {
            this.type = type;
        }
    }

    public static class Device {
        private String id;
        private String name;
        private String accessory;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getAccessory() {
            return accessory;
        }

        public void setAccessory(String accessory) {
            this.accessory = accessory;
        }
    }

    public static class Stat {
        private StatType type;
        private String url;
        private int value;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }

        public StatType getType() {
            return type;
        }

        public void setType(StatType type) {
            this.type = type;
        }
    }
}
