package forpdateam.ru.forpda.entity.app.profile

import forpdateam.ru.forpda.entity.EntityWrapper
import forpdateam.ru.forpda.entity.remote.profile.ProfileModel
import io.reactivex.Observable

interface IUserHolder {
    var user: ProfileModel?

    fun observeCurrentUser(): Observable<EntityWrapper<ProfileModel?>>
}
