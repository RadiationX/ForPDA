package forpdateam.ru.forpda.api.login;

import android.text.Html;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.client.Client;
import rx.Observable;
import rx.Subscriber;

/**
 * Created by radiationx on 29.07.16.
 */
public class Login {
    public final static String loginFormUrl = "http://4pda.ru/forum/index.php?act=auth";
    private final static Pattern captchaPattern = Pattern.compile("captcha-time\" value=\"([^\"]*?)\"[\\s\\S]*?captcha-sig\" value=\"([^\"]*?)\"[\\s\\S]*?src=\"([^\"]*?)\"");
    private final static Pattern errorPattern = Pattern.compile("errors-list\">([\\s\\S]*?)</ul>");

    private LoginForm doLoadForm() throws Throwable {
        String response = Client.getInstance().get(loginFormUrl);

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

    private Boolean doLogin(final LoginForm form) throws Throwable {
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

    private boolean checkLogin(String response) {
        boolean result = false;
        //System.out.print(response);
        Matcher matcher = Pattern.compile("<a[^>]*?act=login&CODE=03&k=([^&]*?)&").matcher(response);
        if (matcher.find()) {
            result = true;
            App.getInstance().getPreferences().edit().putString("logout_key", matcher.group(1)).apply();
        }
        return result;
    }

    public boolean tryLogout() throws Exception {
        String response = Client.getInstance().get("http://4pda.ru/forum/index.php?act=login&CODE=03&k=" + App.getInstance().getPreferences().getString("logout_key", "0"));

        Matcher matcher = Pattern.compile("wr va-m text").matcher(response);
        if (matcher.find())
            throw new Exception("You already logout");

        Client.logout();
        App.getInstance().getPreferences().edit().remove("cookie_member_id").remove("cookie_pass_hash").apply();

        return !checkLogin(Client.getInstance().get(Client.minimalPage));
    }

    public Observable<LoginForm> getForm() {
        return Observable.create(new Observable.OnSubscribe<LoginForm>() {
            @Override
            public void call(Subscriber<? super LoginForm> subscriber) {
                try {
                    subscriber.onNext(doLoadForm());
                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });


    }

    public Observable<Boolean> login(final LoginForm loginForm) {
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                try {
                    subscriber.onNext(doLogin(loginForm));
                    subscriber.onCompleted();
                } catch (Throwable e) {
                    subscriber.onError(e);
                }
            }
        });
    }
}
