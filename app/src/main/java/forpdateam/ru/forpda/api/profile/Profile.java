package forpdateam.ru.forpda.api.profile;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.api.NetworkRequest;
import forpdateam.ru.forpda.api.NetworkResponse;
import forpdateam.ru.forpda.api.Utils;
import forpdateam.ru.forpda.api.profile.models.ProfileModel;

/**
 * Created by radiationx on 03.08.16.
 */
public class Profile {
    private static final Pattern mainPattern = Pattern.compile("<div[^>]*?user-box[\\s\\S]*?<img src=\"([^\"]*?)\"[\\s\\S]*?<h1>([^<]*?)<\\/h1>[\\s\\S]*?(?=<span class=\"title\">([^<]*?)<\\/span>| )[\\s\\S]*?<h2>(?:<span style[^>]*?>|)([^\"<]*?)(?:<\\/span>|)<\\/h2>[\\s\\S]*?(<ul[\\s\\S]*?\\/ul>)[\\s\\S]*?<div class=\"u-note\">([\\s\\S]*?)<\\/div>[^<]*?(?:<\\/li>|<div)[\\s\\S]*?(<ul[\\s\\S]*?\\/ul>)[\\s\\S]*?(<ul[\\s\\S]*?\\/ul>)[\\s\\S]*?(<ul[\\s\\S]*?\\/ul>)[\\s\\S]*?(<ul[\\s\\S]*?\\/ul>)[\\s\\S]*?(<ul[\\s\\S]*?\\/ul>)");
    private static final Pattern info = Pattern.compile("<li[\\s\\S]*?title[^>]*?>([^>]*?)<[\\s\\S]*?div[^>]*>([\\s\\S]*?)</div>");
    private static final Pattern personal = Pattern.compile("<li[\\s\\S]*?title[^>]*?>([^>]*?)<[\\s\\S]*?(?=<div[^>]*>([^<]*)[\\s\\S]*?</div>|)<");
    private static final Pattern contacts = Pattern.compile("<a[^>]*?href=\"([^\"]*?)\"[^>]*?>(?:<strong>)?([\\s\\S]*?)(?:<\\/strong>)?<\\/a>");
    private static final Pattern devices = Pattern.compile("<a[^>]*?href=\"([^\"]*?)\"[^>]*?>([\\s\\S]*?)</a>([\\s\\S]*?)</li>");
    private static final Pattern siteStats = Pattern.compile("<span class=\"title\">([^<]*?)<\\/span>[\\s\\S]*?<div class=\"area\">[\\s\\S]*?(?:<a[^>]*?href=\"([^\"]*?)\"[^>]*?>)?([\\s\\S]*?)(?:<\\/a>)?<\\/div>");
    private static final Pattern forumStats = Pattern.compile("<span class=\"title\">([^<]*?)<\\/span>[\\s\\S]*?<div class=\"area\">[\\s\\S]*?<a[^>]*?href=\"([^\"]*?)\"[^>]*?>[^<]*?(?:<span[^>]*?>)?([^<]*?)(?:<\\/span>)?<\\/a>");
    private static final Pattern note = Pattern.compile("<textarea[^>]*?profile-textarea\"[^>]*?>([\\s\\S]*?)</textarea>");
    private static final Pattern about = Pattern.compile("<div[^>]*?div-custom-about[^>]*?>([\\s\\S]*?)</div>");

    public ProfileModel getProfile(String url) throws Exception {
        ProfileModel profile = new ProfileModel();
        NetworkResponse response = Api.getWebClient().get(url);


        final Matcher mainMatcher = mainPattern.matcher(response.getBody());
        if (mainMatcher.find()) {

            profile.setAvatar(safe(mainMatcher.group(1)));
            profile.setNick(Utils.fromHtml(safe(mainMatcher.group(2))));
            profile.setStatus(safe(mainMatcher.group(3)));
            profile.setGroup(safe(mainMatcher.group(4)));

            Matcher data = info.matcher(mainMatcher.group(5));
            while (data.find()) {
                String field = data.group(1);

                if (field.contains("Рег")) {
                    profile.addInfo(ProfileModel.InfoType.REG_DATE,
                            safe(Utils.fromHtml(data.group(2))));

                } else if (field.contains("Последнее")) {
                    profile.addInfo(ProfileModel.InfoType.ONLINE_DATE,
                            safe(Utils.fromHtml(data.group(2).trim())));
                }
            }

            String signString = safe(mainMatcher.group(6));
            profile.setSign(signString.equals("Нет подписи") ? null : Utils.coloredFromHtml(signString));

            data = personal.matcher(mainMatcher.group(7));
            while (data.find()) {
                String field = data.group(2);
                if (field == null || field.isEmpty()) {
                    profile.addInfo(ProfileModel.InfoType.GENDER,
                            safe(data.group(1)));
                    continue;
                }

                field = data.group(1);
                if (field.contains("Дата")) {
                    profile.addInfo(ProfileModel.InfoType.BIRTHDAY,
                            safe(data.group(2)));

                } else if (field.contains("Время")) {
                    profile.addInfo(ProfileModel.InfoType.USER_TIME,
                            safe(data.group(2)));

                } else if (field.contains("Город")) {
                    profile.addInfo(ProfileModel.InfoType.CITY,
                            safe(data.group(2)));
                }
            }

            data = contacts.matcher(mainMatcher.group(8));
            while (data.find()) {
                ProfileModel.Contact contact = new ProfileModel.Contact();
                contact.setUrl(safe(data.group(1)));
                String title = safe(data.group(2));
                contact.setTitle(title);
                ProfileModel.ContactType type;
                switch (title) {
                    case "QMS":
                        type = ProfileModel.ContactType.QMS;
                        break;
                    case "Вебсайт":
                        type = ProfileModel.ContactType.WEBSITE;
                        break;
                    case "ICQ":
                        type = ProfileModel.ContactType.ICQ;
                        break;
                    case "Twitter":
                        type = ProfileModel.ContactType.TWITTER;
                        break;
                    case "Вконтакте":
                        type = ProfileModel.ContactType.VKONTAKTE;
                        break;
                    case "Google+":
                        type = ProfileModel.ContactType.GOOGLE_PLUS;
                        break;
                    case "Facebook":
                        type = ProfileModel.ContactType.FACEBOOK;
                        break;
                    case "Instagram":
                        type = ProfileModel.ContactType.INSTAGRAM;
                        break;
                    case "Jabber":
                        type = ProfileModel.ContactType.JABBER;
                        break;
                    case "Telegram":
                        type = ProfileModel.ContactType.TELEGRAM;
                        break;
                    case "Mail.ru":
                        type = ProfileModel.ContactType.MAIL_RU;
                        break;
                    case "Windows Live":
                        type = ProfileModel.ContactType.WINDOWS_LIVE;
                        break;
                    default:
                        type = ProfileModel.ContactType.WEBSITE;
                }
                contact.setType(type);
                profile.addContact(contact);
            }

            data = devices.matcher(mainMatcher.group(9));
            while (data.find()) {
                ProfileModel.Device device = new ProfileModel.Device();
                device.setUrl(safe(data.group(1)));
                device.setName(safe(data.group(2)));
                device.setAccessory(safe(data.group(3)));
                profile.addDevice(device);
            }

            data = siteStats.matcher(mainMatcher.group(10));
            while (data.find()) {
                ProfileModel.Stat stat = new ProfileModel.Stat();
                stat.setUrl(data.group(2));
                stat.setValue(data.group(3));

                String field = data.group(1);
                ProfileModel.StatType type = null;
                if (field.contains("Карма")) {
                    type = ProfileModel.StatType.SITE_KARMA;
                } else if (field.contains("Постов")) {
                    type = ProfileModel.StatType.SITE_POSTS;
                } else if (field.contains("Комментов")) {
                    type = ProfileModel.StatType.SITE_COMMENTS;
                }
                stat.setType(type);
                profile.addStat(stat);
            }

            data = forumStats.matcher(mainMatcher.group(11));
            while (data.find()) {
                ProfileModel.Stat stat = new ProfileModel.Stat();
                stat.setUrl(data.group(2));
                stat.setValue(data.group(3));
                String field = data.group(1);

                ProfileModel.StatType type = null;
                if (field.contains("Репу")) {
                    type = ProfileModel.StatType.FORUM_REPUTATION;
                } else if (field.contains("Тем")) {
                    type = ProfileModel.StatType.FORUM_TOPICS;
                } else if (field.contains("Постов")) {
                    type = ProfileModel.StatType.FORUM_POSTS;
                }

                stat.setType(type);
                profile.addStat(stat);
            }
            data = note.matcher(response.getBody());
            if (data.find()) {
                profile.setNote(Utils.fromHtml(data.group(1).replaceAll("\n", "<br></br>")));
            }

            data = about.matcher(response.getBody());
            if (data.find()) {
                profile.setAbout(Utils.coloredFromHtml(safe(data.group(1))));
            }
        }
        return profile;
    }

    public boolean saveNote(String note) throws Exception {
        NetworkRequest.Builder builder = new NetworkRequest.Builder()
                .url("https://4pda.ru/forum/index.php?act=profile-xhr&action=save-note")
                .formHeader("note", note);
        NetworkResponse response = Api.getWebClient().request(builder.build());
        return response.getBody().equals("1");
    }

    private static String safe(String s) {
        return s == null ? null : s.trim();
    }
}
