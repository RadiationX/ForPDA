package forpdateam.ru.forpda.presentation.forumrules

import forpdateam.ru.forpda.common.mvp.IBaseView
import forpdateam.ru.forpda.entity.remote.forum.ForumRules
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType

/**
 * Created by radiationx on 02.01.18.
 */

@StateStrategyType(AddToEndSingleStrategy::class)
interface ForumRulesView : IBaseView {
    fun showData(data: ForumRules)
    fun setStyleType(type: String)
    fun setFontSize(size: Int)
}
