package forpdateam.ru.forpda.presentation.reputation

import forpdateam.ru.forpda.common.mvp.BasePresenter
import forpdateam.ru.forpda.entity.remote.reputation.RepArgs
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

/**
 * Created by radiationx on 03.01.18.
 */

@InjectViewState
class ReputationPresenter(
    private val reputationRepository: ReputationRepository,
    private val avatarRepository: AvatarRepository,
    private val router: TabRouter,
    private val linkHandler: LinkHandler,
    private val errorHandler: ErrorHandler
) : BasePresenter<ReputationView>() {

    lateinit var currentArgs: RepArgs
    var currentData: RepData? = null

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        loadReputation(currentArgs.initialSt)
    }

    fun loadReputation(page: Int? = null) {
        viewModelScope.launch {
            viewState.setRefreshing(true)
            coRunCatching {
                reputationRepository.loadReputation(
                    currentArgs.userId,
                    currentArgs.mode,
                    currentArgs.sort,
                    page ?: currentData?.pagination?.currentPage() ?: currentArgs.initialSt
                )
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
                reputationRepository.changeReputation(0, currentArgs.userId, type, message)
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

    fun selectPage(page: Int) {
        loadReputation(page)
    }

    fun setSort(sort: String) {
        currentArgs = currentArgs.copy(sort = sort)
        loadReputation()
    }

    fun changeReputationMode() {
        val mode = if (currentArgs.mode == RepArgs.MODE_FROM) {
            RepArgs.MODE_TO
        } else {
            RepArgs.MODE_FROM
        }
        currentArgs = currentArgs.copy(mode = mode)
        loadReputation()
    }

    fun onItemClick(item: RepItem) {
        viewState.showItemDialogMenu(item)
    }

    fun onItemLongClick(item: RepItem) {
        viewState.showItemDialogMenu(item)
    }

    fun navigateToProfile(userId: Int) {
        linkHandler.handle("https://4pda.to/forum/index.php?showuser=$userId", router)
    }

    fun navigateToMessage(item: RepItem) {
        linkHandler.handle(item.sourceUrl, router)
    }
}
