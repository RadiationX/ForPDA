package forpdateam.ru.forpda.model

import android.content.SharedPreferences
import com.jakewharton.rxrelay2.BehaviorRelay
import forpdateam.ru.forpda.entity.common.AuthData
import forpdateam.ru.forpda.entity.common.AuthState
import io.reactivex.Observable

class AuthHolder(
        private val preferences: SharedPreferences,
        private val schedulers: SchedulersProvider
) {
    private val relay = BehaviorRelay.create<AuthData>()

    init {
        set(AuthData().apply {
            userId = preferences.getString("member_id", null)?.toInt() ?: AuthData.NO_ID
            state = enumValueOf(preferences.getString("auth_state", null)
                    ?: AuthState.NO_AUTH.toString())

            val cookieMemberId = preferences.getString("cookie_member_id", null)
            val cookiePassHash = preferences.getString("cookie_pass_hash", null)
            if (cookieMemberId != null && cookiePassHash != null) {
                state = AuthState.AUTH
            }
        })
    }

    fun observe(): Observable<AuthData> = relay
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui());

    fun get(): AuthData = relay.value!!

    fun set(value: AuthData) {
        preferences
                .edit()
                .putString("member_id", value.userId.toString())
                .putString("auth_state", value.state.toString())
                .apply()
        relay.accept(value)
    }
}