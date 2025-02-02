package forpdateam.ru.forpda.entity.common

data class AuthData(
    val userId: Int,
    val state: AuthState
) {

    companion object {
        const val NO_ID = 0
    }

    fun isAuth() = state == AuthState.AUTH
}