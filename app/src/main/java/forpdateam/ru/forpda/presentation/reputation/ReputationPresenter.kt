package forpdateam.ru.forpda.presentation.reputation

import forpdateam.ru.forpda.common.mvp.BasePresenter
import forpdateam.ru.forpda.entity.remote.reputation.RepData
import forpdateam.ru.forpda.entity.remote.reputation.RepItem
import forpdateam.ru.forpda.extensions.coRunCatching
import forpdateam.ru.forpda.model.repository.avatar.AvatarRepository
import forpdateam.ru.forpda.model.repository.reputation.ReputationRepository
import forpdateam.ru.forpda.presentation.ErrorHandler
import forpdateam.ru.forpda.presentation.LinkHandler
import forpdateam.ru.forpda.presentation.TabRouter
import kotlinx.coroutines.launch
import moxy.InjectViewState
import ru.radiationx.coretypes.PageOffset
import ru.radiationx.coretypes.UserId
import ru.radiationx.links.Link
import ru.radiationx.quill.QuillExtra

/**
 * Created by radiationx on 03.01.18.
 */
data class ReputationExtra(
    val link: Link.Board.Reputation.History
) : QuillExtra

@InjectViewState
class ReputationPresenter(
    private val argExtra: ReputationExtra,
    private val reputationRepository: ReputationRepository,
    private val avatarRepository: AvatarRepository,
    private val router: TabRouter,
    private val linkHandler: LinkHandler,
    private val errorHandler: ErrorHandler
) : BasePresenter<ReputationView>() {

    var currentLink = argExtra.link
    var currentData: RepData? = null

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        loadReputation()
    }

    fun loadReputation(link: Link.Board.Reputation.History? = null) {
        currentLink = link ?: currentLink
        viewModelScope.launch {
            viewState.setRefreshing(true)
            coRunCatching {
                reputationRepository.loadReputation(currentLink)
            }.onSuccess {
                currentData = it
                viewState.showReputation(it)
                tryShowAvatar(it)
            }.onFailure {
                errorHandler.handle(it)
            }
            viewState.setRefreshing(false)
        }
    }

    fun changeReputation(type: Boolean, message: String) {
        viewModelScope.launch {
            viewState.setRefreshing(true)
            coRunCatching {
                reputationRepository.changeReputation(null, currentLink.userId, type, message)
            }.onSuccess {
                viewState.onChangeReputation()
                loadReputation()
            }.onFailure {
                errorHandler.handle(it)
            }
            viewState.setRefreshing(false)
        }
    }

    private fun tryShowAvatar(data: RepData) {
        viewModelScope.launch {
            coRunCatching {
                avatarRepository.getAvatar(data.nick.orEmpty())
            }.onSuccess {
                viewState.showAvatar(it)
            }.onFailure {
                errorHandler.handle(it)
            }
        }
    }

    fun selectPage(offset: Int) {
        loadReputation(currentLink.copy(offset = PageOffset(offset)))
    }

    fun setSort(order: Link.Board.Reputation.Order) {
        currentLink = currentLink.copy(order = order)
        loadReputation()
    }

    fun changeReputationMode() {
        val mode = when (currentLink.mode) {
            Link.Board.Reputation.History.Mode.From -> Link.Board.Reputation.History.Mode.To
            Link.Board.Reputation.History.Mode.To -> Link.Board.Reputation.History.Mode.From
        }
        loadReputation(currentLink.copy(mode = mode))
    }

    fun onItemClick(item: RepItem) {
        viewState.showItemDialogMenu(item)
    }

    fun onItemLongClick(item: RepItem) {
        viewState.showItemDialogMenu(item)
    }

    fun navigateToProfile(userId: UserId) {
        linkHandler.handle("https://4pda.to/forum/index.php?showuser=${userId.id}")
    }

    fun navigateToMessage(item: RepItem) {
        item.sourceUrl?.also { linkHandler.handle(it) }
    }
}
