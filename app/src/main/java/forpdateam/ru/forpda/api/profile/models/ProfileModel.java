package forpdateam.ru.forpda.api.profile.models;

import android.util.Pair;

import java.util.ArrayList;
import java.util.List;

import forpdateam.ru.forpda.api.profile.interfaces.IProfileModel;

/**
 * Created by radiationx on 03.08.16.
 */
public class ProfileModel implements IProfileModel {
    private String avatar, nick, status, group, regDate, alerts, onlineDate, sign, gender, birthday, userTime, note;
    private ArrayList<Pair<String, String>> contacts = new ArrayList<>();
    private ArrayList<Pair<String, String>> devices = new ArrayList<>();
    private Pair<String, String> karma, sitePosts, comments, reputation, topics, posts;

    @Override
    public String getAvatar() {
        return avatar;
    }

    @Override
    public String getNick() {
        return nick;
    }

    @Override
    public String getStatus() {
        return status;
    }

    @Override
    public String getGroup() {
        return group;
    }

    @Override
    public String getRegDate() {
        return regDate;
    }

    @Override
    public String getAlerts() {
        return alerts;
    }

    @Override
    public String getOnlineDate() {
        return onlineDate;
    }

    @Override
    public String getSign() {
        return sign;
    }

    @Override
    public String getGender() {
        return gender;
    }

    @Override
    public String getBirthDay() {
        return birthday;
    }

    @Override
    public String getUserTime() {
        return userTime;
    }

    @Override
    public ArrayList<Pair<String, String>> getContacts() {
        return contacts;
    }

    @Override
    public ArrayList<Pair<String, String>> getDevices() {
        return devices;
    }

    @Override
    public Pair<String, String> getKarma() {
        return karma;
    }

    @Override
    public Pair<String, String> getSitePosts() {
        return sitePosts;
    }

    @Override
    public Pair<String, String> getComments() {
        return comments;
    }

    @Override
    public Pair<String, String> getReputation() {
        return reputation;
    }

    @Override
    public Pair<String, String> getTopics() {
        return topics;
    }

    @Override
    public Pair<String, String> getPosts() {
        return posts;
    }

    @Override
    public String getNote() {
        return note;
    }

    @Override
    public void setAvatar(String arg) {
        avatar = arg;
    }

    @Override
    public void setNick(String arg) {
        nick = arg;
    }

    @Override
    public void setStatus(String arg) {
        status = arg;
    }

    @Override
    public void setGroup(String arg) {
        group = arg;
    }

    @Override
    public void setRegDate(String arg) {
        regDate = arg;
    }

    @Override
    public void setAlerts(String arg) {
        alerts = arg;
    }

    @Override
    public void setOnlineDate(String arg) {
        onlineDate = arg;
    }

    @Override
    public void setSign(String arg) {
        sign = arg;
    }

    @Override
    public void setGender(String arg) {
        gender = arg;
    }

    @Override
    public void setBirthDay(String arg) {
        birthday = arg;
    }

    @Override
    public void setUserTime(String arg) {
        userTime = arg;
    }

    @Override
    public void addContact(Pair<String, String> arg) {
        contacts.add(arg);
    }

    @Override
    public void addDevice(Pair<String, String> arg) {
        devices.add(arg);
    }

    @Override
    public void setKarma(Pair<String, String> arg) {
        karma = arg;
    }

    @Override
    public void setSitePosts(Pair<String, String> arg) {
        sitePosts = arg;
    }

    @Override
    public void setComments(Pair<String, String> arg) {
        comments = arg;
    }

    @Override
    public void setReputation(Pair<String, String> arg) {
        reputation = arg;
    }

    @Override
    public void setTopics(Pair<String, String> arg) {
        topics = arg;
    }

    @Override
    public void setPosts(Pair<String, String> arg) {
        posts = arg;
    }

    @Override
    public void setNote(String arg) {
        note = arg;
    }
}
