package forpdateam.ru.forpda.entity.remote.others.user

/**
 * Created by radiationx on 08.07.17.
 */

class ForumUser : IForumUser {
    override var id = 0
    override var nick: String? = null
    override var avatar: String? = null

    constructor() {}

    constructor(forumUser: IForumUser) {
        this.id = forumUser.id
        this.nick = forumUser.nick
        this.avatar = forumUser.avatar
    }
}
