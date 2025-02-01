package forpdateam.ru.forpda.entity.remote.theme

/**
 * Created by radiationx on 12.11.16.
 */

sealed interface PollQuestionItem {

    data class Regular(
        val title: String,
        val value: Int,
        val name: String,
        val type: String,
    ) : PollQuestionItem

    data class Result(
        val title: String,
        val votes: Int,
        val percent: Float,
    ) : PollQuestionItem
}
