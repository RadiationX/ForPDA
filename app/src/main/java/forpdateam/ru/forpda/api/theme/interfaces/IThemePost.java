package forpdateam.ru.forpda.api.theme.interfaces;

/**
 * Created by radiationx on 04.08.16.
 */
public interface IThemePost {
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
