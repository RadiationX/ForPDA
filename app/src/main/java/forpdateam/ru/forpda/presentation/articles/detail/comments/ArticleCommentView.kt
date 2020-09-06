package forpdateam.ru.forpda.presentation.articles.detail.comments

import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import forpdateam.ru.forpda.common.mvp.IBaseView
import forpdateam.ru.forpda.entity.remote.news.Comment

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
