package forpdateam.ru.forpda.utils;

import android.net.Uri;
import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        Uri uri = Uri.parse(url.toLowerCase());
        if (uri.getHost() != null && uri.getHost().matches("4pda.ru")) {
            switch (uri.getPathSegments().get(0)) {
                case "forum":
                    return handleForum(uri);
                case "devdb":
                    if (uri.getPathSegments().size() > 1) {
                        if (uri.getPathSegments().get(1).matches("phones|pad|ebook|smartwatch")) {
                            if (uri.getPathSegments().size() > 2 && !uri.getPathSegments().get(2).matches("new|select")) {
                                run("devdb models brand");
                                return true;
                            }
                            run("devdb models");
                        } else {
                            run("devdb device");
                        }
                    } else {
                        run("devdb categories");
                    }
                    return true;
                default:
                    handleSite(uri);
                    break;
            }
        }
        return false;
    }

    public static boolean handleForum(Uri uri) {
        String param = uri.getQueryParameter("showuser");
        if (param != null) {
            run("showuser " + param);
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
            return true;
        }
        param = uri.getQueryParameter("showforum");
        if (param != null || uri.toString().matches("act=idx")) {
            run("showforum " + (param == null ? "0" : param));
            return true;
        }
        param = uri.getQueryParameter("act");
        if (param != null) {
            switch (param) {
                case "qms":
                    if (uri.getQueryParameter("mid") == null) {
                        run("qms contacts");
                    } else {
                        if (uri.getQueryParameter("t") != null) {
                            run("qms chat " + uri.getQueryParameter("mid") + " : " + uri.getQueryParameter("t"));
                        } else {
                            run("qms thread " + uri.getQueryParameter("mid"));
                        }
                    }
                    return true;
                case "boardrules":
                    run("boardrules");
                    return true;
                case "search":
                    run("search " + uri.toString());
                    return true;
                case "rep":
                    run("rep " + uri.toString());
                    return true;
                case "findpost":
                    run("redirect " + uri.toString());
                    return true;
                case "fav":
                    run("favorites");
                    return true;
            }
        }
        return false;
    }

    public static boolean handleSite(Uri uri) {
        if (Pattern.compile("\\d{4}/\\d{2}/\\d{2}/\\d").matcher(uri.toString()).find()) {
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
        Log.d("kek", "run: " + s);
    }
}
