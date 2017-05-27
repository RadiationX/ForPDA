package forpdateam.ru.forpda.views.messagepanel.advanced;

import android.support.annotation.DrawableRes;

/**
 * Created by radiationx on 08.01.17.
 */

public class ButtonData {
    private String text, icon, title;
    private int iconRes;
    private ClickListener listener;

    public interface ClickListener {
        void onClick(ButtonData data);
    }

    public ButtonData(String text, String icon) {
        this.text = text;
        this.icon = icon;
    }

    public ButtonData(String text, @DrawableRes int iconRes) {
        this.text = text;
        this.iconRes = iconRes;
    }

    public ButtonData(String text, @DrawableRes int iconRes, String title) {
        this.text = text;
        this.iconRes = iconRes;
        this.title = title;
    }

    public ButtonData(String text, @DrawableRes int iconRes, ClickListener listener) {
        this.text = text;
        this.iconRes = iconRes;
        this.listener = listener;
    }

    public String getText() {
        return text;
    }

    public String getIcon() {
        return icon;
    }

    public int getIconRes() {
        return iconRes;
    }

    public String getTitle() {
        return title;
    }

    public ClickListener getListener() {
        return listener;
    }
}
