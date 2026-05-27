package forpdateam.ru.forpda.presentation.mentions

import forpdateam.ru.forpda.common.Utils
import forpdateam.ru.forpda.common.mvp.BasePresenter
import forpdateam.ru.forpda.entity.remote.favorites.FavoriteAction
import forpdateam.ru.forpda.entity.remote.mentions.MentionItem
import forpdateam.ru.forpda.extensions.coRunCatching
import forpdateam.ru.forpda.model.repository.faviorites.FavoritesRepository
import forpdateam.ru.forpda.model.repository.mentions.MentionsRepository
import forpdateam.ru.forpda.presentation.ErrorHandler
import forpdateam.ru.forpda.presentation.LinkHandler
import forpdateam.ru.forpda.presentation.Screen
import forpdateam.ru.forpda.presentation.TabRouter
import kotlinx.coroutines.launch
import moxy.InjectViewState
import ru.radiationx.coretypes.PageOffset
import ru.radiationx.coretypes.TopicId
import java.util.regex.Pattern

/**
 * Created by radiationx on 11.11.17.
 */

@InjectViewState
class MentionsPresenter(
    private val mentionsRepository: MentionsRepository,
    private val favoritesRepository: FavoritesRepository,
    private val router: TabRouter,
    private val linkHandler: LinkHandler,
    private val errorHandler: ErrorHandler,
    private val utils: Utils
) : BasePresenter<MentionsView>() {

    var pageOffset = PageOffset.default

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        getMentions()
    }

    fun getMentions() {
        viewModelScope.launch {
            viewState.setRefreshing(true)
            coRunCatching {
                mentionsRepository.getMentions(pageOffset)
            }.onSuccess {
                viewState.showMentions(it)
            }.onFailure {
                errorHandler.handle(it)
            }
            viewState.setRefreshing(false)
        }
    }

    fun addTopicToFavorite(topicId: TopicId, subType: String) {
        viewModelScope.launch {
            coRunCatching {
                favoritesRepository.editFavorites(FavoriteAction.AddTopic(topicId, subType))
            }.onSuccess {
                viewState.onAddToFavorite(it)
            }.onFailure {
                errorHandler.handle(it)
            }
        }
    }

    fun onItemClick(item: MentionItem) {
        linkHandler.handle(
            item.link, mapOf(
                Screen.ARG_TITLE to item.title.orEmpty()
            )
        )
    }

    fun onItemLongClick(item: MentionItem) {
        viewState.showItemDialogMenu(item)
    }

    fun copyLink(item: MentionItem) {
        utils.copyToClipBoard(item.link)
    }

    fun addToFavorites(item: MentionItem) {
        // todo refactor
        val matcher = Pattern.compile("showtopic=(\\d+)").matcher(item.link)
        if (matcher.find()) {
            val topicId = TopicId(matcher.group(1).toInt())
            viewState.showAddFavoritesDialog(topicId)
        }
    }
}
