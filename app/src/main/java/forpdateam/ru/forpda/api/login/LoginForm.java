package forpdateam.ru.forpda.api.login;

/**
 * Created by radiationx on 29.07.16.
 */
public class LoginForm {
    private final static String returnField = "http://4pda.ru/forum/index.php?showforum=200";
    private final static String rememberField = "1";
    private String captchaImageUrl;
    private String captcha;
    private String captchaTime;
    private String captchaSig;
    private String login;
    private String password;
    private String body;

    public void setCaptchaImageUrl(String captchaImageUrl) {
        this.captchaImageUrl = captchaImageUrl;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setCaptcha(String captcha) {
        this.captcha = captcha;
    }

    public void setCaptchaTime(String captchaTime) {
        this.captchaTime = captchaTime;
    }

    public void setCaptchaSig(String captchaSig) {
        this.captchaSig = captchaSig;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getBody() {
        return body;
    }

    public String getCaptchaImageUrl() {
        return captchaImageUrl;
    }

    public String getCaptcha() {
        return captcha;
    }

    public String getCaptchaTime() {
        return captchaTime;
    }

    public String getCaptchaSig() {
        return captchaSig;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public String getReturnField() {
        return returnField;
    }

    public String getRememberField() {
        return rememberField;
    }
}
