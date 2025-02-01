package forpdateam.ru.forpda.entity.remote.theme

/**
 * Created by radiationx on 12.11.16.
 */

data class Poll(
    val title: String?,
    val votesCount: Int,
    val isResult: Boolean,
    val voteButton: Boolean,
    val showResultsButton: Boolean,
    val showPollButton: Boolean,
    val questions: List<PollQuestion>
) {

    fun haveButtons(): Boolean {
        return voteButton or showResultsButton or showPollButton
    }
}
