package forpdateam.ru.forpda.model.repository.profile

import forpdateam.ru.forpda.entity.remote.others.user.ForumUser
import forpdateam.ru.forpda.entity.remote.profile.ProfileModel
import forpdateam.ru.forpda.model.AuthHolder
import forpdateam.ru.forpda.model.data.cache.forumuser.ForumUsersCache
import forpdateam.ru.forpda.model.data.remote.api.profile.ProfileApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

/**
 * Created by radiationx on 02.01.18.
 */

class ProfileRepository @Inject constructor(
    private val profileApi: ProfileApi,
    private val authHolder: AuthHolder,
    private val forumUsersCache: ForumUsersCache
) {

    fun observeCurrentUser(): Flow<ForumUser?> {
        return authHolder.observe().flatMapLatest {
            if (it.isAuth()) {
                forumUsersCache.observeUserById(it.userId)
            } else {
                flowOf(null)
            }
        }
    }

    suspend fun getCurrentUser(): ForumUser? {
        if (!authHolder.get().isAuth()) {
            return null
        }
        val cachedUser = forumUsersCache.getUserById(authHolder.get().userId)
        if (cachedUser != null) {
            return cachedUser
        }
        return loadSelf().user
    }

    suspend fun loadSelf(): ProfileModel {
        return loadProfile("https://4pda.to/forum/index.php?showuser=" + authHolder.get().userId)
    }

    suspend fun loadProfile(url: String): ProfileModel {
        return profileApi.getProfile(url).also {
            forumUsersCache.saveUser(it.user)
        }
    }

    suspend fun saveNote(note: String): Boolean {
        return profileApi.saveNote(note)
    }
}
