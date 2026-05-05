package forpdateam.ru.forpda.model

import android.content.SharedPreferences
import androidx.core.content.edit
import forpdateam.ru.forpda.entity.common.AuthData
import forpdateam.ru.forpda.entity.common.AuthState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class AuthHolder(
    private val preferences: SharedPreferences
) {

    private val dataFlow = MutableStateFlow<AuthData>(load())

    fun observe(): Flow<AuthData> = dataFlow

    fun get(): AuthData = dataFlow.value

    fun set(value: AuthData) {
        preferences.edit {
            putString("member_id", value.userId.toString())
            putString("auth_state", value.state.toString())
        }
        dataFlow.value = value
    }

    private fun load(): AuthData {
        val userId = preferences.getString("member_id", null)?.toInt() ?: AuthData.NO_ID
        var state = enumValueOf<AuthState>(
            preferences.getString("auth_state", null) ?: AuthState.NO_AUTH.toString()
        )
        val cookieMemberId = preferences.getString("cookie_member_id", null)
        val cookiePassHash = preferences.getString("cookie_pass_hash", null)
        if (cookieMemberId != null && cookiePassHash != null) {
            state = AuthState.AUTH
        }
        return AuthData(
            userId = userId,
            state = state
        )
    }
}