package forpdateam.ru.forpda.ui.fragments.editpost

import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CompoundButton
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import by.kirich1409.viewbindingdelegate.viewBinding
import forpdateam.ru.forpda.App.Companion.get
import forpdateam.ru.forpda.App.Companion.getContext
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.common.simple.SimpleTextWatcher
import forpdateam.ru.forpda.databinding.EditPollQuestionBinding
import forpdateam.ru.forpda.entity.remote.editpost.EditPoll
import forpdateam.ru.forpda.entity.remote.editpost.EditPoll.Companion.findQuestionByIndex
import forpdateam.ru.forpda.entity.remote.editpost.EditPoll.Question

/**
 * Created by radiationx on 28.07.17.
 */
class PollQuestionsAdapter : RecyclerView.Adapter<PollQuestionsAdapter.ViewHolder> {
    private var questions: MutableList<Question> = ArrayList()
    private var poll: EditPoll? = null
    private val choiceAdapters = HashMap<Question, PollChoicesAdapter>()

    constructor(questions: MutableList<Question>, poll: EditPoll?) {
        this.questions = questions
        this.poll = poll
    }

    constructor()

    fun add(question: Question) {
        if (questions.size < poll!!.maxQuestions) {
            poll!!.increaseIndexOffset()
            question.index = poll!!.indexOffset + poll!!.baseIndexOffset
            questions.add(question)
            //notifyItemInserted(questions.indexOf(question));
            notifyDataSetChanged()
        } else {
            Toast.makeText(
                getContext(),
                String.format(get().getString(R.string.poll_questions_Max), poll!!.maxQuestions),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun getItem(position: Int): Question {
        return questions[position]
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v =
            LayoutInflater.from(parent.context).inflate(R.layout.edit_poll_question, parent, false)
        return ViewHolder(v, CustomTextWatcher(), CustomCheckedChangeListener())
    }

    override fun getItemCount(): Int {
        return questions.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = checkNotNull(getItem(holder.adapterPosition))

    }

    inner class ViewHolder(
        v: View,
        private var customTextWatcher: CustomTextWatcher,
        private val checkedChangeListener: CustomCheckedChangeListener
    ) : RecyclerView.ViewHolder(v) {

        private val binding by viewBinding<EditPollQuestionBinding>()

        init {
            binding.pollQuestionTitleField.addTextChangedListener(customTextWatcher)

            binding.pollQuestionMulti.setOnCheckedChangeListener(checkedChangeListener)

            binding.pollQuestionChoices.layoutManager = LinearLayoutManager(binding.pollQuestionChoices.context)

            binding.pollAddChoice.setOnClickListener { v1: View? ->
                val choicesAdapter = choiceAdapters[questions[layoutPosition]]
                choicesAdapter!!.add(EditPoll.Choice())
            }

            binding.pollQuestionDelete.setOnClickListener { v1: View? ->
                AlertDialog.Builder(v.context)
                    .setMessage(R.string.ask_delete_question)
                    .setPositiveButton(R.string.ok) { dialog: DialogInterface?, which: Int ->
                        val question = questions[layoutPosition]
                        if (question.index > poll!!.baseIndexOffset) {
                            val start = question.index
                            val end = poll!!.baseIndexOffset + poll!!.indexOffset
                            for (i in start..end) {
                                val q = findQuestionByIndex(poll!!, i)
                                if (q != null) {
                                    q.index = q.index - 1
                                }
                            }
                            poll!!.reduceIndexOffset()
                        }
                        questions.remove(question)
                        choiceAdapters.remove(question)
                        //notifyItemRemoved(getLayoutPosition());
                        notifyDataSetChanged()
                    }
                    .setNegativeButton(R.string.no, null)
                    .show()
            }
        }

        fun bind(item:Question){
            val qstr =
                String.format(get().getString(R.string.poll_question_Pos), (adapterPosition + 1))
            customTextWatcher.updatePosition(adapterPosition)
            checkedChangeListener.updatePosition(adapterPosition)

            binding.pollQuestionTitle.text = qstr
            binding.pollQuestionTitleField.setText(item.title)
            binding.pollQuestionTitleField.hint = qstr

            binding.pollQuestionMulti.isChecked = item.isMulti

            var choicesAdapter = choiceAdapters[item]

            if (choicesAdapter == null) {
                choicesAdapter = PollChoicesAdapter(item, poll)
                choiceAdapters[item] = choicesAdapter
            }


            binding.pollQuestionChoices.adapter = choicesAdapter
        }
    }

    inner class CustomTextWatcher : SimpleTextWatcher() {
        private var position = 0

        fun updatePosition(position: Int) {
            this.position = position
        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            questions[position].title = s.toString()
        }
    }

    inner class CustomCheckedChangeListener : CompoundButton.OnCheckedChangeListener {
        private var position = 0

        fun updatePosition(position: Int) {
            this.position = position
        }

        override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
            questions[position].isMulti = isChecked
        }
    }
}
