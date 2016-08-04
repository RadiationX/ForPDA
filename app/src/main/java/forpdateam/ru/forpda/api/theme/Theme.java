package forpdateam.ru.forpda.api.theme;

import android.util.Log;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import forpdateam.ru.forpda.api.theme.models.ThemePage;
import forpdateam.ru.forpda.api.theme.models.ThemePost;
import forpdateam.ru.forpda.client.Client;
import rx.Observable;
import rx.Subscriber;

/**
 * Created by radiationx on 04.08.16.
 */
public class Theme {
    //y: Oh God... Why?
    //g: Because it is faster
    private final static Pattern postsPattern = Pattern.compile("<a name=\"entry([^\"]*?)\"[^>]*?></a><div class=\"post_header_container\"><div class=\"post_header\"><span class=\"post_date\">([^&]*?)&[^<]*?<a[^>]*?>#(\\d+)</a>\\|</span>[\\s\\S]*?<span[^>]*?><a[^>]*?data-av=\"([^\"]*?)\">([^<]*?)</a></span><br[^>]*?>[\\s\\S]*?<span[^>]*?>(<[^>]*?>([^<]*?)</[^>]*?><br[^>]*?>|)[^<]*?<span[^>]*?color:([^;']*?)'>([^<]*?)</span><br[^>]*?><font color=\"([^\"]*?)\">[^<]*?</font>[\\s\\S]*?<a[^>]*?showuser=([^\"]*?)\">[^<]*?</a>[\\s\\S]*?ajaxrep[^>]*?>([^<]*?)</span></a>\\) [\\s\\S]*?(<a[^>]*?win_minus[^>]*?><img[^>]*?></a>|)[^<]*(<a[^>]*?win_add[^>]*?><img[^>]*?></a>|)<br[^>]*?>[^<]*?<span class=\"post_action\">(<a[^>]*?report[^>]*?>[^<]*?</a>|)[^<]*(<a[^>]*?edit_post[^>]*?>[^<]*?</a>|)[^<]*(<a[^>]*?delete[^>]*?>[^<]*?</a>|)[^<]*(<a[^>]*?CODE=02[^>]*?>[^<]*?</a>|)[^<]*[^<]*[\\s\\S]*?<div class=\"post_body[^>]*?>([\\s\\S]*?)</div></div>(<div data-post=|<!-- TABLE FOOTER -->)");
    private final static Pattern countsPattern = Pattern.compile("parseInt\\((\\d*)\\)[\\s\\S]*?parseInt\\(st\\*(\\d*)\\)");
    private final static Pattern titlePattern = Pattern.compile("<div class=\"topic_title_post\">([^,<]*)(, ([^<]*)|)<");
    private final static Pattern alreadyInFavPattern = Pattern.compile("Тема уже добавлена в <a href=\"[^\"]*act=fav\">");

    private ThemePage get(final String url) throws Exception {
        ThemePage page = new ThemePage();
        try {
            String response = Client.getInstance().get(url);
            Date date = new Date();
            Matcher matcher = countsPattern.matcher(response);
            if (matcher.find()) {
                page.setAllPagesCount(Integer.parseInt(matcher.group(1)));
                page.setPostsOnPageCount(Integer.parseInt(matcher.group(2)));
            }
            matcher = titlePattern.matcher(response);
            if(matcher.find()){
                page.setTitle(matcher.group(1));
                page.setDesc(matcher.group(3));
            }
            matcher = alreadyInFavPattern.matcher(response);
            page.setInFavorite(matcher.matches());
            matcher = postsPattern.matcher(response);
            while (matcher.find()) {
                ThemePost post = new ThemePost();
                post.setId(matcher.group(1));
                post.setDate(matcher.group(2));
                post.setNumber(matcher.group(3));
                post.setUserAvatar(matcher.group(4));
                post.setUserName(matcher.group(5));
                post.setCurator(!matcher.group(6).isEmpty());
                post.setGroupColor(matcher.group(8));
                post.setGroup(matcher.group(9));
                post.setOnline(matcher.group(10).contains("green"));
                post.setUserId(matcher.group(11));
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
            Log.d("kek", "parsing time " + (new Date().getTime() - date.getTime()));
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return page;
    }

    public Observable<ThemePage> getPage(final String url) {
        return Observable.create(new Observable.OnSubscribe<ThemePage>() {
            @Override
            public void call(Subscriber<? super ThemePage> subscriber) {
                try {
                    subscriber.onNext(get(url));
                    subscriber.onCompleted();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        });
    }
}
