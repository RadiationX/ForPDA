package forpdateam.ru.forpda.presentation.articles.detail

import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.SkipStrategy
import moxy.viewstate.strategy.StateStrategyType
import forpdateam.ru.forpda.common.mvp.IBaseView
import forpdateam.ru.forpda.entity.remote.news.DetailsPage

/**
 * Created by radiationx on 01.01.18.
 */

@StateStrategyType(AddToEndSingleStrategy::class)
interface ArticleDetailView : IBaseView {
    fun showArticle(data: DetailsPage)
    fun showArticleImage(imageUrl: String)

    @StateStrategyType(SkipStrategy::class)
    fun showCreateNote(title: String, url: String)
}
