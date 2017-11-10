package forpdateam.ru.forpda.utils;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import org.acra.ACRA;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.TabManager;
import forpdateam.ru.forpda.api.NetworkRequest;
import forpdateam.ru.forpda.api.NetworkResponse;
import forpdateam.ru.forpda.client.Client;
import forpdateam.ru.forpda.fragments.TabFragment;
import forpdateam.ru.forpda.fragments.devdb.BrandFragment;
import forpdateam.ru.forpda.fragments.devdb.BrandsFragment;
import forpdateam.ru.forpda.fragments.devdb.DeviceFragment;
import forpdateam.ru.forpda.fragments.favorites.FavoritesFragment;
import forpdateam.ru.forpda.fragments.forum.AnnounceFragment;
import forpdateam.ru.forpda.fragments.forum.ForumRulesFragment;
import forpdateam.ru.forpda.fragments.mentions.MentionsFragment;
import forpdateam.ru.forpda.fragments.news.details.NewsDetailsFragment;
import forpdateam.ru.forpda.fragments.news.main.NewsMainFragment;
import forpdateam.ru.forpda.fragments.profile.ProfileFragment;
import forpdateam.ru.forpda.fragments.qms.QmsContactsFragment;
import forpdateam.ru.forpda.fragments.qms.QmsThemesFragment;
import forpdateam.ru.forpda.fragments.qms.chat.QmsChatFragment;
import forpdateam.ru.forpda.fragments.reputation.ReputationFragment;
import forpdateam.ru.forpda.fragments.search.SearchFragment;
import forpdateam.ru.forpda.fragments.theme.ThemeFragmentWeb;
import forpdateam.ru.forpda.fragments.topics.TopicsFragment;
import forpdateam.ru.forpda.imageviewer.ImageViewerActivity;
import forpdateam.ru.forpda.settings.Preferences;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

/**
 * Created by radiationx on 04.08.16.
 */
public class IntentHandler {
    private final static String LOG_TAG = IntentHandler.class.getSimpleName();

    /*
    *http://4pda.ru/forum/index.php?showuser=2556269
    *https://4pda.ru/forum/index.php?showtopic=84979&view=getlastpost
    *https://4pda.ru/forum/index.php?showtopic=84979&view=getnewpost
    *https://4pda.ru/forum/index.php?showtopic=84979&view=findpost&p=51813850
    *https://4pda.ru/forum/index.php?showtopic=84979&st=22460#entry51805351
    *https://4pda.ru/forum/index.php?act=findpost&pid=51805351
    *https://4pda.ru/forum/index.php?showforum=8&utm_source=ftmenu
    *https://4pda.ru/forum/index.php?act=idx
    *https://4pda.ru/forum/index.php?act=fav
    *https://4pda.ru/forum/index.php?act=Members
    *https://4pda.ru/forum/index.php?act=attach&code=showuser
    *https://4pda.ru/forum/index.php?act=UserCP
    *https://4pda.ru/forum/index.php?act=boardrules
    *https://4pda.ru/forum/index.php?act=rep&view=history&mid=3916635
    *https://4pda.ru/forum/index.php?act=qms
    *https://4pda.ru/forum/index.php?act=qms&mid=5106086
    *https://4pda.ru/forum/index.php?act=qms&mid=5106086&t=3127574
    *https://4pda.ru/forum/index.php?act=Help
    *https://4pda.ru/forum/index.php?act=search
    *https://4pda.ru/forum/index.php?s=&act=Stats&view=who&t=84979
    *https://4pda.ru/forum/index.php?act=search&query=hui&username=&forums%5B%5D=all&subforums=1&source=all&sort=rel&result=posts
    *https://4pda.ru/devdb/
    *https://4pda.ru/devdb/phones/
    *https://4pda.ru/devdb/phones/acer
    *https://4pda.ru/devdb/acer_liquid_z410_duo
    *https://4pda.ru/special/polzovatelskoe-testirovanie-alcatel-idol-4s/
    *https://4pda.ru
    *https://4pda.ru/2016/08/04/315172/
    *https://4pda.ru/reviews/tag/smart-watches/
    *https://4pda.ru/articles/
    *https://4pda.ru/pages/posts/3916635
    *https://4pda.ru/pages/comments/3916635/
    *https://4pda.ru/?s=hui
    * */
    private final static String FORUM_PATH = "forum";
    private final static String DEVDB_PATH = "forum";
    private final static String SPECIAL_PATH = "forum";

    public static boolean handle(String url) {
        return handle(url, null);
    }

    public static boolean handle(String url, Bundle args) {
        Log.d(LOG_TAG, "handle url " + url);
        if (url == null || url.length() <= 1 || url.equals("#")) {
            return false;
        }
        if (url.substring(0, 2).equals("//")) {
            url = "https:".concat(url);
        } else if (url.substring(0, 1).equals("/")) {
            url = "https://4pda.ru".concat(url);
        }
        url = url.replace("&amp;", "&").replace("\"", "").trim();
        Log.d(LOG_TAG, "Corrected url " + url);


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
            } else {
                handleDownload(fullName, url);
            }
            return true;
        } else if (Pattern.compile("\\/\\/.*?(4pda\\.to|4pda\\.ru|ggpht\\.com|googleusercontent\\.com|windowsphone\\.com|mzstatic\\.com|savepic\\.net|savepice\\.ru|savepic\\.ru|.*?\\.ibb\\.com?)\\/[\\s\\S]*?\\.(png|jpg|jpeg|gif)").matcher(url).find()) {
            ImageViewerActivity.startActivity(App.getContext(), url);
            return true;
            //Toast.makeText(App.getContext(), "Скачивание файлов и открытие изображений временно не поддерживается", Toast.LENGTH_SHORT).show();
        }

        matcher = Pattern.compile("(?:http?s?:)?\\/\\/[\\s\\S]*?4pda\\.(?:ru|to)\\/forum\\/lofiversion\\/[^\\?]*?\\?(t|f)(\\d+)(?:-(\\d+))?").matcher(url);
        if (matcher.find()) {
            url = "https://4pda.ru/forum/index.php?";
            switch (matcher.group(1)) {
                case "t":
                    url += "showtopic=";
                    break;
                case "f":
                    url += "showforum=";
                    break;
            }
            url += matcher.group(2);

            if (matcher.group(3) != null) {
                url += "&st=" + matcher.group(3);
            }
        }


        if (url.matches("(?:http?s?:)?\\/\\/[\\s\\S]*?4pda\\.(?:ru|to)[\\s\\S]*")) {
            /*if (!url.contains("4pda.ru")||!url.contains("4pda.to")) {
                url = "https://4pda.ru".concat(url.substring(0, 1).equals("/") ? "" : "/").concat(url);
            }*/
            Uri uri = Uri.parse(url.toLowerCase());
            Log.d(LOG_TAG, "Compare uri/url " + uri.toString() + " : " + url);

            /*if (Pattern.compile("https?:\\/\\/4pda\\.ru\\/forum\\/dl\\/post\\/\\d+\\/[\\s\\S]*\\.").matcher(url).find()) {
                Toast.makeText(App.getContext(), "Скачивание файлов и открытие изображений временно не поддерживается", Toast.LENGTH_SHORT).show();
                handleDownload(uri);
            }*/

            if (args == null) args = new Bundle();
            Log.d(LOG_TAG, "Url is not a image or file");
            for (String path : uri.getPathSegments()) {
                Log.d(LOG_TAG, "Uri path: " + path);
            }

            if (!uri.getPathSegments().isEmpty()) {
                switch (uri.getPathSegments().get(0)) {
                    case "pages":
                        if (uri.getPathSegments().size() > 1) {
                            if (uri.getPathSegments().get(1).equalsIgnoreCase("go")) {
                                String redUrl = uri.getQueryParameter("u");
                                if (redUrl != null) {
                                    try {
                                        redUrl = URLDecoder.decode(redUrl, "UTF-8");
                                    } catch (UnsupportedEncodingException ex) {
                                        ex.printStackTrace();
                                    }
                                    externalIntent(redUrl);
                                    return true;
                                }
                            }
                        }
                        break;
                    case "forum":
                        return handleForum(uri, args);
                    case "devdb":
                        if (uri.getPathSegments().size() > 1) {
                            if (uri.getPathSegments().get(1).matches("phones|pad|ebook|smartwatch")) {
                                if (uri.getPathSegments().size() > 2 && !uri.getPathSegments().get(2).matches("new|select")) {
                                    run("devdb models brand");
                                    args.putString(BrandFragment.ARG_CATEGORY_ID, uri.getPathSegments().get(1));
                                    args.putString(BrandFragment.ARG_BRAND_ID, uri.getPathSegments().get(2));
                                    TabManager.get().add(BrandFragment.class, args);
                                    return true;
                                }
                                run("devdb models");
                                args.putString(BrandsFragment.ARG_CATEGORY_ID, uri.getPathSegments().get(1));
                                TabManager.get().add(BrandsFragment.class, args);
                                return true;
                            } else {
                                run("devdb device");
                                args.putString(DeviceFragment.ARG_DEVICE_ID, uri.getPathSegments().get(1));
                                TabManager.get().add(DeviceFragment.class, args);
                                return true;
                            }
                        } else {
                            run("devdb categories");
                            TabManager.get().add(BrandsFragment.class);
                            return true;
                        }
                    default:
                        if (handleSite(uri, args))
                            return true;
                }
            } else {
                if (handleSite(uri, args))
                    return true;
            }

        }

        externalIntent(url);
        return false;
    }

    public static void externalIntent(String url) {
        Log.d(LOG_TAG, "Start external intent");
        try {
            //App.get().startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse(url)).addFlags(FLAG_ACTIVITY_NEW_TASK));

            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(FLAG_ACTIVITY_NEW_TASK);
            App.get().startActivity(Intent.createChooser(intent, App.get().getString(R.string.open_with)).addFlags(FLAG_ACTIVITY_NEW_TASK));
        } catch (ActivityNotFoundException e) {
            ACRA.getErrorReporter().handleException(e);
        }
    }

    private static boolean handleForum(Uri uri, Bundle args) {
        String param = uri.getQueryParameter("showuser");
        if (param != null) {
            run("showuser " + param);
            args.putString(TabFragment.ARG_TAB, uri.toString());
            TabManager.get().add(ProfileFragment.class, args);
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
            TabManager.get().add(ThemeFragmentWeb.class, args);
            return true;
        }
        param = uri.getQueryParameter("showforum");
        if (param != null || uri.toString().matches("act=idx")) {
            run("showforum " + (param == null ? "-1" : param));
            int id = param == null ? -1 : Integer.parseInt(param);
            /*if (!ForumFragment.checkIsLink(id)) {
                args.putInt(ForumFragment.ARG_FORUM_ID, id);
                TabManager.get().add(new TabFragment.Builder<>(ForumFragment.class).setArgs(args).build());
            } else {

            }*/
            args.putInt(TopicsFragment.TOPICS_ID_ARG, id);
            TabManager.get().add(TopicsFragment.class, args);
            run("show topics in forum");
            return true;
        }
        param = uri.getQueryParameter("act");
        if (param != null) {
            switch (param) {
                case "qms":
                    if (uri.getQueryParameter("mid") == null) {
                        run("qms contacts");
                        TabManager.get().add(QmsContactsFragment.class, args);
                    } else {
                        if (uri.getQueryParameter("t") != null) {
                            run("qms chat " + uri.getQueryParameter("mid") + " : " + uri.getQueryParameter("t"));
                            args.putInt(QmsChatFragment.THEME_ID_ARG, Integer.parseInt(uri.getQueryParameter("t")));
                            args.putInt(QmsChatFragment.USER_ID_ARG, Integer.parseInt(uri.getQueryParameter("mid")));
                            TabManager.get().add(QmsChatFragment.class, args);
                        } else {
                            run("qms thread " + uri.getQueryParameter("mid"));
                            args.putInt(QmsThemesFragment.USER_ID_ARG, Integer.parseInt(uri.getQueryParameter("mid")));
                            TabManager.get().add(QmsThemesFragment.class, args);
                        }
                    }
                    return true;
                case "boardrules":
                    run("boardrules");
                    TabManager.get().add(ForumRulesFragment.class);
                    return true;
                case "announce":
                    run("announce");
                    String id = uri.getQueryParameter("st");
                    if (id != null) {
                        args.putInt(AnnounceFragment.ARG_ANNOUNCE_ID, Integer.parseInt(id));
                    }
                    String forumId = uri.getQueryParameter("f");
                    if (forumId != null) {
                        args.putInt(AnnounceFragment.ARG_FORUM_ID, Integer.parseInt(forumId));
                    }
                    TabManager.get().add(AnnounceFragment.class, args);
                    return true;
                case "search":
                    run("search " + uri.toString());
                    args.putString(TabFragment.ARG_TAB, uri.toString());
                    TabManager.get().add(SearchFragment.class, args);
                    return true;
                case "rep":
                    args.putString(TabFragment.ARG_TAB, uri.toString());
                    TabManager.get().add(ReputationFragment.class, args);
                    return true;
                case "findpost":
                    args.putString(TabFragment.ARG_TAB, uri.toString());
                    TabManager.get().add(ThemeFragmentWeb.class, args);
                    return true;
                case "fav":
                    run("favorites");
                    TabManager.get().add(FavoritesFragment.class);
                    return true;
                case "mentions":
                    run("mentions");
                    TabManager.get().add(MentionsFragment.class);
                    return true;
            }
        }
        return false;
    }

    private static boolean handleSite(Uri uri, Bundle args) {
        Matcher matcher = Pattern.compile("https?:\\/\\/4pda\\.ru\\/(?:.+?p=|\\d+\\/\\d+\\/\\d+\\/|[\\w\\/]*?\\/?(newer|older)\\/)(\\d+)(?:\\/#comment(\\d+))?").matcher(uri.toString());
        if (matcher.find()) {
            if (matcher.group(2) != null) {
                args.putInt(NewsDetailsFragment.ARG_NEWS_ID, Integer.parseInt(matcher.group(2)));
            }
            if (matcher.group(3) != null) {
                args.putInt(NewsDetailsFragment.ARG_NEWS_COMMENT_ID, Integer.parseInt(matcher.group(3)));
            }
            args.putString(NewsDetailsFragment.ARG_NEWS_URL, uri.toString());

            TabManager.get().add(NewsDetailsFragment.class, args);
            return true;
        }
        if (!uri.getPathSegments().isEmpty() && uri.getPathSegments().get(0).contains("special")) {
            run("show special");
            Toast.makeText(App.getContext(), "Не поддерживается: special", Toast.LENGTH_SHORT).show();
            return true;
        }
        if (uri.getPathSegments().isEmpty()) {
            TabManager.get().add(NewsMainFragment.class);
            run("show newslist");
            return true;
        } else if (uri.getPathSegments().get(0).matches("news|articles|reviews|tag|software|games|review")) {
            run("show newslist category " + uri.getPathSegments().get(0));
            return true;
        }

        return false;
    }

    private static void run(String s) {
        Log.d(LOG_TAG, "Run in theory: " + s);
    }

    public static void handleDownload(String url) {
        Log.d(LOG_TAG, "handleDownload " + url);
        String fileName = Utils.getFileNameFromUrl(url);
        handleDownload(fileName, url);
    }

    public static void handleDownload(String fileName, String url) {
        Log.d(LOG_TAG, "handleDownload " + fileName + " : " + url);
        Activity activity = App.getActivity();
        if (activity != null) {

            new AlertDialog.Builder(activity)
                    .setMessage(String.format(activity.getString(R.string.load_file), fileName))
                    .setPositiveButton(activity.getString(R.string.ok), (dialog, which) -> {
                        redirectDownload(fileName, url);
                    })
                    .setNegativeButton(activity.getString(R.string.cancel), null)
                    .show();
        } else {
            redirectDownload(fileName, url);
        }
    }

    private static void redirectDownload(String fileName, String url) {
        Toast.makeText(App.getContext(), String.format(App.get().getString(R.string.perform_request_link), fileName), Toast.LENGTH_SHORT).show();
        Observable.fromCallable(() -> Client.get().request(new NetworkRequest.Builder().url(url).withoutBody().build()))
                .onErrorReturn(throwable -> new NetworkResponse(null))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(response -> {
                    if (response.getUrl() == null) {
                        Toast.makeText(App.getContext(), R.string.error_occurred, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Activity activity = App.getActivity();
                    if (!Preferences.Main.isSystemDownloader(null) || activity == null) {
                        externalDownloader(response.getRedirect());
                    } else {
                        Runnable checkAction = () -> {
                            Toast.makeText(App.getContext(), String.format(App.get().getString(R.string.perform_request_link), fileName), Toast.LENGTH_SHORT).show();
                            try {
                                systemDownloader(fileName, response.getRedirect());
                            } catch (Exception exception) {
                                Toast.makeText(App.getContext(), R.string.perform_loading_error, Toast.LENGTH_SHORT).show();
                                externalDownloader(response.getRedirect());
                            }
                        };
                        App.get().checkStoragePermission(checkAction, activity);
                    }
                });
    }

    private static void systemDownloader(String fileName, String url) {
        DownloadManager dm = (DownloadManager) App.getContext().getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
        request.setMimeType(MimeTypeUtil.getType(fileName));

        dm.enqueue(request);
    }

    public static void externalDownloader(String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(FLAG_ACTIVITY_NEW_TASK);
            App.get().startActivity(Intent.createChooser(intent, App.get().getString(R.string.load_with)).addFlags(FLAG_ACTIVITY_NEW_TASK));
        } catch (ActivityNotFoundException e) {
            ACRA.getErrorReporter().handleException(e);
        }
    }
}
