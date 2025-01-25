package forpdateam.ru.forpda.entity.remote.editpost

/**
 * Created by radiationx on 28.07.17.
 */
class EditPoll {
    @JvmField
    var title: String = ""
    @JvmField
    var maxQuestions: Int = 0
    @JvmField
    var maxChoices: Int = 0
    @JvmField
    var baseIndexOffset: Int = 0
    var indexOffset: Int = 0
        private set
    private val questions: MutableList<Question> = ArrayList()

    fun getQuestions(): MutableList<Question> {
        return questions
    }

    fun getQuestion(index: Int): Question {
        return questions[index]
    }

    fun addQuestion(question: Question) {
        questions.add(question)
    }

    fun increaseIndexOffset() {
        indexOffset++
    }

    fun reduceIndexOffset() {
        indexOffset--
    }

    class Question {
        @JvmField
        var title: String = ""
        @JvmField
        var isMulti: Boolean = false
        @JvmField
        var index: Int = 0
        @JvmField
        var baseIndexOffset: Int = 0
        var indexOffset: Int = 0
            private set
        private val choices: ArrayList<Choice> = ArrayList()

        fun getChoices(): ArrayList<Choice> {
            return choices
        }

        fun getChoice(index: Int): Choice {
            return choices[index]
        }

        fun addChoice(choice: Choice) {
            choices.add(choice)
        }

        fun increaseIndexOffset() {
            indexOffset++
        }

        fun reduceIndexOffset() {
            indexOffset--
        }
    }

    class Choice {
        @JvmField
        var title: String = ""
        var votes: Int = 0
        @JvmField
        var index: Int = 0
    }

    companion object {
        @JvmStatic
        fun findQuestionByIndex(poll: EditPoll, index: Int): Question? {
            for (q in poll.getQuestions()) {
                if (index == q.index) {
                    return q
                }
            }
            return null
        }

        @JvmStatic
        fun findChoiceByIndex(question: Question, index: Int): Choice? {
            for (q in question.getChoices()) {
                if (index == q.index) {
                    return q
                }
            }
            return null
        }
    }
}
