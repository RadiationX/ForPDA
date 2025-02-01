package forpdateam.ru.forpda.ui.fragments.theme

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import forpdateam.ru.forpda.App.Companion.get
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.entity.remote.ForumPost
import forpdateam.ru.forpda.model.AuthHolder
import forpdateam.ru.forpda.model.preferences.OtherPreferencesHolder
import forpdateam.ru.forpda.presentation.theme.IThemePresenter
import forpdateam.ru.forpda.ui.views.DynamicDialogMenu

/**
 * Created by radiationx on 01.11.16.
 */
class ThemeDialogsHelper_V2(
    private val context: Context,
    private val authHolder: AuthHolder,
    private val otherPreferencesHolder: OtherPreferencesHolder
) {
    private val userMenu = DynamicDialogMenu<IThemePresenter, ForumPost>()
    private val reputationMenu = DynamicDialogMenu<IThemePresenter, ForumPost>()
    private val postMenu = DynamicDialogMenu<IThemePresenter, ForumPost>()

    init {
        userMenu.addItem(
            get().getString(R.string.profile)
        ) { context1: IThemePresenter, data: ForumPost -> context1.openProfile(data.id) }
        userMenu.addItem(
            get().getString(R.string.reputation)
        ) { context1: IThemePresenter, data: ForumPost -> context1.onReputationMenuClick(data.id) }
        userMenu.addItem(
            get().getString(R.string.pm_qms)
        ) { context1: IThemePresenter, data: ForumPost -> context1.openQms(data.id) }
        userMenu.addItem(
            get().getString(R.string.user_themes)
        ) { context1: IThemePresenter, data: ForumPost -> context1.openSearchUserTopic(data.id) }
        userMenu.addItem(
            get().getString(R.string.messages_in_this_theme)
        ) { context1: IThemePresenter, data: ForumPost -> context1.openSearchInTopic(data.id) }
        userMenu.addItem(
            get().getString(R.string.user_messages)
        ) { context1: IThemePresenter, data: ForumPost -> context1.openSearchUserMessages(data.id) }


        reputationMenu.addItem(
            get().getString(R.string.increase)
        ) { context1: IThemePresenter, data: ForumPost ->
            context1.onChangeReputationClick(
                data.id,
                true
            )
        }
        reputationMenu.addItem(
            get().getString(R.string.look)
        ) { context1: IThemePresenter, data: ForumPost -> context1.openReputationHistory(data.id) }
        reputationMenu.addItem(
            get().getString(R.string.decrease)
        ) { context1: IThemePresenter, data: ForumPost ->
            context1.onChangeReputationClick(
                data.id,
                false
            )
        }

        postMenu.addItem(
            get().getString(R.string.reply)
        ) { context1: IThemePresenter, data: ForumPost -> context1.onReplyPostClick(data.id) }
        postMenu.addItem(
            get().getString(R.string.quote_from_clipboard)
        ) { context1: IThemePresenter, data: ForumPost -> context1.quoteFromBuffer(data.id) }
        postMenu.addItem(
            get().getString(R.string.report)
        ) { context1: IThemePresenter, data: ForumPost -> context1.onReportPostClick(data.id) }
        postMenu.addItem(
            get().getString(R.string.edit)
        ) { context1: IThemePresenter, data: ForumPost -> context1.onEditPostClick(data.id) }
        postMenu.addItem(
            get().getString(R.string.delete)
        ) { context1: IThemePresenter, data: ForumPost -> context1.onDeletePostClick(data.id) }
        postMenu.addItem(
            get().getString(R.string.copy_link)
        ) { context1: IThemePresenter, data: ForumPost -> context1.copyPostLink(data.id) }
        postMenu.addItem(
            get().getString(R.string.create_note)
        ) { context1: IThemePresenter, data: ForumPost -> context1.createNote(data.id) }
        postMenu.addItem(
            get().getString(R.string.share)
        ) { context1: IThemePresenter, data: ForumPost -> context1.sharePostLink(data.id) }
    }

    fun showUserMenu(presenter: IThemePresenter, post: ForumPost) {
        userMenu.disallowAll()
        userMenu.allow(0)
        userMenu.allow(1)
        val authData = authHolder.get()
        if (authData.isAuth() && post.userId != authData.userId) {
            userMenu.allow(2)
        }
        userMenu.allow(3)
        userMenu.allow(4)
        userMenu.allow(5)
        userMenu.show(context, presenter, post)
    }

    fun showReputationMenu(presenter: IThemePresenter, post: ForumPost) {
        reputationMenu.disallowAll()
        if (!authHolder.get().isAuth() || post.canPlusRep) {
            reputationMenu.allow(0)
        }
        reputationMenu.allow(1)
        if (!authHolder.get().isAuth() || post.canMinusRep) {
            reputationMenu.allow(2)
        }
        val title = get().getString(R.string.reputation) + (" " + post.nick)
        reputationMenu.show(context, title, presenter, post)
    }

    fun showPostMenu(presenter: IThemePresenter, post: ForumPost) {
        postMenu.disallowAll()
        if (!authHolder.get().isAuth() || post.canQuote) {
            postMenu.allow(0)
            postMenu.allow(1)
        }
        if (authHolder.get().isAuth()) {
            if (post.canReport) postMenu.allow(2)
            if (post.canEdit) postMenu.allow(3)
            if (post.canDelete) postMenu.allow(4)
        }
        postMenu.allow(5)
        postMenu.allow(6)
        postMenu.allow(7)
        postMenu.show(context, presenter, post)
    }

    fun tryReportPost(presenter: IThemePresenter, post: ForumPost) {
        if (otherPreferencesHolder.getShowReportWarning()) {
            AlertDialog.Builder(context)
                .setTitle(R.string.attention)
                .setMessage(R.string.report_warning)
                .setPositiveButton(R.string.ok) { dialogInterface: DialogInterface?, i: Int ->
                    otherPreferencesHolder.setShowReportWarning(false)
                    showReportDialog(presenter, post)
                }
                .show()
        } else {
            showReportDialog(presenter, post)
        }
    }

    @SuppressLint("InflateParams")
    fun showReportDialog(presenter: IThemePresenter, post: ForumPost) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val layout = checkNotNull(inflater.inflate(R.layout.report_layout, null))

        val messageField = layout.findViewById<EditText>(R.id.report_text_field)

        AlertDialog.Builder(context)
            .setTitle(String.format(get().getString(R.string.report_to_post_Nick), post.nick))
            .setView(layout)
            .setPositiveButton(
                R.string.send
            ) { dialogInterface: DialogInterface?, i: Int ->
                presenter.reportPost(
                    post.id,
                    messageField.text.toString()
                )
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    fun deletePost(presenter: IThemePresenter, post: ForumPost) {
        AlertDialog.Builder(context)
            .setMessage(String.format(get().getString(R.string.ask_delete_post_Nick), post.nick))
            .setPositiveButton(
                R.string.ok
            ) { dialogInterface: DialogInterface?, i: Int -> presenter.deletePost(post.id) }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    @SuppressLint("InflateParams")
    fun changeReputation(presenter: IThemePresenter, post: ForumPost, type: Boolean) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val layout = checkNotNull(inflater.inflate(R.layout.reputation_change_layout, null))

        val text = layout.findViewById<TextView>(R.id.reputation_text)
        val messageField = layout.findViewById<EditText>(R.id.reputation_text_field)
        text.text = String.format(
            context.getString(R.string.change_reputation_Type_Nick),
            context.getString(if (type) R.string.increase else R.string.decrease),
            post.nick
        )

        AlertDialog.Builder(context)
            .setView(layout)
            .setPositiveButton(
                R.string.ok
            ) { dialogInterface: DialogInterface?, i: Int ->
                presenter.changeReputation(
                    post.id,
                    type,
                    messageField.text.toString()
                )
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    fun votePost(presenter: IThemePresenter, post: ForumPost, type: Boolean) {
        AlertDialog.Builder(context)
            .setMessage(
                String.format(
                    context.getString(R.string.change_post_reputation_Type_Nick),
                    context.getString(if (type) R.string.increase else R.string.decrease),
                    post.nick
                )
            )
            .setPositiveButton(
                R.string.ok
            ) { dialog: DialogInterface?, which: Int -> presenter.votePost(post.id, type) }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    fun openAnchorDialog(presenter: IThemePresenter, post: ForumPost, anchorName: String) {
        AlertDialog.Builder(context)
            .setTitle(R.string.link_to_anchor)
            .setPositiveButton(
                R.string.copy
            ) { dialog: DialogInterface?, which: Int ->
                presenter.copyAnchorLink(
                    post.id,
                    anchorName
                )
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    fun openSpoilerLinkDialog(
        presenter: IThemePresenter,
        post: ForumPost,
        spoilNumber: String
    ) {
        AlertDialog.Builder(context)
            .setMessage(R.string.spoiler_link_copy_ask)
            .setPositiveButton(
                R.string.ok
            ) { dialog: DialogInterface?, which: Int ->
                presenter.copySpoilerLink(
                    post.id,
                    spoilNumber
                )
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
}

