package forpdateam.ru.forpda.ui.fragments.news.details

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.RelativeLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatImageButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import by.kirich1409.viewbindingdelegate.viewBinding
import forpdateam.ru.forpda.App
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.common.Utils
import forpdateam.ru.forpda.common.simple.SimpleTextWatcher
import forpdateam.ru.forpda.databinding.ArticleCommentsBinding
import forpdateam.ru.forpda.entity.remote.news.Comment
import forpdateam.ru.forpda.extensions.getColorFromAttr
import forpdateam.ru.forpda.presentation.articles.detail.comments.ArticleCommentPresenter
import forpdateam.ru.forpda.presentation.articles.detail.comments.ArticleCommentView
import forpdateam.ru.forpda.ui.fragments.RecyclerTopScroller
import forpdateam.ru.forpda.ui.fragments.TabTopScroller
import forpdateam.ru.forpda.ui.fragments.devdb.brand.DevicesFragment
import forpdateam.ru.forpda.ui.views.ContentController
import forpdateam.ru.forpda.ui.views.FunnyContent
import moxy.MvpAppCompatFragment
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter

/**
 * Created by radiationx on 03.09.17.
 */

class ArticleCommentsFragment : MvpAppCompatFragment(R.layout.article_comments), ArticleCommentView,
    ArticleCommentsAdapter.ClickListener, TabTopScroller {

    private val binding by viewBinding<ArticleCommentsBinding>()

    private val refreshLayout: SwipeRefreshLayout
        get() = binding.swipeRefreshList
    private val recyclerView: RecyclerView
        get() = binding.baseList
    private val messageField: EditText
        get() = binding.messageField
    private val buttonSend: AppCompatImageButton
        get() = binding.buttonSend
    private val progressBarSend: ProgressBar
        get() = binding.sendProgress
    private val writePanel: RelativeLayout
        get() = binding.commentWritePanel

    private val authHolder = App.get().Di().authHolder
    private val adapter = ArticleCommentsAdapter(authHolder)
    private var currentReplyComment: Comment? = null
    private lateinit var contentController: ContentController
    private lateinit var topScroller: RecyclerTopScroller

    @InjectPresenter
    lateinit var presenter: ArticleCommentPresenter

    @ProvidePresenter
    fun providePresenter(): ArticleCommentPresenter = ArticleCommentPresenter(
        (parentFragment as NewsDetailsFragment).provideChildInteractor(),
        App.get().Di().router,
        App.get().Di().linkHandler,
        App.get().Di().authHolder,
        App.get().Di().errorHandler
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        contentController = ContentController(null, binding.additionalContent, refreshLayout)

        refreshLayout.setProgressBackgroundColorSchemeColor(
            refreshLayout.context.getColorFromAttr(androidx.appcompat.R.attr.colorPrimary)
        )
        refreshLayout.setColorSchemeColors(
            refreshLayout.context.getColorFromAttr(androidx.appcompat.R.attr.colorAccent)
        )
        refreshLayout.setOnRefreshListener { presenter.updateComments() }

        recyclerView.setBackgroundColor(recyclerView.context.getColorFromAttr(R.attr.background_for_lists))
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.setHasFixedSize(true)
        recyclerView.addItemDecoration(DevicesFragment.SpacingItemDecoration(App.px12, false))
        adapter.clickListener = this
        recyclerView.adapter = adapter

        topScroller =
            RecyclerTopScroller(recyclerView, (parentFragment as NewsDetailsFragment).getAppBar())

        messageField.addTextChangedListener(object : SimpleTextWatcher() {
            override fun afterTextChanged(s: Editable) {
                if (s.isEmpty()) {
                    currentReplyComment = null
                }
                buttonSend.isClickable = s.isNotEmpty()
            }
        })

        buttonSend.setOnClickListener { sendComment() }
    }

    override fun toggleScrollTop() {
        topScroller.toggleScrollTop()
    }

    private fun createFunny(comments: List<Comment>) {
        if (comments.isEmpty()) {
            if (!contentController.contains(ContentController.TAG_NO_DATA)) {
                val funnyContent = FunnyContent(requireContext())
                    .setImage(R.drawable.ic_comment)
                    .setTitle(R.string.funny_article_comments_nodata_title)
                contentController.addContent(funnyContent, ContentController.TAG_NO_DATA)
            }
            contentController.showContent(ContentController.TAG_NO_DATA)
        } else {
            contentController.hideContent(ContentController.TAG_NO_DATA)
        }
    }

    override fun setMessageFieldVisible(isVisible: Boolean) {
        if (isVisible) {
            writePanel.visibility = View.VISIBLE
        } else {
            writePanel.visibility = View.GONE
        }
        adapter.notifyDataSetChanged()
    }

    override fun onNickClick(comment: Comment, position: Int) {
        presenter.openProfile(comment)
    }

    override fun onLikeClick(comment: Comment, position: Int) {
        if (!authHolder.get().isAuth()) {
            Utils.showNeedAuthDialog(requireContext())
            return
        }

        presenter.likeComment(comment.id)
    }

    override fun onReplyClick(comment: Comment, position: Int) {
        if (!authHolder.get().isAuth()) {
            Utils.showNeedAuthDialog(requireContext())
            return
        }
        if (messageField.text.isEmpty()) {
            fillMessageField(comment)
        } else {
            AlertDialog.Builder(requireContext())
                .setMessage(R.string.comment_reply_warning)
                .setPositiveButton(getString(R.string.ok)) { _, _ ->
                    fillMessageField(comment)
                }
                .setNegativeButton(getString(R.string.cancel), null)
                .show()
        }

    }

    @SuppressLint("SetTextI18n")
    private fun fillMessageField(comment: Comment) {
        currentReplyComment = comment
        messageField.setText("${currentReplyComment?.user?.nick},\n")
        messageField.setSelection(messageField.text.length)
        messageField.requestFocus()
        val imm =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(messageField, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun sendComment() {
        val commentId = currentReplyComment?.id ?: 0
        presenter.replyComment(commentId, messageField.text.toString())
    }

    override fun setRefreshing(isRefreshing: Boolean) {
        refreshLayout.isRefreshing = isRefreshing
    }

    override fun setSendRefreshing(isRefreshing: Boolean) {
        progressBarSend.visibility = if (isRefreshing) View.VISIBLE else View.GONE
        buttonSend.visibility = if (isRefreshing) View.GONE else View.VISIBLE
    }

    override fun showComments(comments: List<Comment>) {
        adapter.addAll(comments)
        createFunny(comments)
    }

    override fun scrollToComment(position: Int) {
        recyclerView.scrollToPosition(position)
    }

    override fun onReplyComment() {
        messageField.text = null
        currentReplyComment = null
    }

}
