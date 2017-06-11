package forpdateam.ru.forpda.utils;

import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import org.acra.ACRA;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.TabManager;
import forpdateam.ru.forpda.client.Client;
import forpdateam.ru.forpda.fragments.TabFragment;
import forpdateam.ru.forpda.fragments.favorites.FavoritesFragment;
import forpdateam.ru.forpda.fragments.forum.ForumFragment;
import forpdateam.ru.forpda.fragments.mentions.MentionsFragment;
import forpdateam.ru.forpda.fragments.profile.ProfileFragment;
import forpdateam.ru.forpda.fragments.qms.chat.QmsChatFragment;
import forpdateam.ru.forpda.fragments.qms.QmsContactsFragment;
import forpdateam.ru.forpda.fragments.qms.QmsThemesFragment;
import forpdateam.ru.forpda.fragments.reputation.ReputationFragment;
import forpdateam.ru.forpda.fragments.search.SearchFragment;
import forpdateam.ru.forpda.fragments.theme.ThemeFragmentWeb;
import forpdateam.ru.forpda.fragments.topics.TopicsFragment;
import forpdateam.ru.forpda.imageviewer.ImageViewerActivity;
import okhttp3.Cookie;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

/**
 * Created by radiationx on 04.08.16.
 */
public class IntentHandler {

    /*
    *http://4pda.ru/forum/index.php?showuser=2556269
    *http://4pda.ru/forum/index.php?showtopic=84979&view=getlastpost
    *http://4pda.ru/forum/index.php?showtopic=84979&view=getnewpost
    *http://4pda.ru/forum/index.php?showtopic=84979&view=findpost&p=51813850
    *http://4pda.ru/forum/index.php?showtopic=84979&st=22460#entry51805351
    *http://4pda.ru/forum/index.php?act=findpost&pid=51805351
    *http://4pda.ru/forum/index.php?showforum=8&utm_source=ftmenu
    *http://4pda.ru/forum/index.php?act=idx
    *http://4pda.ru/forum/index.php?act=fav
    *http://4pda.ru/forum/index.php?act=Members
    *http://4pda.ru/forum/index.php?act=attach&code=showuser
    *http://4pda.ru/forum/index.php?act=UserCP
    *http://4pda.ru/forum/index.php?act=boardrules
    *http://4pda.ru/forum/index.php?act=rep&view=history&mid=3916635
    *http://4pda.ru/forum/index.php?act=qms
    *http://4pda.ru/forum/index.php?act=qms&mid=5106086
    *http://4pda.ru/forum/index.php?act=qms&mid=5106086&t=3127574
    *http://4pda.ru/forum/index.php?act=Help
    *http://4pda.ru/forum/index.php?act=search
    *http://4pda.ru/forum/index.php?s=&act=Stats&view=who&t=84979
    *http://4pda.ru/forum/index.php?act=search&query=hui&username=&forums%5B%5D=all&subforums=1&source=all&sort=rel&result=posts
    *http://4pda.ru/devdb/
    *http://4pda.ru/devdb/phones/
    *http://4pda.ru/devdb/phones/acer
    *http://4pda.ru/devdb/acer_liquid_z410_duo
    *http://4pda.ru/special/polzovatelskoe-testirovanie-alcatel-idol-4s/
    *http://4pda.ru
    *http://4pda.ru/2016/08/04/315172/
    *http://4pda.ru/reviews/tag/smart-watches/
    *http://4pda.ru/articles/
    *http://4pda.ru/pages/posts/3916635
    *http://4pda.ru/pages/comments/3916635/
    *http://4pda.ru/?s=hui
    * */
    private final static String FORUM_PATH = "forum";
    private final static String DEVDB_PATH = "forum";
    private final static String SPECIAL_PATH = "forum";

    public static boolean handle(String url) {
        return handle(url, null);
    }

    public static boolean handle(String url, Bundle args) {
        Log.d("FORPDA_LOG", "handle clear url " + url);
        if (url == null || url.length() <= 1 || url.equals("#")) {
            return false;
        }
        if (url.substring(0, 2).equals("//")) {
            url = "http:".concat(url);
        } else if (url.substring(0, 1).equals("/")) {
            url = "http://4pda.ru".concat(url);
        }
        url = Utils.fromHtml(url);
        Log.d("FORPDA_LOG", "after html url " + url);
        if (url.matches("(?:http?s?:)?\\/\\/[\\s\\S]*?4pda\\.(?:ru|to)[\\s\\S]*")) {
            /*if (!url.contains("4pda.ru")||!url.contains("4pda.to")) {
                url = "http://4pda.ru".concat(url.substring(0, 1).equals("/") ? "" : "/").concat(url);
            }*/
            Uri uri = Uri.parse(url.toLowerCase());
            Log.d("FORPDA_LOG", "HANDLE URL " + uri.toString() + " : " + url);

            /*if (Pattern.compile("https?:\\/\\/4pda\\.ru\\/forum\\/dl\\/post\\/\\d+\\/[\\s\\S]*\\.").matcher(url).find()) {
                Toast.makeText(App.getContext(), "Скачивание файлов и открытие изображений временно не поддерживается", Toast.LENGTH_SHORT).show();
                systemDownload(uri);
            }*/


            Matcher matcher = Pattern.compile("https?:\\/\\/4pda\\.ru\\/forum\\/dl\\/post\\/\\d+\\/([\\s\\S]*\\.([\\s\\S]*))").matcher(url);
            if (matcher.find()) {
                String fullName = matcher.group(1);
                try {
                    fullName = URLDecoder.decode(fullName, "CP1251");
                } catch (Exception ignore) {
                }
                String extension = matcher.group(2);
                boolean isImage = MimeTypeUtil.isImage(extension);
                if (isImage) {
                    ImageViewerActivity.startActivity(App.getContext(), url);
                    return true;
                    //Toast.makeText(App.getContext(), "Скачивание файлов и открытие изображений временно не поддерживается", Toast.LENGTH_SHORT).show();
                } else {
                    systemDownload(fullName, url);
                    return true;
                }
            } else if (Pattern.compile("https?:\\/\\/cs\\d-\\d.4pda.to\\/\\d+").matcher(url).find()) {
                ImageViewerActivity.startActivity(App.getContext(), url);
                return true;
                //Toast.makeText(App.getContext(), "Скачивание файлов и открытие изображений временно не поддерживается", Toast.LENGTH_SHORT).show();
            } else {
                if (args == null) args = new Bundle();
                Log.e("FORPDA_LOG", "HANDLE URL, NOT IMAGE OR FILE");
                switch (uri.getPathSegments().get(0)) {
                    case "forum":
                        return handleForum(uri, args);
                    /*case "devdb":
                        if (uri.getPathSegments().size() > 1) {
                            if (uri.getPathSegments().get(1).matches("phones|pad|ebook|smartwatch")) {
                                if (uri.getPathSegments().size() > 2 && !uri.getPathSegments().get(2).matches("new|select")) {
                                    run("devdb models brand");
                                    return true;
                                }
                                run("devdb models");
                                return true;
                            } else {
                                run("devdb device");
                                return true;
                            }
                        } else {
                            run("devdb categories");
                            return true;
                        }
                    default:
                        return handleSite(uri, args);*/
                }
            }
        }
        Log.e("FORPDA_LOG", "PRE TRY INTENT ");
        try {
            //App.getInstance().startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse(url)).addFlags(FLAG_ACTIVITY_NEW_TASK));

            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(FLAG_ACTIVITY_NEW_TASK);
            App.getInstance().startActivity(Intent.createChooser(intent, "Открыть в").addFlags(FLAG_ACTIVITY_NEW_TASK));
        } catch (ActivityNotFoundException e) {
            ACRA.getErrorReporter().handleException(e);
        }
        return false;
    }

    private static boolean handleForum(Uri uri, Bundle args) {
        String param = uri.getQueryParameter("showuser");
        if (param != null) {
            run("showuser " + param);
            args.putString(TabFragment.ARG_TAB, uri.toString());
            TabManager.getInstance().add(new TabFragment.Builder<>(ProfileFragment.class).setArgs(args).build());
            return true;
        }
        param = uri.getQueryParameter("showtopic");
        if (param != null) {
            String tid = param;
            String view = uri.getQueryParameter("view");
            String st = uri.getQueryParameter("st");
            String pid = uri.getQueryParameter("p");
            if (pid == null) {
                Matcher m = Pattern.compile("#entry(\\d+)").matcher(uri.toString());
                if (m.find())
                    pid = m.group(1);
            }
            run("showtopic " + tid + " : " + view + " : " + st + " : " + pid);
            args.putString(TabFragment.ARG_TAB, uri.toString());
            TabManager.getInstance().add(new TabFragment.Builder<>(ThemeFragmentWeb.class).setArgs(args).build());
            return true;
        }
        param = uri.getQueryParameter("showforum");
        if (param != null || uri.toString().matches("act=idx")) {
            run("showforum " + (param == null ? "-1" : param));
            int id = param == null ? -1 : Integer.parseInt(param);
            if (!ForumFragment.checkIsLink(id)) {
                args.putInt(ForumFragment.ARG_FORUM_ID, id);
                TabManager.getInstance().add(new TabFragment.Builder<>(ForumFragment.class).setArgs(args).build());
            } else {
                args.putInt(TopicsFragment.TOPICS_ID_ARG, id);
                TabManager.getInstance().add(new TabFragment.Builder<>(TopicsFragment.class).setArgs(args).build());
                run("show topics in forum");
            }
            return true;
        }
        param = uri.getQueryParameter("act");
        if (param != null) {
            switch (param) {
                case "qms":
                    if (uri.getQueryParameter("mid") == null) {
                        run("qms contacts");
                        TabManager.getInstance().add(new TabFragment.Builder<>(QmsContactsFragment.class).build());
                    } else {
                        if (uri.getQueryParameter("t") != null) {
                            run("qms chat " + uri.getQueryParameter("mid") + " : " + uri.getQueryParameter("t"));
                            args.putInt(QmsChatFragment.THEME_ID_ARG, Integer.parseInt(uri.getQueryParameter("t")));
                            args.putInt(QmsChatFragment.USER_ID_ARG, Integer.parseInt(uri.getQueryParameter("mid")));
                            TabManager.getInstance().add(new TabFragment.Builder<>(QmsChatFragment.class).setArgs(args).build());
                        } else {
                            run("qms thread " + uri.getQueryParameter("mid"));
                            args.putInt(QmsThemesFragment.USER_ID_ARG, Integer.parseInt(uri.getQueryParameter("mid")));
                            TabManager.getInstance().add(new TabFragment.Builder<>(QmsThemesFragment.class).setArgs(args).build());
                        }
                    }
                    return true;
                case "boardrules":
                    run("boardrules");
                    return false;
                case "search":
                    run("search " + uri.toString());
                    args.putString(TabFragment.ARG_TAB, uri.toString());
                    TabManager.getInstance().add(new TabFragment.Builder<>(SearchFragment.class).setArgs(args).build());
                    return true;
                case "rep":
                    args.putString(TabFragment.ARG_TAB, uri.toString());
                    TabManager.getInstance().add(new TabFragment.Builder<>(ReputationFragment.class).setArgs(args).build());
                    return true;
                case "findpost":
                    args.putString(TabFragment.ARG_TAB, uri.toString());
                    TabManager.getInstance().add(new TabFragment.Builder<>(ThemeFragmentWeb.class).setArgs(args).build());
                    return true;
                case "fav":
                    run("favorites");
                    TabManager.getInstance().add(new TabFragment.Builder<>(FavoritesFragment.class).build());
                    return true;
                case "mentions":
                    run("mentions");
                    TabManager.getInstance().add(new TabFragment.Builder<>(MentionsFragment.class).build());
                    return true;
            }
        }
        return false;
    }

    private static boolean handleSite(Uri uri, Bundle args) {
        if (Pattern.compile("\\d{4}/\\d{2}/\\d{2}/\\d+").matcher(uri.toString()).find()) {
            run("show news");
            return true;
        }
        if (uri.getPathSegments().get(0).contains("special")) {
            run("show special");
            return true;
        }
        if (uri.getPathSegments().size() == 0) {
            run("show newslist");
            return true;
        } else if (uri.getPathSegments().get(0).matches("news|articles|reviews|tag|software|games|review")) {
            run("show newslist category " + uri.getPathSegments().get(0));
            return true;
        }

        return false;
    }

    private static void run(String s) {
        Log.d("FORPDA_LOG", "run: " + s);
        //Toast.makeText(App.getContext(), "ForPDA should run " + s, Toast.LENGTH_SHORT).show();
    }

    public static void systemDownload(String url) {
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
        Log.e("FORPDA_LOG", "SYSTEM DOWNLOAD " + fileName + " : " + url);
        systemDownload(fileName, url);
    }

    public static void systemDownload(String fileName, String url) {
        Runnable runnable = () -> {
            DownloadManager dm = (DownloadManager) App.getContext().getSystemService(Context.DOWNLOAD_SERVICE);
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));

            Map<String, Cookie> cookies = Client.getInstance().getCookies();
            String stringCookies = "";
            for (Map.Entry<String, Cookie> cookieEntry : cookies.entrySet()) {
                stringCookies = stringCookies.concat(cookieEntry.getKey()).concat("=").concat(cookieEntry.getValue().value()).concat(";");
            }
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.addRequestHeader("Cookie", stringCookies);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);

            dm.enqueue(request);
            Toast.makeText(App.getContext(), "Загрузка ".concat(fileName), Toast.LENGTH_SHORT).show();
        };
        TabManager.getInstance().getActive().getMainActivity().checkStoragePermission(runnable);
    }
}
