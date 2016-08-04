package forpdateam.ru.forpda.api.theme.interfaces;

/**
 * Created by radiationx on 04.08.16.
 */
public interface IThemePost {
    String getId();

    String getDate();

    String getNumber();

    String getUserAvatar();

    String getUserName();

    String getGroupColor();

    String getGroup();

    String getUserId();

    String getReputation();

    String getBody();

    boolean isCurator();

    boolean isOnline();

    boolean canMinusRep();

    boolean canPlusRep();

    boolean canReport();

    boolean canEdit();

    boolean canDelete();

    boolean canQuote();
}
