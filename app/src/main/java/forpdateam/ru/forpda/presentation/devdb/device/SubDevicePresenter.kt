package forpdateam.ru.forpda.presentation.devdb.device

import forpdateam.ru.forpda.common.mvp.BasePresenter
import forpdateam.ru.forpda.entity.remote.devdb.Device
import forpdateam.ru.forpda.presentation.ILinkHandler
import forpdateam.ru.forpda.presentation.TabRouter
import forpdateam.ru.forpda.ui.fragments.devdb.device.posts.PostsFragment
import moxy.InjectViewState

/**
 * Created by radiationx on 11.11.17.
 */

@InjectViewState
class SubDevicePresenter(
    private val router: TabRouter,
    private val linkHandler: ILinkHandler
) : BasePresenter<SubDeviceView>() {

    fun onCommentClick(item: Device.Comment) {
        linkHandler.handle("https://4pda.to/forum/index.php?showuser=${item.user.id}", router)
    }

    fun onPostClick(item: Device.PostItem, source: Int) {
        val url = if (source == PostsFragment.SRC_NEWS) {
            "https://4pda.to/index.php?p=${item.id}"
        } else {
            "https://4pda.to/forum/index.php?showtopic=${item.id}"
        }
        linkHandler.handle(url, router)
    }

}
