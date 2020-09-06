package forpdateam.ru.forpda.entity.remote.theme

import java.util.ArrayList

/**
 * Created by radiationx on 12.11.16.
 */

class Poll {
    var title: String? = null
    var votesCount: Int = 0
    //true - result poll
    var isResult: Boolean = false
    var voteButton = false
    var showResultsButton = false
    var showPollButton = false
    val questions = mutableListOf<PollQuestion>()

    fun haveButtons(): Boolean {
        return voteButton or showResultsButton or showPollButton
    }
}
