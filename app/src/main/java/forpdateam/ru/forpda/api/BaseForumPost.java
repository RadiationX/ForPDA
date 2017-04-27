package forpdateam.ru.forpda.api;

/**
 * Created by radiationx on 27.04.17.
 */

public class BaseForumPost implements IBaseForumPost {
    private String date, avatar, nick, groupColor = "black", group, reputation, body;
    private boolean curator, online, minus, plus, report, edit, delete, quote;
    private int id = 0, topicId = 0, forumId = 0, number = 0, userId = 0;

    @Override
    public int getTopicId() {
        return topicId;
    }

    @Override
    public int getForumId() {
        return forumId;
    }

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

    @Override
    public String getAvatar() {
        return avatar;
    }

    @Override
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
        return quote;
    }

    public void setTopicId(int topicId) {
        this.topicId = topicId;
    }

    public void setForumId(int forumId) {
        this.forumId = forumId;
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
        if (groupColor == null)
            return;
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

    public void setCanQuote(boolean quote) {
        this.quote = quote;
    }
}
