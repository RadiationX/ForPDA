package forpdateam.ru.forpda.api.auth;

import android.text.Html;
import android.util.Log;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.api.auth.models.AuthForm;
import forpdateam.ru.forpda.client.Client;
import forpdateam.ru.forpda.client.ClientHelper;

/**
 * Created by radiationx on 25.03.17.
 */

public class Auth {
    public final static String authFormUrl = "http://4pda.ru/forum/index.php?act=auth";
    private final static Pattern captchaPattern = Pattern.compile("captcha-time\" value=\"([^\"]*?)\"[\\s\\S]*?captcha-sig\" value=\"([^\"]*?)\"[\\s\\S]*?src=\"([^\"]*?)\"");
    private final static Pattern errorPattern = Pattern.compile("errors-list\">([\\s\\S]*?)</ul>");

    public AuthForm getForm() throws Exception {
        String response = Client.getInstance().get(authFormUrl);

        if (response == null || response.isEmpty())
            throw new Exception("Page Empty!");

        if (checkLogin(response))
            throw new Exception("You Already Logged");

        AuthForm form = new AuthForm();
        Matcher matcher = captchaPattern.matcher(response);
        if (matcher.find()) {
            form.setCaptchaTime(matcher.group(1));
            form.setCaptchaSig(matcher.group(2));
            form.setCaptchaImageUrl(matcher.group(3));
        } else {
            throw new Exception("Form Not Found");
        }
        form.setBody(response);
        return form;
    }

    public Boolean login(final AuthForm form) throws Exception {
        Map<String, String> headers = new HashMap<>();
        headers.put("captcha-time", form.getCaptchaTime());
        headers.put("captcha-sig", form.getCaptchaSig());
        headers.put("captcha", form.getCaptcha());
        headers.put("return", form.getReturnField());
        headers.put("logout", URLEncoder.encode(form.getNick(), "windows-1251"));
        headers.put("password", URLEncoder.encode(form.getPassword(), "windows-1251"));
        headers.put("remember", form.getRememberField());
        String response = Client.getInstance().post("https://4pda.ru/forum/index.php?act=auth", headers, true);
        Matcher matcher = errorPattern.matcher(response);
        if (matcher.find()) {
            throw new Exception(Html.fromHtml(matcher.group(1)).toString().replaceAll("\\.", ".\n").trim());
        }

        Log.d("FORPDA_LOG", "RESPONSE " + response);
        form.setBody(response);

        return checkLogin(response);
    }

    private boolean checkLogin(String response) {
        Matcher matcher = Pattern.compile("<i class=\"icon-profile\">[\\s\\S]*?<ul class=\"dropdown-menu\">[\\s\\S]*?showuser=(\\d+)\"[\\s\\S]*?action=logout[^\"]*?k=([a-z0-9]{32})").matcher(response);
        if (matcher.find()) {
            App.getInstance().getPreferences().edit().putString("auth_key", matcher.group(2)).apply();
            return true;
        }
        return false;
    }

    public boolean logout() throws Exception {
        String response = Client.getInstance().get("http://4pda.ru/forum/index.php?act=logout&CODE=03&k=".concat(Client.getAuthKey()));

        Matcher matcher = Pattern.compile("wr va-m text").matcher(response);
        if (matcher.find())
            throw new Exception("You already logout");

        Client.clearCookies();
        App.getInstance().getPreferences().edit().remove("cookie_member_id").remove("cookie_pass_hash").apply();
        ClientHelper.setAuthState(ClientHelper.AUTH_STATE_LOGOUT);

        return !checkLogin(Client.getInstance().get(Client.minimalPage));
    }

}
