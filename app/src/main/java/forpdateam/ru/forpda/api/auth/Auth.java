package forpdateam.ru.forpda.api.auth;

import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.api.IWebClient;
import forpdateam.ru.forpda.api.NetworkRequest;
import forpdateam.ru.forpda.api.NetworkResponse;
import forpdateam.ru.forpda.api.Utils;
import forpdateam.ru.forpda.api.auth.models.AuthForm;
import forpdateam.ru.forpda.client.ClientHelper;

/**
 * Created by radiationx on 25.03.17.
 */

public class Auth {
    public final static String AUTH_BASE_URL = "https://4pda.ru/forum/index.php?act=auth";
    private final static Pattern captchaPattern = Pattern.compile("captcha-time\" value=\"([^\"]*?)\"[\\s\\S]*?captcha-sig\" value=\"([^\"]*?)\"[\\s\\S]*?src=\"([^\"]*?)\"");
    private final static Pattern errorPattern = Pattern.compile("errors-list\">([\\s\\S]*?)</ul>");

    public AuthForm getForm() throws Exception {
        NetworkResponse response = Api.getWebClient().get(AUTH_BASE_URL);

        if (response.getBody() == null || response.getBody().isEmpty())
            throw new Exception("Page empty!");

        if (checkLogin(response.getBody()))
            throw new Exception("You already logged");

        AuthForm form = new AuthForm();
        Matcher matcher = captchaPattern.matcher(response.getBody());
        if (matcher.find()) {
            form.setCaptchaTime(matcher.group(1));
            form.setCaptchaSig(matcher.group(2));
            form.setCaptchaImageUrl(matcher.group(3));
        } else {
            throw new Exception("Form Not Found");
        }
        form.setBody(response.getBody());
        return form;
    }

    public Boolean login(final AuthForm form) throws Exception {
        NetworkRequest.Builder builder = new NetworkRequest.Builder()
                .url(AUTH_BASE_URL)
                .formHeader("captcha-time", form.getCaptchaTime())
                .formHeader("captcha-sig", form.getCaptchaSig())
                .formHeader("captcha", form.getCaptcha())
                .formHeader("return", form.getReturnField())
                .formHeader("login", URLEncoder.encode(form.getNick(), "windows-1251"), true)
                .formHeader("password", URLEncoder.encode(form.getPassword(), "windows-1251"), true)
                .formHeader("remember", form.getRememberField())
                .formHeader("hidden", form.isHidden() ? "1" : "0");
        NetworkResponse response = Api.getWebClient().request(builder.build());
        Matcher matcher = errorPattern.matcher(response.getBody());
        if (matcher.find()) {
            throw new Exception(Utils.fromHtml(matcher.group(1)).replaceAll("\\.", ".\n").trim());
        }
        form.setBody(response.getBody());
        return checkLogin(response.getBody());
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
        NetworkResponse response =Api.getWebClient().get("https://4pda.ru/forum/index.php?act=logout&CODE=03&k=".concat(Api.getWebClient().getAuthKey()));

        Matcher matcher = Pattern.compile("wr va-m text").matcher(response.getBody());
        if (matcher.find())
            throw new Exception("You already logout");

        Api.getWebClient().clearCookies();
        App.getInstance().getPreferences().edit().remove("cookie_member_id").remove("cookie_pass_hash").apply();
        ClientHelper.setAuthState(ClientHelper.AUTH_STATE_LOGOUT);

        return !checkLogin(Api.getWebClient().get(IWebClient.MINIMAL_PAGE).getBody());
    }

}
