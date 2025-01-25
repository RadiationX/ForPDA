package forpdateam.ru.forpda.entity.db

import forpdateam.ru.forpda.entity.remote.others.user.IForumUser
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

/**
 * Created by radiationx on 08.07.17.
 */
class ForumUserBd : RealmObject, IForumUser {
    @PrimaryKey
    override var id: Int = 0
    override var nick: String? = ""
    override var avatar: String? = ""

    constructor()

    constructor(forumUser: IForumUser) {
        this.id = forumUser.id
        this.nick = forumUser.nick
        this.avatar = forumUser.avatar
    }
}
