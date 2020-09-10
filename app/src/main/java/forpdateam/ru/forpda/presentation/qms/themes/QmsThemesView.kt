package forpdateam.ru.forpda.presentation.qms.themes

import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.SkipStrategy
import moxy.viewstate.strategy.StateStrategyType
import forpdateam.ru.forpda.common.mvp.IBaseView
import forpdateam.ru.forpda.entity.remote.qms.QmsTheme
import forpdateam.ru.forpda.entity.remote.qms.QmsThemes

/**
 * Created by radiationx on 01.01.18.
 */

@StateStrategyType(AddToEndSingleStrategy::class)
interface QmsThemesView : IBaseView {
    fun showThemes(data: QmsThemes)
    fun showAvatar(avatarUrl: String)

    @StateStrategyType(SkipStrategy::class)
    fun showCreateNote(nick: String, url: String)

    @StateStrategyType(SkipStrategy::class)
    fun showCreateNote(name: String, nick: String, url: String)

    @StateStrategyType(SkipStrategy::class)
    fun onBlockUser(res: Boolean)

    @StateStrategyType(SkipStrategy::class)
    fun showItemDialogMenu(item: QmsTheme)
}
