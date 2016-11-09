package forpdateam.ru.forpda.api.theme;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import biz.source_code.miniTemplator.MiniTemplator;
import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.api.theme.models.ThemePage;
import forpdateam.ru.forpda.api.theme.models.ThemePost;
import forpdateam.ru.forpda.client.Client;
import forpdateam.ru.forpda.utils.ourparser.Html;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

/**
 * Created by radiationx on 04.08.16.
 */
public class Theme {
    //y: Oh God... Why?
    //g: Because it is faster
    private final static Pattern postsPattern = Pattern.compile("<a name=\"entry([^\"]*?)\"[^>]*?><\\/a><div class=\"post_header_container\"><div class=\"post_header\"><span class=\"post_date\">([^&]*?)&[^<]*?<a[^>]*?>#(\\d+)<\\/a>\\|<\\/span>[\\s\\S]*?<span[^>]*?><a[^>]*?data-av=\"([^\"]*?)\">([^<]*?)<\\/a><\\/span><br[^>]*?>[\\s\\S]*?<span[^>]*?>(?:<[^>]*?>([^<]*?|)<\\/[^>]*?><br[^>]*?>|)[^<]*?<span[^>]*?color:([^;']*?)'>([^<]*?)<\\/span>[\\s\\S]*?<br[^>]*?><font color=\"([^\"]*?)\">[^<]*?<\\/font>[\\s\\S]*?<a[^>]*?showuser=([^\"]*?)\">[^<]*?<\\/a>[\\s\\S]*?ajaxrep[^>]*?>([^<]*?)<\\/span><\\/a>\\) [\\s\\S]*?(<a[^>]*?win_minus[^>]*?><img[^>]*?><\\/a>|)[^<]*(<a[^>]*?win_add[^>]*?><img[^>]*?><\\/a>|)<br[^>]*?>[^<]*?<span class=\"post_action\">(<a[^>]*?report[^>]*?>[^<]*?<\\/a>|)[^<]*(<a[^>]*?edit_post[^>]*?>[^<]*?<\\/a>|)[^<]*(<a[^>]*?delete[^>]*?>[^<]*?<\\/a>|)[^<]*(<a[^>]*?CODE=02[^>]*?>[^<]*?<\\/a>|)[^<]*[^<]*[\\s\\S]*?(<div class=\"post_body[^>]*?>[\\s\\S]*?<\\/div>)<\\/div>(?:<div data-post=|<!-- TABLE FOOTER -->)");
    private final static Pattern countsPattern = Pattern.compile("parseInt\\((\\d*)\\)[\\s\\S]*?parseInt\\(st\\*(\\d*)\\)");
    private final static Pattern titlePattern = Pattern.compile("<div class=\"topic_title_post\">([^,<]*)(?:, ([^<]*)|)<br");
    private final static Pattern alreadyInFavPattern = Pattern.compile("Тема уже добавлена в <a href=\"[^\"]*act=fav\">");
    private final static Pattern paginationPattern = Pattern.compile("pagination\">([\\s\\S]*?<span[^>]*?>([^<]*?)</span>[\\s\\S]*?)</div><br");
    private final static Pattern themeIdPattern = Pattern.compile("showtopic=([\\d][^&]*)");
    public final static Pattern elemToScrollPattern = Pattern.compile("(?:anchor=|#)([^&\\n\\=\\?\\.\\#]*)");
    //private final static Pattern newsPattern = Pattern.compile("<section[^>]*?><article[^>]*?>[^<]*?<div class=\"container\"[\\s\\S]*?<img[^>]*?src=\"([^\"]*?)\" alt=\"([\\s\\S]*?)\"[\\s\\S]*?<em[^>]*>([^<]*?)</em>[\\s\\S]*?<a href=\"([^\"]*?)\">([\\s\\S]*?)</a>[\\s\\S]*?<a[^>]*?>([^<]*?)</a><div[^>]*?># ([\\s\\S]*?)</div>[\\s\\S]*?<div class=\"content-box\"[^>]*?>([\\s\\S]*?)</div></div></div>[^<]*?<div class=\"materials-box\">[\\s\\S]*?(<ul[\\s\\S]*?/ul>)[\\s\\S]*?<div class=\"comment-box\" id=\"comments\">[\\s\\S]*?(<ul[\\s\\S]*?/ul>)[^<]*?<form");


    public Observable<ThemePage> getPage(final String url) {
        return getPage(url, false);
    }

    public Observable<ThemePage> getPage(final String url, boolean generateHtml) {
        return Observable.fromCallable(() -> _getPage(url, generateHtml));
    }

    public ThemePage _getPage(final String url, boolean generateHtml) throws Exception {
        ThemePage page = new ThemePage();
        Log.d("kek", "page start _getPage");
        String response = Client.getInstance().get(url);
        String redirectUrl = Client.getInstance().getRedirect(url);
        if (redirectUrl == null)
            redirectUrl = url;
        page.setUrl(redirectUrl);

        Log.d("kek", "page getted");
        long time = System.currentTimeMillis();
        Matcher matcher = elemToScrollPattern.matcher(redirectUrl);
        while (matcher.find()) {
            page.setElementToScroll(matcher.group(1));
        }
        matcher = themeIdPattern.matcher(redirectUrl);
        if (matcher.find()) {
            page.setId(Integer.parseInt(matcher.group(1)));
        }
        matcher = countsPattern.matcher(response);
        if (matcher.find()) {
            page.setAllPagesCount(Integer.parseInt(matcher.group(1)) + 1);
            page.setPostsOnPageCount(Integer.parseInt(matcher.group(2)));
        }
        Log.d("kek", "check 1");
        matcher = paginationPattern.matcher(response);
        if (matcher.find()) {
            /*page.setIsFirstPage(!matcher.group(1).matches("<a[^>]*?>&lt;</a>"));
            page.setIsLastPage(!matcher.group(1).matches("<a[^>]*?>&gt;</a>"));*/
            page.setCurrentPage(Integer.parseInt(matcher.group(2)));
        }
        Log.d("kek", "check 2");
        matcher = titlePattern.matcher(response);
        if (matcher.find()) {
            page.setTitle(matcher.group(1));
            page.setDesc(matcher.group(2));
        }
        Log.d("kek", "check 3");
        matcher = alreadyInFavPattern.matcher(response);
        page.setInFavorite(matcher.find());
        matcher = postsPattern.matcher(response);
        Log.d("kek", "check 4");
        int memberId = Api.Auth().getUserIdInt();
        while (matcher.find()) {
            ThemePost post = new ThemePost();
            post.setId(Integer.parseInt(matcher.group(1)));
            post.setDate(matcher.group(2));
            post.setNumber(Integer.parseInt(matcher.group(3)));
            post.setAvatar(matcher.group(4));
            post.setNick(Html.fromHtml(matcher.group(5)).toString());
            post.setCurator(matcher.group(6) != null);
            post.setGroupColor(matcher.group(7));
            post.setGroup(matcher.group(8));
            post.setOnline(matcher.group(9).contains("green"));
            post.setUserId(Integer.parseInt(matcher.group(10)));
            post.setReputation(matcher.group(11));
            post.setCanMinus(!matcher.group(12).isEmpty());
            post.setCanPlus(!matcher.group(13).isEmpty());
            post.setCanReport(!matcher.group(14).isEmpty());
            post.setCanEdit(!matcher.group(15).isEmpty());
            post.setCanDelete(!matcher.group(16).isEmpty());
            page.setCanQuote(!matcher.group(17).isEmpty());
            post.setBody(matcher.group(18));
            if (post.isCurator() && post.getUserId() == memberId)
                page.setCurator(true);
            page.addPost(post);
        }
        Log.d("kek", "end created page obj " + (System.currentTimeMillis() - time));
        if (generateHtml) {
            long time2 = System.currentTimeMillis();
            MiniTemplator t = App.getInstance().getTemplator();
            boolean authorized = Api.Auth().getState();
            boolean prevDisabled = page.getCurrentPage() <= 1;
            boolean nextDisabled = page.getCurrentPage() == page.getAllPagesCount();

            if (t.variableExists("topic_title"))
                t.setVariable("topic_title", page.getTitle());

            if (t.variableExists("topic_url"))
                t.setVariable("topic_url", redirectUrl);

            if (t.variableExists("topic_description"))
                t.setVariable("topic_description", page.getDesc());

            if (t.variableExists("in_favorite"))
                t.setVariable("in_favorite", Boolean.toString(page.isInFavorite()));

            if (t.variableExists("all_pages"))
                t.setVariable("all_pages", page.getAllPagesCount());

            if (t.variableExists("posts_on_page"))
                t.setVariable("posts_on_page", page.getPostsOnPageCount());

            if (t.variableExists("current_page"))
                t.setVariable("current_page", page.getCurrentPage());

            if (t.variableExists("authorized"))
                t.setVariable("authorized", Boolean.toString(authorized));

            if (t.variableExists("is_curator"))
                t.setVariable("is_curator", Boolean.toString(page.isCurator()));

            if (t.variableExists("member_id"))
                t.setVariable("member_id", Api.Auth().getUserIdInt());

            if (t.variableExists("elem_to_scroll"))
                t.setVariable("elem_to_scroll", page.getElementToScroll());

            if (t.variableExists("body_type"))
                t.setVariable("body_type", "topic");

            if (t.variableExists("navigation_disable"))
                t.setVariable("navigation_disable", prevDisabled && nextDisabled ? "navigation_disable" : "");

            if (t.variableExists("first_disable"))
                t.setVariable("first_disable", getDisableStr(prevDisabled));

            if (t.variableExists("prev_disable"))
                t.setVariable("prev_disable", getDisableStr(prevDisabled));

            if (t.variableExists("next_disable"))
                t.setVariable("next_disable", getDisableStr(nextDisabled));

            if (t.variableExists("last_disable"))
                t.setVariable("last_disable", getDisableStr(nextDisabled));

            if (t.variableExists("disable_avatar_js"))
                t.setVariable("disable_avatar_js", Boolean.toString(true));

            if (t.variableExists("disable_avatar"))
                t.setVariable("disable_avatar", true ? "" : "disable_avatar");

            if (t.variableExists("avatar_type"))
                t.setVariable("avatar_type", true ? "" : "avatar_circle");

            Log.d("kek", "template check 1 " + (System.currentTimeMillis() - time2));

            int hatPostId = page.getPosts().get(0).getId();
            boolean existOnline = t.variableExists("user_online");
            boolean existPostId = t.variableExists("post_id");
            boolean existUserId = t.variableExists("user_id");
            boolean existAvatar = t.variableExists("avatar");
            boolean existNick = t.variableExists("nick");
            boolean existCurator = t.variableExists("curator");
            boolean existGroupColor = t.variableExists("group_color");
            boolean existGroup = t.variableExists("group");
            boolean existReputation = t.variableExists("reputation");
            boolean existDate = t.variableExists("date");
            boolean existNumber = t.variableExists("number");
            boolean existBody = t.variableExists("body");

            boolean existReportBlock = t.blockExists("report_block");
            boolean existReplyBlock = t.blockExists("reply_block");
            boolean existVoteBlock = t.blockExists("vote_block");
            boolean existDeleteBlock = t.blockExists("delete_block");
            boolean existEditBlock = t.blockExists("edit_block");
            Log.d("kek", "template check 2 " + (System.currentTimeMillis() - time2));
            for (ThemePost post : page.getPosts()) {
                if (existOnline)
                    t.setVariable("user_online", post.isOnline() ? "online" : "");
                if (existPostId)
                    t.setVariable("post_id", post.getId());
                if (existUserId)
                    t.setVariable("user_id", post.getUserId());

                //Post header
                if (existAvatar)
                    t.setVariable("avatar", "http://s.4pda.to/forum/uploads/".concat(post.getAvatar()));
                if (existNick)
                    t.setVariable("nick", post.getNick());
                if (existCurator)
                    t.setVariable("curator", post.isCurator() ? "curator" : "");
                if (existGroupColor)
                    t.setVariable("group_color", post.getGroupColor());
                if (existGroup)
                    t.setVariable("group", post.getGroup());
                if (existReputation)
                    t.setVariable("reputation", post.getReputation());
                if (existDate)
                    t.setVariable("date", post.getDate());
                if (existNumber)
                    t.setVariable("number", post.getNumber());

                //Post body
                if (hatPostId == post.getId() && page.getPosts().size() > 1)
                    if (t.blockExists("hat_button"))
                        t.addBlock("hat_button");
                if (existBody)
                    t.setVariable("body", post.getBody());

                //Post footer
                if (existReportBlock && post.canReport() && authorized)
                    t.addBlock("report_block");
                if (existReplyBlock && page.canQuote() && authorized)
                    t.addBlock("reply_block");
                if (existVoteBlock && authorized && post.getUserId() != memberId)
                    t.addBlock("vote_block");
                if (existDeleteBlock && post.canDelete() && authorized)
                    t.addBlock("delete_block");
                if (existEditBlock && post.canDelete() && authorized)
                    t.addBlock("edit_block");

                t.addBlock("post");
            }
            Log.d("kek", "template check 3 " + (System.currentTimeMillis() - time2));
            page.setHtml(t.generateOutput());
            Log.d("kek", "template check 4 " + (System.currentTimeMillis() - time2));
            t.reset();
            Log.d("kek", "template check 5 " + (System.currentTimeMillis() - time2));
        }

        Log.d("kek", "theme parsing time " + (System.currentTimeMillis() - time));
        return page;
    }

    private String getDisableStr(boolean b) {
        return b ? "disabled" : "";
    }


    public Observable<String> reportPost(int themeId, int postId, String message) {
        return Observable.fromCallable(() -> _reportPost(themeId, postId, message));
    }

    private String _reportPost(int topicId, int postId, String message) throws Exception {
        Map<String, String> headers = new HashMap<>();
        headers.put("act", "report");
        headers.put("send", "1");
        headers.put("t", Integer.toString(topicId));
        headers.put("p", Integer.toString(postId));
        headers.put("message", message);

        String response = Client.getInstance().post("http://4pda.ru/forum/index.php?act=report&amp;send=1&amp;t=" + topicId + "&amp;p=" + postId, headers);

        Pattern p = Pattern.compile("<div class=\"errorwrap\">\n" +
                "\\s*<h4>Причина:</h4>\n" +
                "\\s*\n" +
                "\\s*<p>(.*)</p>", Pattern.MULTILINE);
        Matcher m = p.matcher(response);
        return m.find() ? "Ошибка отправки жалобы: ".concat(m.group(1)) : "Жалоба отправлена";
    }


    public Observable<String> deletePost(int postId) {
        return Observable.fromCallable(() -> _deletePost(postId));
    }

    private String _deletePost(int postId) throws Exception {
        String url = "http://4pda.ru/forum/index.php?act=zmod&auth_key=".concat(App.getInstance().getPreferences().getString("auth_key", null)).concat("&code=postchoice&tact=delete&selectedpids=").concat(Integer.toString(postId));
        String response = Client.getInstance().get(url);
        return response.equals("ok") ? "" : null;
    }


    public Observable<String> votePost(int postId, boolean type) {
        return Observable.fromCallable(() -> _votePost(postId, type));
    }

    private String _votePost(int postId, boolean type) throws Exception {
        String response = Client.getInstance().get("http://4pda.ru/forum/zka.php?i=".concat(Integer.toString(postId)).concat("&v=").concat(type ? "1" : "-1"));
        String result = null;

        Matcher m = Pattern.compile("ok:\\s*?((?:\\+|\\-)?\\d+)").matcher(response);
        if (m.find()) {
            int code = Integer.parseInt(m.group(1));
            switch (code) {
                case 0:
                    result = "Ошибка: Вы уже голосовали за это сообщение";
                    break;
                case 1:
                    result = "Репутация поста повышена";
                    break;
                case -1:
                    result = "Репутация поста понижена";
                    break;
            }
        }
        if (result == null) result = "Ошибка изменения репутации поста";
        return result;
    }

    public Observable<String> changeReputation(int postId, int userId, boolean type, String message) {
        return Observable.fromCallable(() -> _changeReputation(postId, userId, type, message));
    }

    private String _changeReputation(int postId, int userId, boolean type, String message) throws Exception {
        Map<String, String> headers = new HashMap<>();
        headers.put("act", "rep");
        headers.put("p", Integer.toString(postId));
        headers.put("mid", Integer.toString(userId));
        headers.put("type", type ? "add" : "minus");
        headers.put("message", message);

        String response = Client.getInstance().post("http://4pda.ru/forum/index.php", headers);

        Pattern p = Pattern.compile("<title>(.*?)(?: - 4PDA|)</title>[\\s\\S]*?wr va-m text\">([\\s\\S]*?)</div></div></div></div><div class=\"footer\">");
        Matcher m = p.matcher(response);
        String result = null;
        if (m.find()) {
            if (m.group(1).contains("Ошибка"))
                result = Html.fromHtml(m.group(2)).toString();
        }
        return result;

    }


}