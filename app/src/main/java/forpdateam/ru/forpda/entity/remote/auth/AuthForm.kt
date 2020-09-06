package forpdateam.ru.forpda.entity.remote.auth

import forpdateam.ru.forpda.model.data.remote.IWebClient

/**
 * Created by radiationx on 29.07.16.
 */
class AuthForm {
    var captchaImageUrl: String? = null
    var captcha: String? = null
    var captchaTime: String? = null
    var captchaSig: String? = null
    var nick: String? = null
    var password: String? = null
    var isHidden = false
}
