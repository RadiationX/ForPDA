package forpdateam.ru.forpda.model.data.remote.api.editpost

import forpdateam.ru.forpda.entity.remote.editpost.AttachmentItem
import forpdateam.ru.forpda.entity.remote.editpost.EditPoll
import forpdateam.ru.forpda.entity.remote.editpost.EditPostForm
import forpdateam.ru.forpda.model.data.remote.ParserPatterns
import forpdateam.ru.forpda.model.data.remote.api.ApiUtils
import forpdateam.ru.forpda.model.data.remote.parser.BaseParser
import forpdateam.ru.forpda.model.data.storage.IPatternProvider
import java.text.DecimalFormat
import java.util.regex.Matcher

class EditPostParser(
        private val patternProvider: IPatternProvider
) : BaseParser() {

    private val scope = ParserPatterns.EditPost

    fun parseForm(response: String): EditPostForm = EditPostForm().also { form ->
        if (response == "nopermission") {
            form.errorCode = EditPostForm.ERROR_NO_PERMISSION
            return form
        }
        patternProvider
                .getPattern(scope.scope, scope.form)
                .matcher(response)
                .findOnce {
                    form.message = ApiUtils.escapeNewLine(it.group(1)).fromHtml().orEmpty()
                    form.editReason = it.group(2)
                }

        return form
    }

    fun parsePoll(response: String): EditPoll? = patternProvider
            .getPattern(scope.scope, scope.poll_info)
            .matcher(response)
            .mapOnce { matcher ->
                val poll = EditPoll()
                patternProvider
                        .getPattern(scope.scope, scope.poll_fucking_invalid_json)
                        .matcher(matcher.group(2))
                        .findAll { jsonMatcher ->
                            poll.addQuestion(EditPoll.Question().apply {
                                val questionIndex = jsonMatcher.group(1).toInt()
                                if (questionIndex > poll.baseIndexOffset) {
                                    poll.baseIndexOffset = questionIndex
                                }
                                index = questionIndex
                                title = jsonMatcher.group(3).fromHtml()
                            })
                        }
                        .reset(matcher.group(3)).findAll { jsonMatcher ->
                            val questionIndex = jsonMatcher.group(1).toInt()
                            EditPoll.findQuestionByIndex(poll, questionIndex)?.also { question ->
                                val choice = EditPoll.Choice()

                                val choiceIndex = jsonMatcher.group(2).toInt()
                                if (choiceIndex > question.baseIndexOffset) {
                                    question.baseIndexOffset = choiceIndex
                                }
                                choice.index = choiceIndex
                                choice.title = jsonMatcher.group(3).fromHtml()
                                question.addChoice(choice)
                            }
                        }
                        .reset(matcher.group(4)).findAll { jsonMatcher ->
                            val questionIndex = jsonMatcher.group(1).toInt()
                            EditPoll.findQuestionByIndex(poll, questionIndex)?.also { question ->
                                val choiceIndex = jsonMatcher.group(2).toInt()
                                val choice = EditPoll.findChoiceByIndex(question, choiceIndex)
                                if (choice != null) {
                                    choice.votes = jsonMatcher.group(3).toInt()
                                }
                            }
                        }
                        .reset(matcher.group(5)).findAll { jsonMatcher ->
                            val questionIndex = jsonMatcher.group(1).toInt()
                            EditPoll.findQuestionByIndex(poll, questionIndex)?.also { question ->
                                question.isMulti = jsonMatcher.group(3) == "1"
                            }
                        }

                poll.maxQuestions = matcher.group(6).toInt()
                poll.maxChoices = matcher.group(7).toInt()
                poll.title = matcher.group(8).fromHtml()
                poll
            }

}
