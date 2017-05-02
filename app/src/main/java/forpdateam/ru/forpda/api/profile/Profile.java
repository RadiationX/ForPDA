package forpdateam.ru.forpda.api.profile;

import android.text.Html;
import android.util.Pair;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.api.Utils;
import forpdateam.ru.forpda.api.profile.models.ProfileModel;
import forpdateam.ru.forpda.client.ForPdaRequest;

/**
 * Created by radiationx on 03.08.16.
 */
public class Profile {
    private static final Pattern mainPattern = Pattern.compile("<div[^>]*?user-box[\\s\\S]*?<img src=\"([^\"]*?)\"[\\s\\S]*?<h1>([^<]*?)<\\/h1>[\\s\\S]*?(?=<span class=\"title\">([^<]*?)<\\/span>| )[\\s\\S]*?<h2>(?:<span style[^>]*?>|)([^\"<]*?)(?:<\\/span>|)<\\/h2>[\\s\\S]*?(<ul[\\s\\S]*?\\/ul>)[\\s\\S]*?<div class=\"u-note\">([\\s\\S]*?)<\\/div>[\\s\\S]*?(<ul[\\s\\S]*?\\/ul>)[\\s\\S]*?(<ul[\\s\\S]*?\\/ul>)[\\s\\S]*?(<ul[\\s\\S]*?\\/ul>)[\\s\\S]*?(<ul[\\s\\S]*?\\/ul>)[\\s\\S]*?(<ul[\\s\\S]*?\\/ul>)");
    private static final Pattern info = Pattern.compile("<li[\\s\\S]*?title[^>]*?>([^>]*?)<[\\s\\S]*?div[^>]*>([\\s\\S]*?)</div>");
    private static final Pattern personal = Pattern.compile("<li[\\s\\S]*?title[^>]*?>([^>]*?)<[\\s\\S]*?(?=<div[^>]*>([^<]*)[\\s\\S]*?</div>|)<");
    private static final Pattern contacts = Pattern.compile("<a[^>]*?href=\"([^\"]*?)\"[^>]*?>(?=<strong>([\\s\\S]*?)</strong>|([\\s\\S]*?)</a>)");
    private static final Pattern devices = Pattern.compile("<a[^>]*?href=\"([^\"]*?)\"[^>]*?>([\\s\\S]*?)</a>([\\s\\S]*?)</li>");
    private static final Pattern siteStats = Pattern.compile("<span class=\"title\">([^<]*?)</span>[\\s\\S]*?<div class=\"area\">[\\s\\S]*?(?=<a[^>]*?href=\"([^\"]*?)\"[^>]*?>([\\s\\S]*?)<|([\\s\\S]*?)</div>)");
    private static final Pattern forumStats = Pattern.compile("<span class=\"title\">([^<]*?)</span>[\\s\\S]*?<div class=\"area\">[\\s\\S]*?<a[^>]*?href=\"([^\"]*?(history|pst|topics)[^\"]*?)\"[^>]*?>[^<]*?(<span[^>]*?>|)([^<]*?)(</span>|)</a>");
    private static final Pattern note = Pattern.compile("<textarea[^>]*?profile-textarea\"[^>]*?>([\\s\\S]*?)</textarea>");
    private static final Pattern about = Pattern.compile("<div[^>]*?div-custom-about[^>]*?>([\\s\\S]*?)</div>");

    public ProfileModel getProfile(String url) throws Exception {
        ProfileModel profile = new ProfileModel();
        final String response = Api.getWebClient().get(url);

        final Matcher mainMatcher = mainPattern.matcher(response);
        if (mainMatcher.find()) {
            profile.setAvatar(safe(mainMatcher.group(1)));
            profile.setNick(Utils.fromHtml(safe(mainMatcher.group(2))));
            profile.setStatus(safe(mainMatcher.group(3)));
            profile.setGroup(safe(mainMatcher.group(4)));

            Matcher data = info.matcher(mainMatcher.group(5));
            while (data.find()) {
                if (data.group(1).contains("Рег"))
                    profile.setRegDate(safe(data.group(2)));

                if (data.group(1).contains("Последнее"))
                    profile.setOnlineDate(safe(Utils.fromHtml(data.group(2).trim())));
            }

            String signString = safe(mainMatcher.group(6));
            profile.setSign(signString.equals("Нет подписи") ? null : Html.fromHtml(signString));

            data = personal.matcher(mainMatcher.group(7));
            while (data.find()) {
                if (data.group(2) == null || data.group(2).isEmpty())
                    profile.setGender(safe(data.group(1)));

                if (data.group(1).contains("Дата"))
                    profile.setBirthDay(safe(data.group(2)));

                if (data.group(1).contains("Время"))
                    profile.setUserTime(safe(data.group(2)));

                if (data.group(1).contains("Город"))
                    profile.setCity(safe(data.group(2)));
            }

            data = contacts.matcher(mainMatcher.group(8));
            while (data.find())
                profile.addContact(Pair.create(safe(data.group(1)), safe(data.group(3) == null ? data.group(2) : data.group(3))));

            data = devices.matcher(mainMatcher.group(9));
            while (data.find())
                profile.addDevice(Pair.create(safe(data.group(1)), safe(data.group(2)) + safe(data.group(3))));

            data = siteStats.matcher(mainMatcher.group(10));
            while (data.find()) {
                Pair<String, String> pair = Pair.create(safe(data.group(2)), safe(data.group(3) == null ? data.group(4) : data.group(3)));
                if (data.group(1).contains("Карма"))
                    profile.setKarma(pair);

                if (data.group(1).contains("Постов"))
                    profile.setSitePosts(pair);

                if (data.group(1).contains("Комментов"))
                    profile.setComments(pair);
            }

            data = forumStats.matcher(mainMatcher.group(11));
            while (data.find()) {
                Pair<String, String> pair = Pair.create(safe(data.group(2)), safe(data.group(5)));
                if (data.group(1).contains("Репу"))
                    profile.setReputation(pair);

                if (data.group(1).contains("Тем"))
                    profile.setTopics(pair);

                if (data.group(1).contains("Постов"))
                    profile.setPosts(pair);
            }
            data = note.matcher(response);
            if (data.find()) {
                profile.setNote(Utils.fromHtml(data.group(1).replaceAll("\n", "<br></br>")));
            }

            data = about.matcher(response);
            if (data.find()) {
                profile.setAbout(forpdateam.ru.forpda.utils.ourparser.Html.fromHtml(safe(data.group(1))));
            }
        }
        return profile;
    }

    public boolean saveNote(String note) throws Exception {
        ForPdaRequest.Builder builder = new ForPdaRequest.Builder()
                .url("http://4pda.ru/forum/index.php?act=profile-xhr&action=save-note")
                .formHeader("note", note);
        String response = Api.getWebClient().request(builder.build());
        return response.equals("1");
    }

    private static String safe(String s) {
        return s == null ? null : s.trim();
    }
}
