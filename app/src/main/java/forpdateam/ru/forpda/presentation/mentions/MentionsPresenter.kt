package forpdateam.ru.forpda.presentation.mentions

import moxy.InjectViewState
import forpdateam.ru.forpda.common.Utils
import forpdateam.ru.forpda.common.mvp.BasePresenter
import forpdateam.ru.forpda.entity.remote.mentions.MentionItem
import forpdateam.ru.forpda.model.data.remote.api.favorites.FavoritesApi
import forpdateam.ru.forpda.model.repository.faviorites.FavoritesRepository
import forpdateam.ru.forpda.model.repository.mentions.MentionsRepository
import forpdateam.ru.forpda.presentation.IErrorHandler
import forpdateam.ru.forpda.presentation.ILinkHandler
import forpdateam.ru.forpda.presentation.Screen
import forpdateam.ru.forpda.presentation.TabRouter
import java.util.regex.Pattern

/**
 * Created by radiationx on 11.11.17.
 */

@InjectViewState
class MentionsPresenter(
        private val mentionsRepository: MentionsRepository,
        private val favoritesRepository: FavoritesRepository,
        private val router: TabRouter,
        private val linkHandler: ILinkHandler,
        private val errorHandler: IErrorHandler
) : BasePresenter<MentionsView>() {

    var currentSt: Int = 0

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        getMentions()
    }

    fun getMentions() {
        mentionsRepository
                .getMentions(currentSt)
                .doOnSubscribe { viewState.setRefreshing(true) }
                .doAfterTerminate { viewState.setRefreshing(false) }
                .subscribe({
                    viewState.showMentions(it)
                }, {
                    errorHandler.handle(it)
                })
                .untilDestroy()
    }

    fun addTopicToFavorite(topicId: Int, subType: String) {
        favoritesRepository
                .editFavorites(FavoritesApi.ACTION_ADD, -1, topicId, subType)
                .subscribe({
                    viewState.onAddToFavorite(it)
                }, {
                    errorHandler.handle(it)
                })
                .untilDestroy()
    }

    fun onItemClick(item: MentionItem) {
        linkHandler.handle(item.link, router, mapOf(
                Screen.ARG_TITLE to item.title.orEmpty()
        ))
    }

    fun onItemLongClick(item: MentionItem) {
        viewState.showItemDialogMenu(item)
    }

    fun copyLink(item: MentionItem) {
        Utils.copyToClipBoard(item.link)
    }

    fun addToFavorites(item: MentionItem) {
        var id = 0
        val matcher = Pattern.compile("showtopic=(\\d+)").matcher(item.link)
        if (matcher.find()) {
            id = Integer.parseInt(matcher.group(1))
        }
        viewState.showAddFavoritesDialog(id)
    }
}
