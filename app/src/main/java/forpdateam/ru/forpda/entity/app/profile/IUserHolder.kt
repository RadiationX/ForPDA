package forpdateam.ru.forpda.entity.app.profile

import forpdateam.ru.forpda.entity.remote.others.user.ForumUser
import kotlinx.coroutines.flow.Flow

interface IUserHolder {
    var user: ForumUser?

    fun observeCurrentUser(): Flow<ForumUser?>
}
