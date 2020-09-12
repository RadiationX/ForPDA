package forpdateam.ru.forpda.common;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.annotation.NonNull;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;

import static android.content.Context.CLIPBOARD_SERVICE;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

/**
 * Created by isanechek on 30.07.16.
 */

public class Utils {
    public static boolean isMM() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    public static String getFileNameFromUrl(String url){
        String fileName = url;
        try {
            fileName = URLDecoder.decode(url, "CP1251");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        int cut = fileName.lastIndexOf('/');
        if (cut != -1) {
            fileName = fileName.substring(cut + 1);
        }
        return fileName;
    }

    public static void copyToClipBoard(String s) {
        ClipboardManager clipboard = (ClipboardManager) App.getContext().getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("label", s);
        clipboard.setPrimaryClip(clip);
    }

    public static String readFromClipboard() {
        ClipboardManager clipboard = (ClipboardManager) App.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard.hasPrimaryClip()) {
            android.content.ClipDescription description = clipboard.getPrimaryClipDescription();
            android.content.ClipData data = clipboard.getPrimaryClip();
            if (data != null && description != null && description.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN))
                return String.valueOf(data.getItemAt(0).getText());
        }
        return null;
    }

    public static void shareText(String text) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, text);
        sendIntent.setType("text/plain");
        sendIntent.addFlags(FLAG_ACTIVITY_NEW_TASK);
        App.get().startActivity(Intent.createChooser(sendIntent, App.get().getString(R.string.share)).addFlags(FLAG_ACTIVITY_NEW_TASK));
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

    public static void longLog(String msg) {
        int maxLogSize = 1000;
        for (int i = 0; i <= msg.length() / maxLogSize; i++) {
            int start = i * maxLogSize;
            int end = (i + 1) * maxLogSize;
            end = end > msg.length() ? msg.length() : end;
            Log.v("LONG_LOG", msg.substring(start, end));
        }
    }

    public static void log(@NonNull String msg) {
        Log.d("TEST", msg);
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
