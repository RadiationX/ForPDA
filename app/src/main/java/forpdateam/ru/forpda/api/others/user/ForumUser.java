package forpdateam.ru.forpda.api.others.user;

/**
 * Created by radiationx on 08.07.17.
 */

public class ForumUser implements IForumUser {
    private int id = 0;
    private String nick = "";
    private String avatar = "";

    public ForumUser() {
    }

    public ForumUser(IForumUser forumUser) {
        this.id = forumUser.getId();
        this.nick = forumUser.getNick();
        this.avatar = forumUser.getAvatar();
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getNick() {
        return nick;
    }

    @Override
    public String getAvatar() {
        return avatar;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}
