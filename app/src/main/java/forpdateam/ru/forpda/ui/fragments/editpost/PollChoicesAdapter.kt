package forpdateam.ru.forpda.ui.fragments.editpost

import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputLayout
import forpdateam.ru.forpda.App.Companion.get
import forpdateam.ru.forpda.App.Companion.getContext
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.common.simple.SimpleTextWatcher
import forpdateam.ru.forpda.entity.remote.editpost.EditPoll
import forpdateam.ru.forpda.entity.remote.editpost.EditPoll.Companion.findChoiceByIndex
import forpdateam.ru.forpda.entity.remote.editpost.EditPoll.Question

/**
 * Created by radiationx on 28.07.17.
 */
class PollChoicesAdapter : RecyclerView.Adapter<PollChoicesAdapter.ViewHolder> {
    private var choices:ArrayList<EditPoll.Choice> = ArrayList<EditPoll.Choice>()
    private var poll: EditPoll? = null
    private var question: Question? = null

    constructor(question: Question, poll: EditPoll?) {
        this.choices = question.getChoices()
        this.poll = poll
        this.question = question
    }

    constructor()

    fun add(choice: EditPoll.Choice) {
        if (choices.size < poll!!.maxChoices) {
            question!!.increaseIndexOffset()
            choice.index = question!!.indexOffset + question!!.baseIndexOffset
            choices.add(choice)
            //notifyItemInserted(choices.indexOf(choice));
            notifyDataSetChanged()
        } else {
            Toast.makeText(
                getContext(),
                String.format(get().getString(R.string.poll_answers_Max), poll!!.maxChoices),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun getItem(position: Int): EditPoll.Choice {
        return choices[position]
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v =
            LayoutInflater.from(parent.context).inflate(R.layout.edit_poll_choice, parent, false)
        return ViewHolder(v, MyCustomEditTextListener())
    }

    override fun getItemCount(): Int {
        return choices.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = checkNotNull(getItem(holder.adapterPosition))
        holder.myCustomEditTextListener.updatePosition(holder.adapterPosition)
        holder.title.editText!!.setText(item.title)
        holder.title.hint = String.format(
            get().getString(R.string.poll_answer_Pos),
            holder.adapterPosition + 1
        )
    }


    inner class ViewHolder(
        v: View,
        var myCustomEditTextListener: MyCustomEditTextListener
    ) : RecyclerView.ViewHolder(v) {
        var title: TextInputLayout = v.findViewById(R.id.poll_choice_title)
        var delete: ImageButton = v.findViewById(R.id.poll_choice_delete)

        init {
            title.editText!!.addTextChangedListener(myCustomEditTextListener)
            delete.setOnClickListener { v1: View? ->
                AlertDialog.Builder(v.context)
                    .setMessage(R.string.ask_delete_answer)
                    .setPositiveButton(R.string.ok) { dialog: DialogInterface?, which: Int ->
                        val choice = choices[layoutPosition]
                        //notifyItemRemoved(getLayoutPosition());
                        if (choice.index > question!!.baseIndexOffset) {
                            val start = choice.index
                            val end = question!!.baseIndexOffset + question!!.indexOffset
                            for (i in start..end) {
                                val c = findChoiceByIndex(
                                    question!!, i
                                )
                                if (c != null) {
                                    c.index = c.index - 1
                                }
                            }
                            question!!.reduceIndexOffset()
                        }
                        choices.removeAt(layoutPosition)
                        notifyDataSetChanged()
                    }
                    .setNegativeButton(R.string.no, null)
                    .show()
            }
        }
    }

    inner class MyCustomEditTextListener : SimpleTextWatcher() {
        private var position = 0

        fun updatePosition(position: Int) {
            this.position = position
        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            choices[position].title = s.toString()
        }
    }
}
