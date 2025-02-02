package forpdateam.ru.forpda.entity.remote.others.user

/**
 * Created by radiationx on 08.07.17.
 */
@Suppress("DataClassPrivateConstructor")
data class ForumUser private constructor(
    val id: Int,
    val nick: String,
    val avatar: String?
) {
    companion object {
        fun optional(id: Int, nick: String?, avatar: String?): ForumUser? {
            if (id <= 0 || nick == null) return null
            return ForumUser(id, nick, avatar)
        }

        fun required(id: Int, nick: String?, avatar: String?): ForumUser {
            check(id > 0) { "Can't create user with $id" }
            checkNotNull(nick) { "Can't create user with $id, $nick" }
            return ForumUser(id, nick, avatar)
        }
    }
}