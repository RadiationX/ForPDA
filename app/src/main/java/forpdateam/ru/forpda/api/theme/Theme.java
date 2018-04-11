package forpdateam.ru.forpda.api.theme;

import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.api.ApiUtils;
import forpdateam.ru.forpda.api.NetworkRequest;
import forpdateam.ru.forpda.api.NetworkResponse;
import forpdateam.ru.forpda.api.others.pagination.Pagination;
import forpdateam.ru.forpda.api.theme.models.Poll;
import forpdateam.ru.forpda.api.theme.models.PollQuestion;
import forpdateam.ru.forpda.api.theme.models.PollQuestionItem;
import forpdateam.ru.forpda.api.theme.models.ThemePage;
import forpdateam.ru.forpda.api.theme.models.ThemePost;
import forpdateam.ru.forpda.client.ClientHelper;

/**
 * Created by radiationx on 04.08.16.
 */
public class Theme {
    //y: Oh God... Why?
    //g: Because it is faster
    private final static Pattern postsPattern = Pattern.compile("<a name=\"entry([^\"]*?)\"[^>]*?><\\/a><div class=\"post_header_container\"><div class=\"post_header\"><span class=\"post_date\">([^&]*?)&[^<]*?<a[^>]*?>#(\\d+)<\\/a>[^<]*?<\\/span>[\\s\\S]*?<font color=\"([^\"]*?)\">[^<]*?<\\/font>[\\s\\S]*?<a[^>]*?data-av=\"([^\"]*?)\"[^>]*?>([^<]*?)<[\\s\\S]*?<a[^>]*?showuser=([^\"]*?)\"[^>]*?>[^<]*?<\\/a>[\\s\\S]*?<span[^>]*?post_user_info[^>]>(<strong[\\s\\S]*?<\\/strong>(?:<br[^>]*?>))?(?:<span[^<]*?color:([^;']*)[^>]*?>)?([\\s\\S]*?)(?:<\\/span>|)(?:  \\| [^<]*?)?<\\/span>[\\s\\S]*?(<a[^>]*?win_minus[^>]*?>[\\s\\S]*?<\\/a>|) \\([\\s\\S]*?ajaxrep[^>]*?>([^<]*?)<\\/span><\\/a>\\)[^<]*(<a[^>]*?win_add[^>]*?>[\\s\\S]*?<\\/a>|)<br[^>]*?>[^<]*?<span class=\"post_action\">(<a[^>]*?report[^>]*?>[^<]*?<\\/a>|)[^<]*(<a[^>]*?edit_post[^>]*?>[^<]*?<\\/a>|)[^<]*(<a[^>]*?delete[^>]*?>[^<]*?<\\/a>|)[^<]*(<a[^>]*?CODE=02[^>]*?>[^<]*?<\\/a>|)[^<]*[^<]*[\\s\\S]*?<div class=\"post_body[^>]*?>([\\s\\S]*?)<\\/div><\\/div>(?:<div data-post|<!-- TABLE FOOTER -->|<div class=\"topic_foot_nav\">)");

    private final static Pattern universalForumPosts = Pattern.compile("(?:<a name=\"entry([^\"]*?)\"[^>]*?><\\/a>|<div[^>]*?class=\"cat_name[^>]*?>[^<]*?<a[^>]*?showtopic=(\\d+)[^>]*?p=(\\d+)[^>]*?>([\\s\\S]*?)<\\/a><\\/div>)[\\s\\S]*?<div class=\"post_header_container\"><div class=\"post_header\"><span class=\"post_date\">([^&]*?)&[^<]*?<a[^>]*?>#?([^<]*?)<\\/a>[^<]*?<\\/span>[\\s\\S]*?<font color=\"([^\"]*?)\">[^<]*?<\\/font>[\\s\\S]*?<a[^>]*?data-av=\"([^\"]*?)\"[^>]*?>([^<]*?)<[\\s\\S]*?<a[^>]*?showuser=([^\"]*?)\"[^>]*?>[^<]*?<\\/a>[\\s\\S]*?<span[^>]*?post_user_info[^>]>(<strong[\\s\\S]*?<\\/strong>(?:<br[^>]*?>))?(?:<span[^<]*?color:([^;']*)[^>]*?>)?([\\s\\S]*?)(?:<\\/span>|)(?:  \\| [^<]*?)?<\\/span>[\\s\\S]*?(<a[^>]*?win_minus[^>]*?>[\\s\\S]*?<\\/a>|) \\([\\s\\S]*?data-member-rep[^>]*?>([^<]*?)<\\/span><\\/a>\\)[^<]*(<a[^>]*?win_add[^>]*?>[\\s\\S]*?<\\/a>|)<br[^>]*?>[^<]*?<span class=\"post_action\">(<a[^>]*?report[^>]*?>[^<]*?<\\/a>|)[^<]*(<a[^>]*?edit_post[^>]*?>[^<]*?<\\/a>|)[^<]*(<a[^>]*?delete[^>]*?>[^<]*?<\\/a>|)[^<]*(<a[^>]*?CODE=02[^>]*?>[^<]*?<\\/a>|)[^<]*[^<]*[\\s\\S]*?<div class=\"post_body[^>]*?>([\\s\\S]*?)<\\/div><\\/div>(?=<div[^>]*?class=\"cat_name|<div><div[^>]*?class=\"pagination|<div><\\/div><br[^>]*?><\\/form>|<div data-post|<!-- TABLE FOOTER -->|<div class=\"topic_foot_nav\">)");

    private final static Pattern titlePattern = Pattern.compile("<div class=\"topic_title_post\">(?:([^<]*?)(?: ?\\| ?([^<]*?)|))<br");
    private final static Pattern alreadyInFavPattern = Pattern.compile("<a href=\"[^\"]*?act=fav[^\"]*?tact=delete[^\"]*?\"[^>]*?>");
    private final static Pattern favIdPattern = Pattern.compile("href=\"[^\"]*?act=fav[^\"]*?tact=delete[^\"]*?selectedtids=(\\d+)");
    public final static Pattern themeIdPattern = Pattern.compile("ipb_input_f:(\\d+),[\\s\\S]*?ipb_input_t:(\\d+),");
    public final static Pattern elemToScrollPattern = Pattern.compile("(?:anchor=|#)([^&\\n\\=\\?\\.\\#]*)");
    //private final static Pattern newsPattern = Pattern.compile("<section[^>]*?><article[^>]*?>[^<]*?<div class=\"container\"[\\s\\S]*?<img[^>]*?src=\"([^\"]*?)\" alt=\"([\\s\\S]*?)\"[\\s\\S]*?<em[^>]*>([^<]*?)</em>[\\s\\S]*?<a href=\"([^\"]*?)\">([\\s\\S]*?)</a>[\\s\\S]*?<a[^>]*?>([^<]*?)</a><div[^>]*?># ([\\s\\S]*?)</div>[\\s\\S]*?<div class=\"content-box\"[^>]*?>([\\s\\S]*?)</div></div></div>[^<]*?<div class=\"materials-box\">[\\s\\S]*?(<ul[\\s\\S]*?/ul>)[\\s\\S]*?<div class=\"comment-box\" id=\"comments\">[\\s\\S]*?(<ul[\\s\\S]*?/ul>)[^<]*?<form");

    private final static Pattern pollMainPattern = Pattern.compile("<form[^>]*?addpoll[^>]*?post[^>]*?>[\\s\\S]*?<tr[^>]*?>[^<]*?<th[^>]*?>(?:<[^>]*>)([^>]*?)(?:<[^>]*>)<\\/th>[^<]*?<\\/tr>([\\s\\S]*?)<tr>[^<]*?<td[^>]*?>[^<]*?(?:<b>)Всего голосов: ([\\d]*?)(?:<\\/b>)[\\s\\S]*?<td[^>]*?formbuttonrow[^>]*?>([\\s\\S]*?)<\\/td>[\\s\\S]*?<\\/form>");
    private final static Pattern pollQuestions = Pattern.compile("<tr><td[^>]*?><div class[\\s\\S]*?<strong>([\\s\\S]*?)<\\/strong>[\\s\\S]*?<table[^>]*?>([\\s\\S]*?)<\\/table>");
    private final static Pattern pollQuestionItems = Pattern.compile("<tr>(?:<td[^>]*?colspan[^>]*?><input type=\"([^\"]*?)\" name=\"([^\"]*?)\" value=\"([^\"]*?)\"[^>]*?>[^<]*?<b>([\\s\\S]*?)<\\/b>[\\s\\S]*?|<td[^>]*?width[^>]*?>([\\s\\S]*?)<\\/td><td[^>]*?>[^<]*?<b>([\\s\\S]*?)<\\/b>[^\\[]*?\\[([^\\%]*?)\\%[\\s\\S]*?)<\\/tr>");
    private final static Pattern pollButtons = Pattern.compile("<input[^>]*?value=\"([^\"]*?)\"");
    public final static Pattern attachImagesPattern = Pattern.compile("(4pda\\.ru\\/forum\\/dl\\/post\\/\\d+\\/[^\"']*?\\.(?:jpe?g|png|gif|bmp))\"?(?:[^>]*?title=\"([^\"']*?\\.(?:jpe?g|png|gif|bmp)) - [^\"']*?\")?");

    public Theme() {
    }


    public ThemePage getTheme(final String url, boolean hatOpen, boolean pollOpen) throws Exception {
        NetworkResponse response = Api.getWebClient().get(url);
        return parsePage(url, response, hatOpen, pollOpen);
    }

    public ThemePage parsePage(String url, NetworkResponse response, boolean hatOpen, boolean pollOpen) throws Exception {
        ThemePage page = new ThemePage();
        page.setHatOpen(hatOpen);
        page.setPollOpen(pollOpen);
        String redirectUrl = response.getRedirect();
        if (redirectUrl == null)
            redirectUrl = url;
        page.setUrl(redirectUrl);

        long time = System.currentTimeMillis();
        Matcher matcher = elemToScrollPattern.matcher(redirectUrl);
        while (matcher.find()) {
            page.addAnchor(matcher.group(1));
        }
        matcher = themeIdPattern.matcher(response.getBody());
        if (matcher.find()) {
            page.setForumId(Integer.parseInt(matcher.group(1)));
            page.setId(Integer.parseInt(matcher.group(2)));
        }
        page.setPagination(Pagination.parseForum(response.getBody()));
        matcher = titlePattern.matcher(response.getBody());
        if (matcher.find()) {
            page.setTitle(ApiUtils.fromHtml(matcher.group(1)));
            page.setDesc(ApiUtils.fromHtml(matcher.group(2)));
        }
        matcher = alreadyInFavPattern.matcher(response.getBody());
        if (matcher.find()) {
            page.setInFavorite(true);
            matcher = favIdPattern.matcher(response.getBody());
            if (matcher.find()) {
                page.setFavId(Integer.parseInt(matcher.group(1)));
            }
        }
        matcher = universalForumPosts.matcher(response.getBody());
        Matcher attachMatcher = null;
        while (matcher.find()) {
            ThemePost item = new ThemePost();
            item.setTopicId(page.getId());
            item.setForumId(page.getForumId());
            item.setId(Integer.parseInt(matcher.group(1)));
            item.setDate(matcher.group(5));
            item.setNumber(Integer.parseInt(matcher.group(6)));
            item.setOnline(matcher.group(7).contains("green"));
            String avatar = matcher.group(8);
            if (!avatar.isEmpty()) {
                avatar = "https://s.4pda.to/forum/uploads/".concat(avatar);
            }
            item.setAvatar(avatar);
            item.setNick(ApiUtils.fromHtml(matcher.group(9)));
            item.setUserId(Integer.parseInt(matcher.group(10)));
            item.setCurator(matcher.group(11) != null);
            item.setGroupColor(matcher.group(12));
            item.setGroup(matcher.group(13));
            item.setCanMinus(!matcher.group(14).isEmpty());
            item.setReputation(matcher.group(15));
            item.setCanPlus(!matcher.group(16).isEmpty());
            item.setCanReport(!matcher.group(17).isEmpty());
            item.setCanEdit(!matcher.group(18).isEmpty());
            item.setCanDelete(!matcher.group(19).isEmpty());
            page.setCanQuote(!matcher.group(20).isEmpty());
            item.setCanQuote(page.canQuote());
            item.setBody(matcher.group(21));
            if (attachMatcher == null) {
                attachMatcher = attachImagesPattern.matcher(item.getBody());
            } else {
                attachMatcher.reset(item.getBody());
            }
            while (attachMatcher.find()) {
                item.addAttachImage(attachMatcher.group(1), attachMatcher.group(2));
            }
            if (item.isCurator() && item.getUserId() == ClientHelper.getUserId())
                page.setCurator(true);
            page.addPost(item);
        }
        matcher = pollMainPattern.matcher(response.getBody());
        if (matcher.find()) {
            Poll poll = new Poll();
            final boolean isResult = matcher.group().contains("<img");
            poll.setIsResult(isResult);
            poll.setTitle(ApiUtils.fromHtml(matcher.group(1)));
            Matcher matcher1 = pollQuestions.matcher(matcher.group(2));
            while (matcher1.find()) {
                PollQuestion pollQuestion = new PollQuestion();
                pollQuestion.setTitle(ApiUtils.fromHtml(matcher1.group(1)));
                Matcher itemsMatcher = pollQuestionItems.matcher(matcher1.group(2));
                while (itemsMatcher.find()) {
                    PollQuestionItem questionItem = new PollQuestionItem();
                    if (!isResult) {
                        questionItem.setType(itemsMatcher.group(1));
                        questionItem.setName(ApiUtils.fromHtml(itemsMatcher.group(2)));
                        questionItem.setValue(Integer.parseInt(itemsMatcher.group(3)));
                        questionItem.setTitle(ApiUtils.fromHtml(itemsMatcher.group(4)));
                    } else {
                        questionItem.setTitle(ApiUtils.fromHtml(itemsMatcher.group(5)));
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

        return page;
    }

    public String reportPost(int topicId, int postId, String message) throws Exception {
        NetworkRequest request = new NetworkRequest.Builder()
                .url("https://4pda.ru/forum/index.php?act=report&send=1&t=" + Integer.toString(topicId) + "&p=" + Integer.toString(postId))
                .formHeader("message", URLEncoder.encode(message, "windows-1251"), true)
                .build();
        NetworkResponse response = Api.getWebClient().request(request);
        Pattern p = Pattern.compile("<div class=\"errorwrap\">\n" +
                "\\s*<h4>Причина:</h4>\n" +
                "\\s*\n" +
                "\\s*<p>(.*)</p>", Pattern.MULTILINE);
        Matcher m = p.matcher(response.getBody());
        return m.find() ? "Ошибка отправки жалобы: ".concat(m.group(1)) : "Жалоба отправлена";
    }


    public Boolean deletePost(int postId) throws Exception {
        String url = "https://4pda.ru/forum/index.php?act=zmod&auth_key=".concat(Api.getWebClient().getAuthKey()).concat("&code=postchoice&tact=delete&selectedpids=").concat(Integer.toString(postId));
        NetworkResponse response = Api.getWebClient().request(new NetworkRequest.Builder().url(url).xhrHeader().build());
        return response.getBody().equals("ok");
    }


    public String votePost(int postId, boolean type) throws Exception {
        NetworkResponse response = Api.getWebClient().get("https://4pda.ru/forum/zka.php?i=".concat(Integer.toString(postId)).concat("&v=").concat(type ? "1" : "-1"));
        String result = null;

        Matcher m = Pattern.compile("ok:\\s*?((?:\\+|\\-)?\\d+)").matcher(response.getBody());
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
