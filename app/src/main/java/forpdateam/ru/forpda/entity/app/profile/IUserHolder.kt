package forpdateam.ru.forpda.entity.app.profile

import forpdateam.ru.forpda.entity.EntityWrapper
import forpdateam.ru.forpda.entity.remote.others.user.ForumUser
import io.reactivex.Observable

interface IUserHolder {
    var user: ForumUser?

    fun observeCurrentUser(): Observable<EntityWrapper<ForumUser?>>
}
