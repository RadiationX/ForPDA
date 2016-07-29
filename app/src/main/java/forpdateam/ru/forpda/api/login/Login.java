package forpdateam.ru.forpda.api.login;

import android.text.Html;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import forpdateam.ru.forpda.client.Client;

/**
 * Created by radiationx on 29.07.16.
 */
public class Login {
    private final static String loginFormUrl = "http://4pda.ru/forum/index.php?act=auth";
    private final static Pattern captchaPattern = Pattern.compile("captcha-time\" value=\"([^\"]*?)\"[\\s\\S]*?captcha-sig\" value=\"([^\"]*?)\"[\\s\\S]*?src=\"([^\"]*?)\"");
    private final static Pattern errorPattern = Pattern.compile("errors-list\">([\\s\\S]*?)</ul>");

    public LoginForm loadForm() throws Exception {
        String response = Client.getInstance().get(loginFormUrl);
        Log.d("kek", "load form response " + response);

        if (response == null || response.isEmpty())
            throw new Exception("Page Empty!");

        if (checkLogin(response))
            throw new Exception("You Already Logged");

        LoginForm form = new LoginForm();
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

    public Boolean tryLogin(LoginForm form) throws Exception {
        Map<String, String> headers = new HashMap<>();
        headers.put("captcha-time", form.getCaptchaTime());
        headers.put("captcha-sig", form.getCaptchaSig());
        headers.put("captcha", form.getCaptcha());
        headers.put("return", form.getReturnField());
        headers.put("login", form.getLogin());
        headers.put("password", form.getPassword());
        headers.put("remember", form.getRememberField());
        String response = Client.getInstance().post("http://4pda.ru/forum/index.php?act=auth", headers);
        Matcher matcher = errorPattern.matcher(response);
        if (matcher.find())
            throw new Exception(Html.fromHtml(matcher.group(1)).toString().replaceAll("\\.", ".\n").trim());

        form.setBody(response);

        return checkLogin(response);
    }

    public boolean checkLogin(String response) {
        boolean result = false;
        Matcher matcher = Pattern.compile("<a[^>]*?act=login\\&CODE=03\\&k=([^&]*?)&").matcher(response);
        if (matcher.find()) {
            result = true;
            Log.d("kek", "logout key " + matcher.group(1));
        }
        return result;
    }

    public void tryLogout() {

    }
}
