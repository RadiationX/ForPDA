package forpdateam.ru.forpda.entity.remote.others.user

/**
 * Created by radiationx on 08.07.17.
 */
@Suppress("DataClassPrivateConstructor")
data class ForumPostUser private constructor(
    val id: Int,
    val nick: String,
    val avatar: String?
) {
    companion object {
        fun required(id: Int, nick: String?, avatar: String?): ForumPostUser {
            check(id > 0) { "Can't create user with $id" }
            checkNotNull(nick) { "Can't create user with $id, $nick" }
            return ForumPostUser(id, nick, avatar)
        }
    }
}