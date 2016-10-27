package forpdateam.ru.forpda.api.theme;

import android.util.Log;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import biz.source_code.miniTemplator.MiniTemplator;
import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.api.theme.models.ThemePage;
import forpdateam.ru.forpda.api.theme.models.ThemePost;
import forpdateam.ru.forpda.client.Client;
import io.reactivex.Observable;

/**
 * Created by radiationx on 04.08.16.
 */
public class Theme {
    //y: Oh God... Why?
    //g: Because it is faster
    private final static Pattern postsPattern = Pattern.compile("<a name=\"entry([^\"]*?)\"[^>]*?></a><div class=\"post_header_container\"><div class=\"post_header\"><span class=\"post_date\">([^&]*?)&[^<]*?<a[^>]*?>#(\\d+)</a>\\|</span>[\\s\\S]*?<span[^>]*?><a[^>]*?data-av=\"([^\"]*?)\">([^<]*?)</a></span><br[^>]*?>[\\s\\S]*?<span[^>]*?>(<[^>]*?>([^<]*?)</[^>]*?><br[^>]*?>|)[^<]*?<span[^>]*?color:([^;']*?)'>([^<]*?)</span><br[^>]*?><font color=\"([^\"]*?)\">[^<]*?</font>[\\s\\S]*?<a[^>]*?showuser=([^\"]*?)\">[^<]*?</a>[\\s\\S]*?ajaxrep[^>]*?>([^<]*?)</span></a>\\) [\\s\\S]*?(<a[^>]*?win_minus[^>]*?><img[^>]*?></a>|)[^<]*(<a[^>]*?win_add[^>]*?><img[^>]*?></a>|)<br[^>]*?>[^<]*?<span class=\"post_action\">(<a[^>]*?report[^>]*?>[^<]*?</a>|)[^<]*(<a[^>]*?edit_post[^>]*?>[^<]*?</a>|)[^<]*(<a[^>]*?delete[^>]*?>[^<]*?</a>|)[^<]*(<a[^>]*?CODE=02[^>]*?>[^<]*?</a>|)[^<]*[^<]*[\\s\\S]*?(<div class=\"post_body[^>]*?>[\\s\\S]*?</div>)</div>(<div data-post=|<!-- TABLE FOOTER -->)");
    private final static Pattern countsPattern = Pattern.compile("parseInt\\((\\d*)\\)[\\s\\S]*?parseInt\\(st\\*(\\d*)\\)");
    private final static Pattern titlePattern = Pattern.compile("<div class=\"topic_title_post\">([^,<]*)(, ([^<]*)|)<");
    private final static Pattern alreadyInFavPattern = Pattern.compile("Тема уже добавлена в <a href=\"[^\"]*act=fav\">");
    private final static Pattern paginationPattern = Pattern.compile("pagination\">([\\s\\S]*?<span[^>]*?>([^<]*?)</span>[\\s\\S]*?)</div><br");


    private final static Pattern newsPattern = Pattern.compile("<section[^>]*?><article[^>]*?>[^<]*?<div class=\"container\"[\\s\\S]*?<img[^>]*?src=\"([^\"]*?)\" alt=\"([\\s\\S]*?)\"[\\s\\S]*?<em[^>]*>([^<]*?)</em>[\\s\\S]*?<a href=\"([^\"]*?)\">([\\s\\S]*?)</a>[\\s\\S]*?<a[^>]*?>([^<]*?)</a><div[^>]*?># ([\\s\\S]*?)</div>[\\s\\S]*?<div class=\"content-box\"[^>]*?>([\\s\\S]*?)</div></div></div>[^<]*?<div class=\"materials-box\">[\\s\\S]*?(<ul[\\s\\S]*?/ul>)[\\s\\S]*?<div class=\"comment-box\" id=\"comments\">[\\s\\S]*?(<ul[\\s\\S]*?/ul>)[^<]*?<form");

    private ThemePage _getPage(final String url, boolean generateHtml) throws Exception {
        ThemePage page = new ThemePage();
        Log.d("kek", "page start _getPage");
        String response = Client.getInstance().get(url);
        Log.d("kek", "page getted");
        Date date = new Date();
        Matcher matcher = countsPattern.matcher(response);
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
            page.setDesc(matcher.group(3));
        }
        Log.d("kek", "check 3");
        matcher = alreadyInFavPattern.matcher(response);
        page.setInFavorite(matcher.find());
        matcher = postsPattern.matcher(response);
        Log.d("kek", "check 4");
        while (matcher.find()) {
            ThemePost post = new ThemePost();
            post.setId(Integer.parseInt(matcher.group(1)));
            post.setDate(matcher.group(2));
            post.setNumber(Integer.parseInt(matcher.group(3)));
            post.setAvatar(matcher.group(4));
            post.setNick(matcher.group(5));
            post.setCurator(!matcher.group(6).isEmpty());
            post.setGroupColor(matcher.group(8));
            post.setGroup(matcher.group(9));
            post.setOnline(matcher.group(10).contains("green"));
            post.setUserId(Integer.parseInt(matcher.group(11)));
            post.setReputation(matcher.group(12));
            post.setCanMinus(!matcher.group(13).isEmpty());
            post.setCanPlus(!matcher.group(14).isEmpty());
            post.setCanReport(!matcher.group(15).isEmpty());
            post.setCanEdit(!matcher.group(16).isEmpty());
            post.setCanDelete(!matcher.group(17).isEmpty());
            post.setCanQoute(!matcher.group(18).isEmpty());
            post.setBody(matcher.group(19));
            page.addPost(post);
        }

        if (generateHtml) {
            MiniTemplator t = App.getInstance().getTemplator();
            boolean authorized = Api.Auth().getState();
            boolean prevDisabled = page.getCurrentPage() <= 1;
            boolean nextDisabled = page.getCurrentPage() == page.getAllPagesCount();

            if (t.variableExists("topic_title"))
                t.setVariable("topic_title", page.getTitle());

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

            if (t.variableExists("topic_url"))
                t.setVariable("topic_url", url);

            if (t.variableExists("disable_avatar"))
                t.setVariable("disable_avatar", true ? "" : "disable_avatar");

            if (t.variableExists("avatar_type"))
                t.setVariable("avatar_type", true ? "" : "avatar_circle");

            int hatPostId = page.getPosts().get(0).getId();
            boolean existOnline = t.variableExists("user_online");
            boolean existPostId = t.variableExists("post_id");
            boolean existUserId = t.variableExists("user_id");
            boolean existAvatar = t.variableExists("avatar");
            boolean existNick = t.variableExists("nick");
            boolean existGroupColor = t.variableExists("group_color");
            boolean existGroup = t.variableExists("group");
            boolean existReputation = t.variableExists("reputation");
            boolean existDate = t.variableExists("date");
            boolean existNumber = t.variableExists("number");
            boolean existBody = t.variableExists("body");

            boolean existReportBlock = t.blockExists("report_block");
            boolean existNickBlock = t.blockExists("nick_block");
            boolean existQuoteBlock = t.blockExists("quote_block");
            boolean existVoteBlock = t.blockExists("vote_block");
            boolean existDeleteBlock = t.blockExists("delete_block");
            boolean existEditBlock = t.blockExists("edit_block");
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
                if (hatPostId == post.getId())
                    if (t.blockExists("hat_button"))
                        t.addBlock("hat_button");
                if (existBody)
                    t.setVariable("body", post.getBody());

                //Post footer
                if (existReportBlock && post.canReport() && authorized)
                    t.addBlock("report_block");
                if (existNickBlock && post.canQuote() && authorized)
                    t.addBlock("nick_block");
                if (existQuoteBlock && post.canQuote() && authorized)
                    t.addBlock("quote_block");
                if (existVoteBlock && authorized)
                    t.addBlock("vote_block");
                if (existDeleteBlock && post.canDelete() && authorized)
                    t.addBlock("delete_block");
                if (existEditBlock && post.canDelete() && authorized)
                    t.addBlock("edit_block");

                t.addBlock("post");
            }
            page.setHtml(t.generateOutput());
            t.reset();
        }

        Log.d("kek", "theme parsing time " + (new Date().getTime() - date.getTime()));
        return page;
    }

    private String getDisableStr(boolean b) {
        return b ? "disabled" : "";
    }

    public Observable<ThemePage> getPage(final String url) {
        return getPage(url, false);
    }

    public Observable<ThemePage> getPage(final String url, boolean generateHtml) {
        return Observable.create(subscriber -> {
            try {
                subscriber.onNext(_getPage(url, generateHtml));
                subscriber.onComplete();
            } catch (Exception e) {
                subscriber.onError(e);
            }
        });
    }
}
