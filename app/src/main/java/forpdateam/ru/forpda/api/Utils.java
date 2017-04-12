package forpdateam.ru.forpda.api;

import android.text.TextUtils;

import forpdateam.ru.forpda.utils.ourparser.Html;

/**
 * Created by radiationx on 26.03.17.
 */

public class Utils {
    public static String fromHtml(String s) {
        if (s == null) return null;
        return Html.fromHtml(s).toString();
    }

    public static String htmlEncode(String s) {
        if (s == null) return null;
        return TextUtils.htmlEncode(s);
    }
}
