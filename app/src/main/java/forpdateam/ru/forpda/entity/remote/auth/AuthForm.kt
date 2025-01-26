package forpdateam.ru.forpda.entity.remote.auth

/**
 * Created by radiationx on 29.07.16.
 */
data class AuthForm(
    val captcha: String,
    val nick: String,
    val password: String,
    val isHidden: Boolean
) {
    fun isFilled(): Boolean {
        return nick.isNotEmpty() && password.isNotEmpty() && captcha.length == 4
    }
}
