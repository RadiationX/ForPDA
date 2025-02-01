package forpdateam.ru.forpda.entity.remote.theme

/**
 * Created by radiationx on 12.11.16.
 */

data class PollQuestion(
    val title: String,
    val questionItems: List<PollQuestionItem>
)
