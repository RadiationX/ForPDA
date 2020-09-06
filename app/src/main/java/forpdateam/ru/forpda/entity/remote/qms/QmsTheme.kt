package forpdateam.ru.forpda.entity.remote.qms

/**
 * Created by radiationx on 03.08.16.
 */
class QmsTheme : IQmsTheme {
    override var id: Int = 0
    var userId: Int = 0
    override var countMessages: Int = 0
    override var countNew: Int = 0
    override var name: String? = null
    override var date: String? = null
    var nick: String? = null

    constructor() {}

    constructor(qmsTheme: IQmsTheme) {
        id = qmsTheme.id
        countMessages = qmsTheme.countMessages
        countNew = qmsTheme.countNew
        name = qmsTheme.name
        date = qmsTheme.date
    }
}
