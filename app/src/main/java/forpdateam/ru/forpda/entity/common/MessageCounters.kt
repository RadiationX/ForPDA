package forpdateam.ru.forpda.entity.common

class MessageCounters {
    var qms: Int = 0
    var favorites: Int = 0
    var mentions: Int = 0

    fun getAll() = qms + favorites + mentions
}