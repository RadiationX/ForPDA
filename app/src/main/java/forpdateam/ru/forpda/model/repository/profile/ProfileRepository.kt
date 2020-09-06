package forpdateam.ru.forpda.model.repository.profile

import com.jakewharton.rxrelay2.BehaviorRelay
import forpdateam.ru.forpda.entity.EntityWrapper
import forpdateam.ru.forpda.entity.app.profile.IUserHolder
import forpdateam.ru.forpda.entity.remote.others.user.ForumUser
import forpdateam.ru.forpda.entity.remote.profile.ProfileModel
import forpdateam.ru.forpda.model.AuthHolder
import forpdateam.ru.forpda.model.SchedulersProvider
import forpdateam.ru.forpda.model.data.cache.forumuser.ForumUsersCache
import forpdateam.ru.forpda.model.data.remote.api.profile.ProfileApi
import forpdateam.ru.forpda.model.repository.BaseRepository
import io.reactivex.Observable
import io.reactivex.Single

/**
 * Created by radiationx on 02.01.18.
 */

class ProfileRepository(
        private val schedulers: SchedulersProvider,
        private val profileApi: ProfileApi,
        private val userHolder: IUserHolder,
        private val authHolder: AuthHolder,
        private val forumUsersCache: ForumUsersCache
) : BaseRepository(schedulers) {

    fun observeCurrentUser(): Observable<EntityWrapper<ProfileModel?>> = userHolder
            .observeCurrentUser()
            .runInIoToUi()

    fun loadSelf() = loadProfile("https://4pda.ru/forum/index.php?showuser=" + authHolder.get().userId)

    fun loadProfile(url: String): Single<ProfileModel> = Single
            .fromCallable { profileApi.getProfile(url) }
            .doOnSuccess {
                if (it.id == authHolder.get().userId) {
                    userHolder.user = it
                }
                forumUsersCache.saveUser(ForumUser().apply {
                    id = it.id
                    nick = it.nick
                    avatar = it.avatar
                })
            }
            .runInIoToUi()

    fun saveNote(note: String): Single<Boolean> = Single
            .fromCallable { profileApi.saveNote(note) }
            .runInIoToUi()
}
