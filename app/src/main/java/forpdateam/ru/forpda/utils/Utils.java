package forpdateam.ru.forpda.utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.utils.ourparser.Html;

import static android.content.Context.CLIPBOARD_SERVICE;

/**
 * Created by isanechek on 30.07.16.
 */

public class Utils {
    public static boolean isMM() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    public static void copyToClipBoard(String s) {
        ClipboardManager clipboard = (ClipboardManager) App.getContext().getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("label", s);
        clipboard.setPrimaryClip(clip);
    }

    public static <T> T checkNotNull(T value, String message) {
        if (value == null) {
            throw new NullPointerException(message);
        }
        return value;
    }

    public static <T> T checkNotNull(T value) {
        if (value == null) {
            throw new NullPointerException();
        }
        return value;
    }

    public static void log(@NonNull String msg) {
        Log.e("TEST", msg);
    }

    public static String fromHtml(String s) {
        if (s == null) return null;
        return Html.fromHtml(s).toString();
    }
}
