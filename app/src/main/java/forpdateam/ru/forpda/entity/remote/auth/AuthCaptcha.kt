package forpdateam.ru.forpda.entity.remote.auth

data class AuthCaptcha(
    val captchaImageUrl: String?,
    val captchaTime: String?,
    val captchaSig: String?,
)