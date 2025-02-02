package forpdateam.ru.forpda.entity.remote.others.user

@Suppress("DataClassPrivateConstructor")
data class User private constructor(
    val id: Int,
    val nick: String
) {
    companion object {
        fun optional(id: Int, nick: String?): User? {
            if (id <= 0 || nick == null) return null
            return User(id, nick)
        }

        fun required(id: Int, nick: String?): User {
            check(id > 0) { "Can't create user with $id" }
            checkNotNull(nick) { "Can't create user with $id, $nick" }
            return User(id, nick)
        }
    }
}
