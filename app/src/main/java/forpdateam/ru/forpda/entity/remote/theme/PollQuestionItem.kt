package forpdateam.ru.forpda.entity.remote.theme

/**
 * Created by radiationx on 12.11.16.
 */

class PollQuestionItem {
    var title: String? = null

    //For no result poll
    var value: Int = 0
    var name: String? = null
    var type: String? = null

    //For result poll
    var votes: Int = 0
    var percent: Float = 0.toFloat()
}
