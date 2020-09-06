package forpdateam.ru.forpda.presentation.qms.blacklist

import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy
import com.arellomobile.mvp.viewstate.strategy.SkipStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import forpdateam.ru.forpda.common.mvp.IBaseView
import forpdateam.ru.forpda.entity.remote.others.user.ForumUser
import forpdateam.ru.forpda.entity.remote.qms.QmsContact

/**
 * Created by radiationx on 01.01.18.
 */

@StateStrategyType(AddToEndSingleStrategy::class)
interface QmsBlackListView : IBaseView {
    fun showContacts(items: List<QmsContact>)
    fun showFoundUsers(items: List<ForumUser>)

    @StateStrategyType(SkipStrategy::class)
    fun clearNickField()

    @StateStrategyType(SkipStrategy::class)
    fun showItemDialogMenu(item: QmsContact)
}
