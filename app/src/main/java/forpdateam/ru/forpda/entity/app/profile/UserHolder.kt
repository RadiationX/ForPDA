package forpdateam.ru.forpda.entity.app.profile

import android.content.SharedPreferences
import androidx.core.content.edit
import forpdateam.ru.forpda.entity.remote.others.user.ForumUser
import forpdateam.ru.forpda.extensions.nullString
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.json.JSONObject

class UserHolder(
    private val sharedPreferences: SharedPreferences
) : IUserHolder {

    private val currentUserState = MutableStateFlow<ForumUser?>(user)

    override var user: ForumUser?
        get() {
            return sharedPreferences
                .getString("current_user", null)
                ?.let {
                    val jsonProfile = JSONObject(it)
                    ForumUser.required(
                        id = jsonProfile.getInt("id"),
                        nick = jsonProfile.nullString("nick"),
                        avatar = jsonProfile.nullString("avatar")
                    )
                }
        }
        set(value) {
            currentUserState.value = value
            val result = value?.let { profile ->
                JSONObject().apply {
                    put("id", profile.id)
                    put("nick", profile.nick)
                    put("avatar", profile.avatar)
                }
            }
            sharedPreferences.edit {
                if (result == null) {
                    remove("current_user")
                } else {
                    putString("current_user", result.toString())
                }
            }
        }

    override fun observeCurrentUser(): Flow<ForumUser?> = currentUserState
}
