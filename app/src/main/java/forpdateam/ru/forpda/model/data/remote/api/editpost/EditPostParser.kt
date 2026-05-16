package forpdateam.ru.forpda.model.data.remote.api.editpost

import forpdateam.ru.forpda.entity.remote.editpost.EditPost
import forpdateam.ru.forpda.model.data.remote.ParserPatterns
import forpdateam.ru.forpda.model.data.remote.api.ApiUtils
import forpdateam.ru.forpda.model.data.remote.parser.BaseParser
import forpdateam.ru.forpda.model.data.storage.PatternProvider
import ru.radiationx.regexparser.RegexParser

class EditPostParser(
    private val patternProvider: PatternProvider
) : BaseParser() {

    private val scope = ParserPatterns.EditPost

    fun parseForm(response: String): EditPost.Form {
        return patternProvider
            .getRegexParser(scope.scope, scope.form)
            .requireOnce(response) {
                EditPost.Form(
                    message = ApiUtils.escapeNewLine(it.require(1)).fromHtml(),
                    editReason = it.get(2)
                )
            }
    }

    fun parsePoll(response: String): EditPost.Poll? = patternProvider
        .getRegexParser(scope.scope, scope.poll_info)
        .mapOnce(response) { matcher ->
            val jsonParser =
                patternProvider.getRegexParser(scope.scope, scope.poll_fucking_invalid_json)
            val tmpQuestions = parseTmpQuestions(jsonParser, matcher.require(2))
            val tmpChoices = parseTmpChoices(jsonParser, matcher.require(3))
            val tmpChoicesVotes = parseTmpChoicesVotes(jsonParser, matcher.require(4))
            val tmpQuestionsMulti = parseTmpQuestionsMulti(jsonParser, matcher.require(5))

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
                title = matcher.require(8).fromHtml(),
                maxQuestions = matcher.require(6).toInt(),
                maxChoices = matcher.require(7).toInt(),
                questions = questions
            )
        }

    private fun parseTmpQuestions(
        regexParser: RegexParser,
        input: String
    ): List<TmpQuestion> = regexParser.map(input) {
        TmpQuestion(
            id = EditPost.Poll.QuestionId(it.require(1).toInt()),
            title = it.require(3).fromHtml()
        )
    }

    private fun parseTmpQuestionsMulti(
        regexParser: RegexParser,
        input: String
    ): List<TmpQuestionMulti> = regexParser.map(input) {
        TmpQuestionMulti(
            id = EditPost.Poll.QuestionId(it.require(1).toInt()),
            isMulti = it.require(3) == "1"
        )
    }

    private fun parseTmpChoices(
        regexParser: RegexParser,
        input: String
    ): List<TmpChoice> = regexParser.map(input) {
        TmpChoice(
            id = EditPost.Poll.ChoiceId(
                questionId = EditPost.Poll.QuestionId(it.require(1).toInt()),
                index = it.require(2).toInt()
            ),
            title = it.require(3).fromHtml()
        )
    }

    private fun parseTmpChoicesVotes(
        regexParser: RegexParser,
        input: String
    ): List<TmpChoiceVotes> = regexParser.map(input) { jsonMatcher ->
        TmpChoiceVotes(
            id = EditPost.Poll.ChoiceId(
                questionId = EditPost.Poll.QuestionId(jsonMatcher.require(1).toInt()),
                index = jsonMatcher.require(2).toInt()
            ),
            votes = jsonMatcher.require(3).toInt()
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
