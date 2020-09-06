package forpdateam.ru.forpda.entity.common

class AuthData {

    companion object {
        const val NO_ID = 0
    }

    var userId: Int = NO_ID
    var state = AuthState.NO_AUTH

    fun isAuth() = state == AuthState.AUTH
}