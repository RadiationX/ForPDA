package forpdateam.ru.forpda.entity.remote.editpost

import ru.radiationx.coretypes.AttachmentId
import ru.radiationx.coretypes.PostId

data class EditPost(
    val postId: PostId,
    val form: Form,
    val poll: Poll?,
    val attachments: List<Attachment>
) {

    data class Form(
        val message: String,
        val editReason: String?,
    )

    data class Poll(
        val title: String,
        val maxQuestions: Int,
        val maxChoices: Int,
        val questions: List<Question>
    ) {

        val baseIndexOffset: Int
            get() = questions.maxOfOrNull { it.id.index } ?: 0

        data class Question(
            val id: QuestionId,
            val title: String,
            val choices: List<Choice>,
            val isMulti: Boolean
        ) {
            val baseIndexOffset: Int
                get() = choices.maxOfOrNull { it.id.index } ?: 0
        }

        data class Choice(
            val id: ChoiceId,
            val title: String,
            val votes: Int
        )

        data class QuestionId(val index: Int)
        data class ChoiceId(val questionId: QuestionId, val index: Int)
    }

    class Attachment(
        val id: AttachmentId,
        val name: String,
        val extension: String,
        val size: Long,
        val sizeFormatted: String,
        val md5: String,
        val type: Type
    ) {

        sealed interface Type {

            data object File : Type

            data class Image(
                val url: String,
                val width: Int,
                val height: Int
            ) : Type
        }
    }

}
