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

    public static String escapeNewLine(String s) {
        StringBuilder sb = new StringBuilder();
        char c;
        final int length = s.length();
        for (int i = 0; i < length; i++) {
            c = s.charAt(i);
            if (c == '\n') {
                sb.append("<br>");
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
