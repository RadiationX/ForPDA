package forpdateam.ru.forpda.api.theme.models;

import forpdateam.ru.forpda.api.theme.interfaces.IThemePost;

/**
 * Created by radiationx on 04.08.16.
 */
public class ThemePost implements IThemePost {
    private String date, avatar, nick, groupColor, group, reputation, body;
    private boolean curator, online, minus, plus, report, edit, delete, qoute;
    private int id, number, userId;

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getDate() {
        return date;
    }

    @Override
    public int getNumber() {
        return number;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getNick() {
        return nick;
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
    public int getUserId() {
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

    public void setId(int id) {
        this.id = id;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public void setGroupColor(String groupColor) {
        this.groupColor = groupColor;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public void setUserId(int userId) {
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
