package forpdateam.ru.forpda.ui.fragments.qms.chat

import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.widget.AppCompatAutoCompleteTextView
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.view.isVisible
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.common.simple.SimpleTextWatcher
import forpdateam.ru.forpda.databinding.ToolbarQmsNewThemeBinding
import forpdateam.ru.forpda.entity.remote.others.user.ForumUser
import forpdateam.ru.forpda.presentation.qms.chat.QmsChatPresenter

/**
 * Created by radiationx on 11.06.17.
 */
class ChatThemeCreator internal constructor(
    private val fragment: QmsChatFragment,
    private val presenter: QmsChatPresenter,
    private val toolbarBinding: ToolbarQmsNewThemeBinding
) {
    private val nickField: AppCompatAutoCompleteTextView
        get() = toolbarBinding.qmsThemeNickField
    private val titleField: AppCompatEditText
        get() = toolbarBinding.qmsThemeTitleField

    private var userNick: String? = null
    private var themeTitle: String? = null

    init {
        initCreatorViews()
    }

    private fun searchUser(nick: String) {
        presenter.findUser(nick)
    }

    fun initNick(nick: String) {
        userNick = nick
        nickField.isVisible = false
        fragment.setSubtitle(userNick)
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
        nickField.addTextChangedListener(object : SimpleTextWatcher() {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                userNick = s.toString()
                searchUser(userNick!!)
                fragment.setSubtitle(userNick)
            }
        })
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
                userNick!!,
                titleField.text.toString(),
                fragment.messagePanel.message
            )
        }
    }

    fun setVisible(isVisible: Boolean) {
        toolbarBinding.root.visibility = if (isVisible) View.VISIBLE else View.GONE
        //editItem.setVisible(isVisible);
        //doneItem.setVisible(isVisible);
    }

    interface ThemeCreatorInterface {
        fun onCreateNewTheme(nick: String, title: String, message: String)
    }
}
