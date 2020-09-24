package forpdateam.ru.forpda.presentation.other

import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType
import forpdateam.ru.forpda.common.mvp.IBaseView
import forpdateam.ru.forpda.entity.app.CloseableInfo
import forpdateam.ru.forpda.entity.app.other.AppMenuItem
import forpdateam.ru.forpda.entity.remote.profile.ProfileModel

@StateStrategyType(AddToEndSingleStrategy::class)
interface OtherView : IBaseView {
    fun showItems(profileItem: ProfileModel?, infoList: List<CloseableInfo>, menu: List<List<AppMenuItem>>)
    fun updateProfile()
}
