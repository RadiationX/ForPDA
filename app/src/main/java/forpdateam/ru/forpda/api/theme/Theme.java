package forpdateam.ru.forpda.api.theme;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import biz.source_code.miniTemplator.MiniTemplator;
import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.api.others.pagination.Pagination;
import forpdateam.ru.forpda.api.theme.models.Poll;
import forpdateam.ru.forpda.api.theme.models.PollQuestion;
import forpdateam.ru.forpda.api.theme.models.PollQuestionItem;
import forpdateam.ru.forpda.api.theme.models.ThemePage;
import forpdateam.ru.forpda.api.theme.models.ThemePost;
import forpdateam.ru.forpda.client.Client;
import forpdateam.ru.forpda.client.OnlyShowException;
import forpdateam.ru.forpda.utils.Utils;
import forpdateam.ru.forpda.utils.ourparser.Html;
import io.reactivex.Observable;

/**
 * Created by radiationx on 04.08.16.
 */
public class Theme {
    //y: Oh God... Why?
    //g: Because it is faster
    private final static Pattern postsPattern = Pattern.compile("<a name=\"entry([^\"]*?)\"[^>]*?><\\/a><div class=\"post_header_container\"><div class=\"post_header\"><span class=\"post_date\">([^&]*?)&[^<]*?<a[^>]*?>#(\\d+)<\\/a>[^<]*?<\\/span>[\\s\\S]*?<font color=\"([^\"]*?)\">[^<]*?<\\/font>[\\s\\S]*?<a[^>]*?data-av=\"([^\"]*?)\"[^>]*?>([^<]*?)<[\\s\\S]*?<a[^>]*?showuser=([^\"]*?)\"[^>]*?>[^<]*?<\\/a>[\\s\\S]*?<span[^>]*?post_user_info[^>]>(<strong[\\s\\S]*?<\\/strong>(?:<br[^>]*?>))?(?:<span[^<]*?color:([^;']*)[^>]*?>)?([\\s\\S]*?)(?:<\\/span>|)(?:  \\| [^<]*?)?<\\/span>[\\s\\S]*?(<a[^>]*?win_minus[^>]*?>[\\s\\S]*?<\\/a>|) \\([\\s\\S]*?ajaxrep[^>]*?>([^<]*?)<\\/span><\\/a>\\)[^<]*(<a[^>]*?win_add[^>]*?>[\\s\\S]*?<\\/a>|)<br[^>]*?>[^<]*?<span class=\"post_action\">(<a[^>]*?report[^>]*?>[^<]*?<\\/a>|)[^<]*(<a[^>]*?edit_post[^>]*?>[^<]*?<\\/a>|)[^<]*(<a[^>]*?delete[^>]*?>[^<]*?<\\/a>|)[^<]*(<a[^>]*?CODE=02[^>]*?>[^<]*?<\\/a>|)[^<]*[^<]*[\\s\\S]*?<div class=\"post_body[^>]*?>([\\s\\S]*?)<\\/div><\\/div>(?:<div data-post|<!-- TABLE FOOTER -->|<div class=\"topic_foot_nav\">)");

    private final static Pattern titlePattern = Pattern.compile("<div class=\"topic_title_post\">(?:([^<]*?)(?:, ([^<]*?)|))<br");
    private final static Pattern alreadyInFavPattern = Pattern.compile("Тема уже добавлена в <a href=\"[^\"]*act=fav\">");
    private final static Pattern favIdPatternt = Pattern.compile("href=\"[^\"]*?act=fav[^\"]*?tact=delete[^\"]*?selectedtids=(\\d+)");
    private final static Pattern themeIdPattern = Pattern.compile("ipb_input_f:(\\d+),[\\s\\S]*?ipb_input_t:(\\d+),");
    public final static Pattern elemToScrollPattern = Pattern.compile("(?:anchor=|#)([^&\\n\\=\\?\\.\\#]*)");
    //private final static Pattern newsPattern = Pattern.compile("<section[^>]*?><article[^>]*?>[^<]*?<div class=\"container\"[\\s\\S]*?<img[^>]*?src=\"([^\"]*?)\" alt=\"([\\s\\S]*?)\"[\\s\\S]*?<em[^>]*>([^<]*?)</em>[\\s\\S]*?<a href=\"([^\"]*?)\">([\\s\\S]*?)</a>[\\s\\S]*?<a[^>]*?>([^<]*?)</a><div[^>]*?># ([\\s\\S]*?)</div>[\\s\\S]*?<div class=\"content-box\"[^>]*?>([\\s\\S]*?)</div></div></div>[^<]*?<div class=\"materials-box\">[\\s\\S]*?(<ul[\\s\\S]*?/ul>)[\\s\\S]*?<div class=\"comment-box\" id=\"comments\">[\\s\\S]*?(<ul[\\s\\S]*?/ul>)[^<]*?<form");

    private final static Pattern pollMainPattern = Pattern.compile("<form[^>]*?addpoll[^>]*?post[^>]*?>[\\s\\S]*?<tr[^>]*?>[^<]*?<th[^>]*?>(?:<[^>]*>)([^>]*?)(?:<[^>]*>)<\\/th>[^<]*?<\\/tr>([\\s\\S]*?)<tr>[^<]*?<td[^>]*?>[^<]*?(?:<b>)Всего голосов: ([\\d]*?)(?:<\\/b>)[\\s\\S]*?<td[^>]*?formbuttonrow[^>]*?>([\\s\\S]*?)<\\/td>[\\s\\S]*?<\\/form>");
    private final static Pattern pollQuestions = Pattern.compile("<tr><td[^>]*?><div class[\\s\\S]*?<strong>([\\s\\S]*?)<\\/strong>[\\s\\S]*?<table[^>]*?>([\\s\\S]*?)<\\/table>");
    private final static Pattern pollQuestionItems = Pattern.compile("<tr>(?:<td[^>]*?colspan[^>]*?><input type=\"([^\"]*?)\" name=\"([^\"]*?)\" value=\"([^\"]*?)\"[^>]*?>[^<]*?<b>([\\s\\S]*?)<\\/b>[\\s\\S]*?|<td[^>]*?width[^>]*?>([\\s\\S]*?)<\\/td><td[^>]*?>[^<]*?<b>([\\s\\S]*?)<\\/b>[^\\[]*?\\[([^\\%]*?)\\%[\\s\\S]*?)<\\/tr>");
    private final static Pattern pollButtons = Pattern.compile("<input[^>]*?value=\"([^\"]*?)\"");

    public Theme() {
    }

    public Observable<ThemePage> getPage(final String url) {
        return getPage(url, false);
    }

    public Observable<ThemePage> getPage(final String url, boolean generateHtml) {
        return Observable.fromCallable(() -> _getPage(url, generateHtml));
    }


    public ThemePage _getPage(final String url, boolean generateHtml) throws Exception {
        Log.d("kek", "page start _getPage");
        String response = Client.getInstance().get(url);


        return parsePage(url, response, generateHtml);
    }

    private final static Pattern firstLetter = Pattern.compile("[^a-zA-Z]*?([a-zA-Z])");

    public ThemePage parsePage(String url, String response, boolean generateHtml) {
        ThemePage page = new ThemePage();
        String redirectUrl = Client.getInstance().getRedirect(url);
        if (redirectUrl == null)
            redirectUrl = url;
        page.setUrl(redirectUrl);

        Log.d("kek", "page getted");
        Log.d("kek", "page redirected " + redirectUrl);
        long time = System.currentTimeMillis();
        Matcher matcher = elemToScrollPattern.matcher(redirectUrl);
        while (matcher.find()) {
            page.setElementToScroll(matcher.group(1));
        }
        matcher = themeIdPattern.matcher(response);
        if (matcher.find()) {
            Log.d("suka", "IDS PARSING " + matcher.group(1) + " : " + matcher.group(2));
            page.setForumId(Integer.parseInt(matcher.group(1)));
            page.setId(Integer.parseInt(matcher.group(2)));
        }
        page.setPagination(Pagination.parseForum(response));
        matcher = titlePattern.matcher(response);
        if (matcher.find()) {
            page.setTitle(Utils.fromHtml(matcher.group(1)));
            page.setDesc(Utils.fromHtml(matcher.group(2)));
        }
        matcher = alreadyInFavPattern.matcher(response);
        if (matcher.find()) {
            page.setInFavorite(true);
            matcher = favIdPatternt.matcher(response);
            if (matcher.find()) {
                page.setFavId(Integer.parseInt(matcher.group(1)));
            }
        }
        matcher = postsPattern.matcher(response);
        Log.d("kek", "posts matcher " + (System.currentTimeMillis() - time));
        int memberId = Api.Auth().getUserIdInt();
        while (matcher.find()) {
            ThemePost post = new ThemePost();
            post.setId(Integer.parseInt(matcher.group(1)));
            post.setDate(matcher.group(2));
            post.setNumber(Integer.parseInt(matcher.group(3)));
            post.setOnline(matcher.group(4).contains("green"));
            post.setAvatar(matcher.group(5));
            post.setNick(Utils.fromHtml(matcher.group(6)));
            post.setUserId(Integer.parseInt(matcher.group(7)));
            post.setCurator(matcher.group(8) != null);
            post.setGroupColor(matcher.group(9));
            post.setGroup(matcher.group(10));
            post.setCanMinus(!matcher.group(11).isEmpty());
            post.setReputation(matcher.group(12));
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
        Log.d("kek", "poll matcher " + (System.currentTimeMillis() - time));
        matcher = pollMainPattern.matcher(response);
        if (matcher.find()) {
            Poll poll = new Poll();
            final boolean isResult = matcher.group().contains("img");
            poll.setIsResult(isResult);
            poll.setTitle(Utils.fromHtml(matcher.group(1)));
            Matcher matcher1 = pollQuestions.matcher(matcher.group(2));
            while (matcher1.find()) {
                PollQuestion pollQuestion = new PollQuestion();
                pollQuestion.setTitle(Utils.fromHtml(matcher1.group(1)));
                Matcher itemsMatcher = pollQuestionItems.matcher(matcher1.group(2));
                while (itemsMatcher.find()) {
                    PollQuestionItem questionItem = new PollQuestionItem();
                    if (!isResult) {
                        questionItem.setType(itemsMatcher.group(1));
                        questionItem.setName(itemsMatcher.group(2));
                        questionItem.setValue(Integer.parseInt(itemsMatcher.group(3)));
                        questionItem.setTitle(Utils.fromHtml(itemsMatcher.group(4)));
                    } else {
                        questionItem.setTitle(Utils.fromHtml(itemsMatcher.group(5)));
                        questionItem.setVotes(Integer.parseInt(itemsMatcher.group(6)));
                        questionItem.setPercent(Float.parseFloat(itemsMatcher.group(7).replace(",", ".")));
                    }
                    pollQuestion.addItem(questionItem);
                }
                poll.addQuestion(pollQuestion);
            }
            matcher1 = pollButtons.matcher(matcher.group(4));
            while (matcher1.find()) {
                String value = matcher1.group(1);
                if (value.contains("Голосовать")) {
                    poll.setVoteButton();
                } else if (value.contains("результаты")) {
                    poll.setShowResultButton();
                } else if (value.contains("пункты опроса")) {
                    poll.setShowPollButton();
                }
            }
            poll.setVotesCount(Integer.parseInt(matcher.group(3)));
            page.setPoll(poll);
        }
        Log.d("kek", "end created page obj " + (System.currentTimeMillis() - time));
        if (generateHtml) {
            long time2 = System.currentTimeMillis();
            MiniTemplator t = App.getInstance().getTemplator();
            boolean authorized = Api.Auth().getState();
            boolean prevDisabled = page.getPagination().getCurrent() <= 1;
            boolean nextDisabled = page.getPagination().getCurrent() == page.getPagination().getAll();

            t.setVariableOpt("topic_title", page.getTitle());
            t.setVariableOpt("topic_url", redirectUrl);
            t.setVariableOpt("topic_description", page.getDesc());
            t.setVariableOpt("in_favorite", Boolean.toString(page.isInFavorite()));
            t.setVariableOpt("all_pages", page.getPagination().getAll());
            t.setVariableOpt("posts_on_page", page.getPagination().getPerPage());
            t.setVariableOpt("current_page", page.getPagination().getCurrent());
            t.setVariableOpt("authorized", Boolean.toString(authorized));
            t.setVariableOpt("is_curator", Boolean.toString(page.isCurator()));
            t.setVariableOpt("member_id", Api.Auth().getUserIdInt());
            t.setVariableOpt("elem_to_scroll", page.getElementToScroll());
            t.setVariableOpt("body_type", "topic");
            t.setVariableOpt("navigation_disable", prevDisabled && nextDisabled ? "navigation_disable" : "");
            t.setVariableOpt("first_disable", getDisableStr(prevDisabled));
            t.setVariableOpt("prev_disable", getDisableStr(prevDisabled));
            t.setVariableOpt("next_disable", getDisableStr(nextDisabled));
            t.setVariableOpt("last_disable", getDisableStr(nextDisabled));
            t.setVariableOpt("disable_avatar_js", Boolean.toString(true));
            t.setVariableOpt("disable_avatar", App.getInstance().getPreferences().getBoolean("theme.show_avatars", true) ? "" : "disable_avatar");
            t.setVariableOpt("avatar_type", App.getInstance().getPreferences().getBoolean("theme.circle_avatars", true) ? "avatar_circle" : "");

            Log.d("kek", "template check 1 " + (System.currentTimeMillis() - time2));

            int hatPostId = page.getPosts().get(0).getId();
            Log.d("kek", "template check 2 " + (System.currentTimeMillis() - time2));
            String letter;
            Matcher letterMatcher = null;
            for (ThemePost post : page.getPosts()) {
                t.setVariableOpt("user_online", post.isOnline() ? "online" : "");
                t.setVariableOpt("post_id", post.getId());
                t.setVariableOpt("user_id", post.getUserId());

                //Post header
                //t.setVariableOpt("avatar", post.getAvatar().isEmpty() ? "file:///android_asset/av.png" : "http://s.4pda.to/forum/uploads/".concat(post.getAvatar()));
                t.setVariableOpt("avatar", post.getAvatar().isEmpty() ? "" : "http://s.4pda.to/forum/uploads/".concat(post.getAvatar()));
                t.setVariableOpt("none_avatar", post.getAvatar().isEmpty() ? "none_avatar" : "");

                letter = post.getNick().substring(0, 1);
                if (letterMatcher != null) {
                    letterMatcher = letterMatcher.reset(letter);
                } else {
                    letterMatcher = firstLetter.matcher(letter);
                }
                if (!letterMatcher.find()) {
                    letterMatcher = letterMatcher.reset(post.getNick());
                    if (letterMatcher.find()) {
                        letter = letterMatcher.group(1);
                    }
                }
                t.setVariableOpt("nick_letter", letter);
                t.setVariableOpt("nick", post.getNick());
                t.setVariableOpt("curator", post.isCurator() ? "curator" : "");
                t.setVariableOpt("group_color", post.getGroupColor());
                t.setVariableOpt("group", post.getGroup());
                t.setVariableOpt("reputation", post.getReputation());
                t.setVariableOpt("date", post.getDate());
                t.setVariableOpt("number", post.getNumber());

                //Post body
                if (page.getPosts().size() > 1 && hatPostId == post.getId()) {
                    t.setVariableOpt("hat_state_class", "close");
                    t.addBlockOpt("hat_button");
                    t.addBlockOpt("hat_content_start");
                    t.addBlockOpt("hat_content_end");
                } else {
                    t.setVariableOpt("hat_state_class", "");
                }
                t.setVariableOpt("body", post.getBody());

                //Post footer

                if (post.canReport() && authorized)
                    t.addBlockOpt("report_block");
                if (page.canQuote() && authorized && post.getUserId() != memberId)
                    t.addBlockOpt("reply_block");
                if (authorized && post.getUserId() != memberId)
                    t.addBlockOpt("vote_block");
                if (post.canDelete() && authorized)
                    t.addBlockOpt("delete_block");
                if (post.canEdit() && authorized)
                    t.addBlockOpt("edit_block");

                t.addBlockOpt("post");
            }

            //Poll block

            if (page.getPoll() != null) {
                Poll poll = page.getPoll();
                boolean isResult = poll.isResult();
                t.setVariableOpt("poll_type", isResult ? "result" : "default");
                t.setVariableOpt("poll_title", poll.getTitle().isEmpty() || poll.getTitle().equals("-") ? "Опрос" : poll.getTitle());

                for (PollQuestion question : poll.getQuestions()) {
                    t.setVariableOpt("question_title", question.getTitle());

                    for (PollQuestionItem questionItem : question.getQuestionItems()) {
                        t.setVariableOpt("question_item_title", questionItem.getTitle());

                        if (isResult) {
                            t.setVariableOpt("question_item_votes", questionItem.getVotes());
                            t.setVariableOpt("question_item_percent", Float.toString(questionItem.getPercent()));
                            t.addBlockOpt("poll_result_item");
                        } else {
                            t.setVariableOpt("question_item_type", questionItem.getType());
                            t.setVariableOpt("question_item_name", questionItem.getName());
                            t.setVariableOpt("question_item_value", questionItem.getValue());
                            t.addBlockOpt("poll_default_item");
                        }
                    }
                    t.addBlockOpt("poll_question_block");
                }
                t.setVariableOpt("poll_votes_count", poll.getVotesCount());
                if (poll.haveButtons()) {
                    if (poll.haveVoteButton())
                        t.addBlockOpt("poll_vote_button");
                    if (poll.haveShowResultsButton())
                        t.addBlockOpt("poll_show_results_button");
                    if (poll.haveShowPollButton())
                        t.addBlockOpt("poll_show_poll_button");
                    t.addBlockOpt("poll_buttons");
                }
                t.addBlockOpt("poll_block");
            }


            Log.d("kek", "template check 3 " + (System.currentTimeMillis() - time2));
            page.setHtml(t.generateOutput());
            Log.d("kek", "template check 4 " + (System.currentTimeMillis() - time2));
            t.reset();
            Log.d("kek", "template check 5 " + (System.currentTimeMillis() - time2));
        }

        Log.d("kek", "theme parsing time " + (System.currentTimeMillis() - time));
        /*final String veryLongString = page.getHtml();

        int maxLogSize = 1000;
        for (int i = 0; i <= veryLongString.length() / maxLogSize; i++) {
            int start = i * maxLogSize;
            int end = (i + 1) * maxLogSize;
            end = end > veryLongString.length() ? veryLongString.length() : end;
            Log.v("SUKA", veryLongString.substring(start, end));
        }*/

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
        String url = "http://4pda.ru/forum/index.php?act=zmod&auth_key=".concat(Client.getAuthKey()).concat("&code=postchoice&tact=delete&selectedpids=").concat(Integer.toString(postId));
        String response = Client.getInstance().get(url);
        return response.equals("ok") ? response : "";
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
}
