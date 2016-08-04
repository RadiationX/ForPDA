package forpdateam.ru.forpda.api.theme.models;

import forpdateam.ru.forpda.api.theme.interfaces.IThemePost;

/**
 * Created by radiationx on 04.08.16.
 */
public class ThemePost implements IThemePost {
    private String id, date, number, userAvatar, userName, groupColor, group, userId, reputation, body;
    private boolean curator, online, minus, plus, report, edit, delete, qoute;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getDate() {
        return date;
    }

    @Override
    public String getNumber() {
        return number;
    }

    @Override
    public String getUserAvatar() {
        return userAvatar;
    }

    @Override
    public String getUserName() {
        return userName;
    }

    @Override
    public String getGroupColor() {
        return groupColor;
    }

    @Override
    public String getGroup() {
        return group;
    }

    @Override
    public String getUserId() {
        return userId;
    }

    @Override
    public String getReputation() {
        return reputation;
    }

    @Override
    public String getBody() {
        return body;
    }

    @Override
    public boolean isCurator() {
        return curator;
    }

    @Override
    public boolean isOnline() {
        return online;
    }

    @Override
    public boolean canMinusRep() {
        return minus;
    }

    @Override
    public boolean canPlusRep() {
        return plus;
    }

    @Override
    public boolean canReport() {
        return report;
    }

    @Override
    public boolean canEdit() {
        return edit;
    }

    @Override
    public boolean canDelete() {
        return delete;
    }

    @Override
    public boolean canQuote() {
        return qoute;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public void setUserAvatar(String userAvatar) {
        this.userAvatar = userAvatar;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setGroupColor(String groupColor) {
        this.groupColor = groupColor;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setReputation(String reputation) {
        this.reputation = reputation;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setCurator(boolean curator) {
        this.curator = curator;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public void setCanMinus(boolean minus) {
        this.minus = minus;
    }

    public void setCanPlus(boolean plus) {
        this.plus = plus;
    }

    public void setCanReport(boolean report) {
        this.report = report;
    }

    public void setCanEdit(boolean edit) {
        this.edit = edit;
    }

    public void setCanDelete(boolean delete) {
        this.delete = delete;
    }

    public void setCanQoute(boolean qoute) {
        this.qoute = qoute;
    }
}
