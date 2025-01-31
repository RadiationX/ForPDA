package forpdateam.ru.forpda.entity.app.profile

import android.content.SharedPreferences
import com.jakewharton.rxrelay2.BehaviorRelay
import forpdateam.ru.forpda.common.Html
import forpdateam.ru.forpda.entity.EntityWrapper
import forpdateam.ru.forpda.entity.remote.others.user.ForumUser
import forpdateam.ru.forpda.entity.remote.profile.ProfileModel
import forpdateam.ru.forpda.extensions.nullString
import io.reactivex.Observable
import org.json.JSONArray
import org.json.JSONObject

class UserHolder(
    private val sharedPreferences: SharedPreferences
) : IUserHolder {

    private val currentUserRelay = BehaviorRelay.createDefault(EntityWrapper(user))

    override var user: ForumUser?
        get() {
            return sharedPreferences
                .getString("current_user", null)
                ?.let {
                    val jsonProfile = JSONObject(it)
                    ForumUser(
                        id = jsonProfile.getInt("id"),
                        avatar = jsonProfile.nullString("avatar"),
                        nick = jsonProfile.nullString("nick")
                    )
                }
        }
        set(value) {
            currentUserRelay.accept(EntityWrapper(value))
            val result = value?.let { profile ->
                JSONObject().apply {
                    put("id", profile.id)
                    put("avatar", profile.avatar)
                    put("nick", profile.nick)
                }
            }
            if (result == null) {
                sharedPreferences.edit().remove("current_user").apply()
            } else {
                sharedPreferences.edit().putString("current_user", result.toString()).apply()
            }
        }

    override fun observeCurrentUser(): Observable<EntityWrapper<ForumUser?>> = currentUserRelay
}
