package forpdateam.ru.forpda.presentation.devdb.device

import moxy.InjectViewState
import forpdateam.ru.forpda.common.Utils
import forpdateam.ru.forpda.common.mvp.BasePresenter
import forpdateam.ru.forpda.entity.remote.devdb.Device
import forpdateam.ru.forpda.model.repository.devdb.DevDbRepository
import forpdateam.ru.forpda.presentation.IErrorHandler
import forpdateam.ru.forpda.presentation.ILinkHandler
import forpdateam.ru.forpda.presentation.Screen
import forpdateam.ru.forpda.presentation.TabRouter
import forpdateam.ru.forpda.ui.fragments.devdb.device.posts.PostsFragment

/**
 * Created by radiationx on 11.11.17.
 */

@InjectViewState
class SubDevicePresenter(
        private val router: TabRouter,
        private val linkHandler: ILinkHandler
) : BasePresenter<SubDeviceView>() {

    fun onCommentClick(item: Device.Comment) {
        linkHandler.handle("https://4pda.ru/forum/index.php?showuser=${item.userId}", router)
    }

    fun onPostClick(item: Device.PostItem, source: Int) {
        val url = if (source == PostsFragment.SRC_NEWS) {
            "https://4pda.ru/index.php?p=${item.id}"
        } else {
            "https://4pda.ru/forum/index.php?showtopic=${item.id}"
        }
        linkHandler.handle(url, router)
    }

}
