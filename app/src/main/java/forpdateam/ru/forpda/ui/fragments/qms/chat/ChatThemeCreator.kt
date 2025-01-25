package forpdateam.ru.forpda.ui.fragments.qms.chat

import android.view.View
import android.view.ViewStub
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.widget.AppCompatAutoCompleteTextView
import androidx.appcompat.widget.AppCompatEditText
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.common.simple.SimpleTextWatcher
import forpdateam.ru.forpda.entity.remote.others.user.ForumUser
import forpdateam.ru.forpda.presentation.qms.chat.QmsChatPresenter

/**
 * Created by radiationx on 11.06.17.
 */
class ChatThemeCreator internal constructor(
    private val fragment: QmsChatFragment,
    private val presenter: QmsChatPresenter
) {
    private val viewStub =
        fragment.findViewById(R.id.toolbar_content) as ViewStub
    private val nickField: AppCompatAutoCompleteTextView
    private val titleField: AppCompatEditText

    private var userNick: String?
    private var themeTitle: String?

    init {
        viewStub.layoutResource = R.layout.toolbar_qms_new_theme
        viewStub.inflate()
        nickField =
            fragment.findViewById(R.id.qms_theme_nick_field) as AppCompatAutoCompleteTextView
        titleField = fragment.findViewById(R.id.qms_theme_title_field) as AppCompatEditText
        this.userNick = presenter.nick
        this.themeTitle = presenter.title
        initCreatorViews()
    }

    private fun searchUser(nick: String) {
        presenter.findUser(nick)
    }

    fun onShowSearchRes(res: List<ForumUser>) {
        val nicks: MutableList<String?> = ArrayList()
        for (user in res) {
            nicks.add(user.nick)
        }
        nickField.setAdapter<ArrayAdapter<String>>(
            ArrayAdapter(
                nickField.context,
                android.R.layout.simple_dropdown_item_1line,
                nicks
            )
        )
    }

    private fun initCreatorViews() {
        if (userNick != null) {
            nickField.visibility = View.GONE
        } else {
            nickField.addTextChangedListener(object : SimpleTextWatcher() {
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    userNick = s.toString()
                    searchUser(userNick!!)
                    fragment.setSubtitle(userNick)
                }
            })
        }
        titleField.addTextChangedListener(object : SimpleTextWatcher() {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                themeTitle = s.toString()
                fragment.setTitle(themeTitle)
            }
        })
    }

    fun sendNewTheme() {
        if (userNick == null || userNick!!.isEmpty()) {
            Toast.makeText(
                fragment.requireContext(),
                R.string.chat_creator_enter_nick,
                Toast.LENGTH_SHORT
            ).show()
        } else if (titleField.text.toString().isEmpty()) {
            Toast.makeText(
                fragment.requireContext(),
                R.string.chat_creator_enter_title,
                Toast.LENGTH_SHORT
            ).show()
        } else if (fragment.messagePanel.message.isEmpty()) {
            Toast.makeText(
                fragment.requireContext(),
                R.string.chat_creator_enter_message,
                Toast.LENGTH_SHORT
            ).show()
        } else {
            fragment.onCreateNewTheme(
                userNick,
                titleField.text.toString(),
                fragment.messagePanel.message
            )
        }
    }

    fun setVisible(isVisible: Boolean) {
        viewStub.visibility = if (isVisible) View.VISIBLE else View.GONE
        //editItem.setVisible(isVisible);
        //doneItem.setVisible(isVisible);
    }

    interface ThemeCreatorInterface {
        fun onCreateNewTheme(nick: String?, title: String?, message: String?)
    }
}
