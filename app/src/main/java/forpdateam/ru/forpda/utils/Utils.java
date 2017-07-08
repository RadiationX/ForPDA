package forpdateam.ru.forpda.utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import forpdateam.ru.forpda.App;

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

    public static void longLog(String msg){
        int maxLogSize = 1000;
        for (int i = 0; i <= msg.length() / maxLogSize; i++) {
            int start = i * maxLogSize;
            int end = (i + 1) * maxLogSize;
            end = end > msg.length() ? msg.length() : end;
            Log.v("FORPDA_LOG", msg.substring(start, end));
        }
    }
    public static void log(@NonNull String msg) {
        Log.e("TEST", msg);
    }

    public static String fromHtml(String s) {
        if (s == null) return null;
        return Html.fromHtml(s, Html.FROM_HTML_MODE_COMPACT).toString();
    }


    //copypast from forpda
    public static SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
    public static SimpleDateFormat parseDateTimeFormat = new SimpleDateFormat("dd.MM.yyyy, HH:mm");

    public static String getDay() {
        GregorianCalendar nowCalendar = new GregorianCalendar();
        return dateFormat.format(nowCalendar.getTime());
    }

    public static String getYesterday() {
        GregorianCalendar nowCalendar = new GregorianCalendar();
        nowCalendar.add(Calendar.DAY_OF_MONTH, -1);
        return dateFormat.format(nowCalendar.getTime());
    }

    public static String getForumDateTime(Date date) {

        if (date == null) return "";
        return parseDateTimeFormat.format(date);
    }

    public static String getNewsDateTime(Date date) {

        if (date == null) return "";
        return dateFormat.format(date);
    }

    public static Date parseForumDateTime(String dateTime) {
        try {
            Date res = parseDateTimeFormat.parse(dateTime.replace("Сегодня", getDay()).replace("Вчера", getYesterday()));

            Calendar calendar = new GregorianCalendar();
            calendar.setTime(res);
            int year = calendar.get(Calendar.YEAR);
            if (year < 100)
                calendar.set(Calendar.YEAR, 2000 + year);
            return calendar.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
}
