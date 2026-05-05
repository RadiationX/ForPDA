package forpdateam.ru.forpda.model.repository.profile

import forpdateam.ru.forpda.entity.app.profile.IUserHolder
import forpdateam.ru.forpda.entity.remote.others.user.ForumUser
import forpdateam.ru.forpda.entity.remote.profile.ProfileModel
import forpdateam.ru.forpda.model.AuthHolder
import forpdateam.ru.forpda.model.data.cache.forumuser.ForumUsersCache
import forpdateam.ru.forpda.model.data.remote.api.profile.ProfileApi
import kotlinx.coroutines.flow.Flow

/**
 * Created by radiationx on 02.01.18.
 */

class ProfileRepository(
    private val profileApi: ProfileApi,
    private val userHolder: IUserHolder,
    private val authHolder: AuthHolder,
    private val forumUsersCache: ForumUsersCache
) {

    fun observeCurrentUser(): Flow<ForumUser?> {
        return userHolder.observeCurrentUser()
    }

    suspend fun loadSelf(): ProfileModel {
        return loadProfile("https://4pda.to/forum/index.php?showuser=" + authHolder.get().userId)
    }

    suspend fun loadProfile(url: String): ProfileModel {
        return profileApi.getProfile(url).also {
            if (it.user.id == authHolder.get().userId) {
                userHolder.user = it.user
            }
            forumUsersCache.saveUser(it.user)
        }
    }

    suspend fun saveNote(note: String): Boolean {
        return profileApi.saveNote(note)
    }
}
