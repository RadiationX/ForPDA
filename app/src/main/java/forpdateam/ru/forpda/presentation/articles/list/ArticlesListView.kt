package forpdateam.ru.forpda.presentation.articles.list

import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy
import com.arellomobile.mvp.viewstate.strategy.SkipStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import forpdateam.ru.forpda.common.mvp.IBaseView
import forpdateam.ru.forpda.entity.remote.news.NewsItem

/**
 * Created by radiationx on 01.01.18.
 */

@StateStrategyType(AddToEndSingleStrategy::class)
interface ArticlesListView : IBaseView {
    fun showNews(items: List<NewsItem>, withClear: Boolean)
    fun updateItems(items: List<NewsItem>)

    @StateStrategyType(SkipStrategy::class)
    fun showItemDialogMenu(item: NewsItem)

    @StateStrategyType(SkipStrategy::class)
    fun showCreateNote(title: String, url: String)
}
