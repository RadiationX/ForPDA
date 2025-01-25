package forpdateam.ru.forpda.presentation.articles.detail.comments

import forpdateam.ru.forpda.common.mvp.IBaseView
import forpdateam.ru.forpda.entity.remote.news.Comment
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType

/**
 * Created by radiationx on 01.01.18.
 */

@StateStrategyType(AddToEndSingleStrategy::class)
interface ArticleCommentView : IBaseView {
    fun onReplyComment()
    fun showComments(comments: List<Comment>)
    fun setSendRefreshing(isRefreshing: Boolean)
    fun scrollToComment(position: Int)
    fun setMessageFieldVisible(isVisible: Boolean)
}
