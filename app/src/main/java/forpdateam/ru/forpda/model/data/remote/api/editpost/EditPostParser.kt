package forpdateam.ru.forpda.model.data.remote.api.editpost

import forpdateam.ru.forpda.entity.remote.editpost.EditPost
import forpdateam.ru.forpda.extensions.map
import forpdateam.ru.forpda.extensions.mapOnce
import forpdateam.ru.forpda.extensions.requireOnce
import forpdateam.ru.forpda.model.data.remote.ParserPatterns
import forpdateam.ru.forpda.model.data.remote.api.ApiUtils
import forpdateam.ru.forpda.model.data.remote.parser.BaseParser
import forpdateam.ru.forpda.model.data.storage.IPatternProvider
import java.util.regex.Pattern

class EditPostParser(
    private val patternProvider: IPatternProvider
) : BaseParser() {

    private val scope = ParserPatterns.EditPost

    fun parseForm(response: String): EditPost.Form {
        return patternProvider
            .getPattern(scope.scope, scope.form)
            .matcher(response)
            .requireOnce {
                EditPost.Form(
                    message = ApiUtils.escapeNewLine(it.group(1)).fromHtml().orEmpty(),
                    editReason = it.group(2)
                )
            }
    }

    fun parsePoll(response: String): EditPost.Poll? = patternProvider
        .getPattern(scope.scope, scope.poll_info)
        .matcher(response)
        .mapOnce { matcher ->
            val jsonPattern =
                patternProvider.getPattern(scope.scope, scope.poll_fucking_invalid_json)
            val tmpQuestions = parseTmpQuestions(jsonPattern, matcher.group(2))
            val tmpChoices = parseTmpChoices(jsonPattern, matcher.group(3))
            val tmpChoicesVotes = parseTmpChoicesVotes(jsonPattern, matcher.group(4))
            val tmpQuestionsMulti = parseTmpQuestionsMulti(jsonPattern, matcher.group(5))

            val questions = tmpQuestions.map { question ->
                val choice = tmpChoices.map { choice ->
                    val votes = tmpChoicesVotes.find { it.id == choice.id }?.votes ?: 0
                    EditPost.Poll.Choice(
                        id = choice.id,
                        title = choice.title,
                        votes = votes
                    )
                }
                val isMulti = tmpQuestionsMulti.find { it.id == question.id }?.isMulti ?: false
                EditPost.Poll.Question(
                    id = question.id,
                    title = question.title,
                    choices = choice,
                    isMulti = isMulti
                )
            }

            EditPost.Poll(
                title = requireNotNull(matcher.group(8).fromHtml()),
                maxQuestions = matcher.group(6).toInt(),
                maxChoices = matcher.group(7).toInt(),
                questions = questions
            )
        }

    private fun parseTmpQuestions(
        jsonPattern: Pattern,
        input: String
    ): List<TmpQuestion> =
        jsonPattern.matcher(input).map { jsonMatcher ->
            TmpQuestion(
                id = EditPost.Poll.QuestionId(jsonMatcher.group(1).toInt()),
                title = requireNotNull(jsonMatcher.group(3).fromHtml())
            )
        }

    private fun parseTmpQuestionsMulti(
        jsonPattern: Pattern,
        input: String
    ): List<TmpQuestionMulti> = jsonPattern.matcher(input).map { jsonMatcher ->
        TmpQuestionMulti(
            id = EditPost.Poll.QuestionId(jsonMatcher.group(1).toInt()),
            isMulti = jsonMatcher.group(3) == "1"
        )
    }

    private fun parseTmpChoices(
        jsonPattern: Pattern,
        input: String
    ): List<TmpChoice> = jsonPattern.matcher(input).map { jsonMatcher ->
        TmpChoice(
            id = EditPost.Poll.ChoiceId(
                questionId = EditPost.Poll.QuestionId(jsonMatcher.group(1).toInt()),
                index = jsonMatcher.group(2).toInt()
            ),
            title = requireNotNull(jsonMatcher.group(3).fromHtml())
        )
    }

    private fun parseTmpChoicesVotes(
        jsonPattern: Pattern,
        input: String
    ): List<TmpChoiceVotes> = jsonPattern.matcher(input).map { jsonMatcher ->
        TmpChoiceVotes(
            id = EditPost.Poll.ChoiceId(
                questionId = EditPost.Poll.QuestionId(jsonMatcher.group(1).toInt()),
                index = jsonMatcher.group(2).toInt()
            ),
            votes = jsonMatcher.group(3).toInt()
        )
    }

    private data class TmpQuestion(
        val id: EditPost.Poll.QuestionId,
        val title: String,
    )

    private data class TmpQuestionMulti(
        val id: EditPost.Poll.QuestionId,
        val isMulti: Boolean
    )

    private data class TmpChoice(
        val id: EditPost.Poll.ChoiceId,
        val title: String
    )

    private data class TmpChoiceVotes(
        val id: EditPost.Poll.ChoiceId,
        val votes: Int
    )
}
