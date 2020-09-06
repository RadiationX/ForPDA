package forpdateam.ru.forpda.client

class GoogleCaptchaException(val pageContent: String) : Exception("Google Captcha")