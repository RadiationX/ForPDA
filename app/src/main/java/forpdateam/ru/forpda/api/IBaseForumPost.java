package forpdateam.ru.forpda.api;

/**
 * Created by radiationx on 27.04.17.
 */

public interface IBaseForumPost {
    int getId();

    String getDate();

    int getNumber();

    String getAvatar();

    String getNick();

    String getGroupColor();

    String getGroup();

    int getUserId();

    String getReputation();

    String getBody();

    boolean isCurator();

    boolean isOnline();

    boolean canMinusRep();

    boolean canPlusRep();

    boolean canReport();

    boolean canEdit();

    boolean canDelete();
}
