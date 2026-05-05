package forpdateam.ru.forpda.entity.app.profile

import android.content.SharedPreferences
import com.jakewharton.rxrelay2.BehaviorRelay
import forpdateam.ru.forpda.common.Html
import forpdateam.ru.forpda.entity.EntityWrapper
import forpdateam.ru.forpda.entity.remote.others.user.ForumUser
import forpdateam.ru.forpda.entity.remote.profile.ProfileModel
import forpdateam.ru.forpda.extensions.nullString
import io.reactivex.Observable
import kotlinx.coroutines.flow.MutableStateFlow
import org.json.JSONArray
import org.json.JSONObject
import androidx.core.content.edit
import kotlinx.coroutines.flow.Flow

class UserHolder(
    private val sharedPreferences: SharedPreferences
) : IUserHolder {

    private val currentUserRelay = MutableStateFlow<ForumUser?>(user)

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
            currentUserRelay.value = value
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

    override fun observeCurrentUser(): Flow<ForumUser?> = currentUserRelay
}
