package forpdateam.ru.forpda.fragments;

/**
 * Created by radiationx on 07.08.16.
 */
public interface ITabFragment {
    String getTitle();

    String getDefaultUrl();

    String getParentTag();

    int getUID();

    void setUID();

    boolean isAlone();

    boolean onBackPressed();

    void hidePopupWindows();
}
