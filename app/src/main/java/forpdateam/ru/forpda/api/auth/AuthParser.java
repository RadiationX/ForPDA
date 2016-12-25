package forpdateam.ru.forpda.api.auth;

import android.text.Html;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.api.Api;
import forpdateam.ru.forpda.api.auth.models.AuthForm;
import forpdateam.ru.forpda.client.Client;
import io.reactivex.Observable;

/**
 * Created by radiationx on 29.07.16.
 */
public class AuthParser {
    public final static String authFormUrl = "http://4pda.ru/forum/index.php?act=auth";
    private final static Pattern captchaPattern = Pattern.compile("captcha-time\" value=\"([^\"]*?)\"[\\s\\S]*?captcha-sig\" value=\"([^\"]*?)\"[\\s\\S]*?src=\"([^\"]*?)\"");
    private final static Pattern errorPattern = Pattern.compile("errors-list\">([\\s\\S]*?)</ul>");

    private AuthForm doLoadForm() throws Exception {
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

    private Boolean doLogin(final AuthForm form) throws Exception {
        Map<String, String> headers = new HashMap<>();
        headers.put("captcha-time", form.getCaptchaTime());
        headers.put("captcha-sig", form.getCaptchaSig());
        headers.put("captcha", form.getCaptcha());
        headers.put("return", form.getReturnField());
        headers.put("login", form.getNick());
        headers.put("password", form.getPassword());
        headers.put("remember", form.getRememberField());
        String response = Client.getInstance().post("http://4pda.ru/forum/index.php?act=auth", headers);
        Matcher matcher = errorPattern.matcher(response);
        if (matcher.find())
            throw new Exception(Html.fromHtml(matcher.group(1)).toString().replaceAll("\\.", ".\n").trim());

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

    public boolean tryLogout() throws Exception {
        String response = Client.getInstance().get("http://4pda.ru/forum/index.php?act=login&CODE=03&k=" + App.getInstance().getPreferences().getString("auth_key", "0"));

        Matcher matcher = Pattern.compile("wr va-m text").matcher(response);
        if (matcher.find())
            throw new Exception("You already logout");

        Client.getCookies().clear();
        App.getInstance().getPreferences().edit().remove("cookie_member_id").remove("cookie_pass_hash").apply();
        Api.Auth().setState(false);

        return !checkLogin(Client.getInstance().get(Client.minimalPage));
    }

    public Observable<AuthForm> getForm() {
        return Observable.fromCallable(this::doLoadForm);
    }

    public Observable<Boolean> tryLogin(final AuthForm authForm) {
        return Observable.fromCallable(() -> doLogin(authForm));
    }
}
