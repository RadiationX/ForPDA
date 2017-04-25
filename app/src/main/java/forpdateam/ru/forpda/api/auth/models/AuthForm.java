package forpdateam.ru.forpda.api.auth.models;

import forpdateam.ru.forpda.api.IWebClient;

/**
 * Created by radiationx on 29.07.16.
 */
public class AuthForm {
    private final static String rememberField = "1";
    private String captchaImageUrl;
    private String captcha;
    private String captchaTime;
    private String captchaSig;
    private String nick;
    private String password;
    private String body;
    private boolean hidden = false;

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

    public void setNick(String nick) {
        this.nick = nick;
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

    public String getNick() {
        return nick;
    }

    public String getPassword() {
        return password;
    }

    public String getReturnField() {
        return IWebClient.MINIMAL_PAGE;
    }

    public String getRememberField() {
        return rememberField;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }
}
