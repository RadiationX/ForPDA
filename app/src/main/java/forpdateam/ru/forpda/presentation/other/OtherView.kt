package forpdateam.ru.forpda.presentation.other

import forpdateam.ru.forpda.common.mvp.IBaseView
import forpdateam.ru.forpda.entity.app.CloseableInfo
import forpdateam.ru.forpda.entity.app.other.AppMenuItem
import forpdateam.ru.forpda.entity.remote.others.user.ForumUser
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(AddToEndSingleStrategy::class)
interface OtherView : IBaseView {
    fun showItems(
        user: ForumUser?,
        infoList: List<CloseableInfo>,
        menu: List<List<AppMenuItem>>
    )

    fun updateProfile()
}
