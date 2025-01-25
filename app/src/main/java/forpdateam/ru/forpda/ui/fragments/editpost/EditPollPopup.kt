package forpdateam.ru.forpda.ui.fragments.editpost

import android.content.Context
import android.content.DialogInterface
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.common.simple.SimpleTextWatcher
import forpdateam.ru.forpda.entity.remote.editpost.EditPoll
import forpdateam.ru.forpda.entity.remote.editpost.EditPoll.Question

/**
 * Created by radiationx on 28.07.17.
 */
class EditPollPopup(context: Context) {
    private val dialog = BottomSheetDialog(context)
    private val bottomSheet: View?

    private val pollTitle: TextView
    private val pollTitleField: EditText
    private val addPoll: ImageButton
    private val questionsView: RecyclerView

    private var questionsAdapter: PollQuestionsAdapter? = null
    private var poll: EditPoll? = null


    init {
        dialog.setOnShowListener { dialog1: DialogInterface? ->
            dialog.window!!
                .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        }

        bottomSheet = View.inflate(context, R.layout.edit_poll, null)

        pollTitle = bottomSheet.findViewById(R.id.poll_title)
        pollTitleField = bottomSheet.findViewById(R.id.poll_title_field)
        addPoll = bottomSheet.findViewById(R.id.add_poll)
        questionsView = bottomSheet.findViewById(R.id.poll_questions)

        questionsView.layoutManager = LinearLayoutManager(questionsView.context)

        addPoll.setOnClickListener { v: View? ->
            questionsAdapter!!.add(Question())
        }
        pollTitleField.addTextChangedListener(object : SimpleTextWatcher() {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (poll != null) {
                    poll!!.title = s.toString()
                }
            }
        })
    }

    fun show() {
        if (bottomSheet != null && bottomSheet.parent != null && bottomSheet.parent is ViewGroup) {
            (bottomSheet.parent as ViewGroup).removeView(bottomSheet)
        }
        dialog.setContentView(bottomSheet!!)
        dialog.window!!.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)

        //dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        dialog.show()
    }

    fun setPoll(poll: EditPoll) {
        this.poll = poll
        pollTitleField.setText(poll.title)
        questionsAdapter = PollQuestionsAdapter(poll.getQuestions(), poll)
        questionsView.adapter = questionsAdapter
    }
}
