package forpdateam.ru.forpda.presentation.qms.contacts

import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.SkipStrategy
import moxy.viewstate.strategy.StateStrategyType
import forpdateam.ru.forpda.common.mvp.IBaseView
import forpdateam.ru.forpda.entity.remote.qms.QmsContact

/**
 * Created by radiationx on 01.01.18.
 */

@StateStrategyType(AddToEndSingleStrategy::class)
interface QmsContactsView : IBaseView {
    fun showContacts(items: List<QmsContact>)

    @StateStrategyType(SkipStrategy::class)
    fun onBlockUser(res: Boolean)

    @StateStrategyType(SkipStrategy::class)
    fun showCreateNote(nick: String, url: String)

    @StateStrategyType(SkipStrategy::class)
    fun showItemDialogMenu(item: QmsContact)
}
