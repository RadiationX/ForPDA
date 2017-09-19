package forpdateam.ru.forpda.api.profile.models;

import android.text.Spanned;

import java.util.ArrayList;

/**
 * Created by radiationx on 03.08.16.
 */
public class ProfileModel {
    public enum ContactType {
        QMS, WEBSITE, ICQ, TWITTER, VKONTAKTE, GOOGLE_PLUS, FACEBOOK, INSTAGRAM, TELEGRAM, MAIL_RU, JABBER, WINDOWS_LIVE
    }

    public enum InfoType {
        REG_DATE, ALERTS, ONLINE_DATE, GENDER, BIRTHDAY, USER_TIME, CITY
    }

    public enum StatType {
        SITE_KARMA, SITE_POSTS, SITE_COMMENTS, FORUM_REPUTATION, FORUM_TOPICS, FORUM_POSTS;
    }

    private Spanned sign, about;
    private String avatar, nick, status, group, note;
    private ArrayList<Contact> contacts = new ArrayList<>();
    private ArrayList<Info> info = new ArrayList<>();
    private ArrayList<Stat> stats = new ArrayList<>();
    private ArrayList<Device> devices = new ArrayList<>();

    public void addInfo(InfoType type, String value) {
        Info info = new Info();
        info.setType(type);
        info.setValue(value);
        this.info.add(info);
    }

    public void addStat(Stat stat) {
        this.stats.add(stat);
    }

    public void addContact(Contact arg) {
        contacts.add(arg);
    }

    public void addDevice(Device arg) {
        devices.add(arg);
    }

    public void setSign(Spanned sign) {
        this.sign = sign;
    }

    public void setAbout(Spanned about) {
        this.about = about;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Spanned getSign() {
        return sign;
    }

    public Spanned getAbout() {
        return about;
    }

    public ArrayList<Contact> getContacts() {
        return contacts;
    }

    public ArrayList<Info> getInfo() {
        return info;
    }

    public ArrayList<Stat> getStats() {
        return stats;
    }

    public ArrayList<Device> getDevices() {
        return devices;
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

    public String getNote() {
        return note;
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
        private String url;
        private String name;
        private String accessory;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
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
        private String value;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
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
