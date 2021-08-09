package forpdateam.ru.forpda.presentation.reputation

import moxy.InjectViewState
import forpdateam.ru.forpda.common.mvp.BasePresenter
import forpdateam.ru.forpda.entity.remote.reputation.RepData
import forpdateam.ru.forpda.entity.remote.reputation.RepItem
import forpdateam.ru.forpda.model.data.remote.api.reputation.ReputationApi
import forpdateam.ru.forpda.model.repository.avatar.AvatarRepository
import forpdateam.ru.forpda.model.repository.reputation.ReputationRepository
import forpdateam.ru.forpda.presentation.IErrorHandler
import forpdateam.ru.forpda.presentation.ILinkHandler
import forpdateam.ru.forpda.presentation.TabRouter

/**
 * Created by radiationx on 03.01.18.
 */

@InjectViewState
class ReputationPresenter(
        private val reputationRepository: ReputationRepository,
        private val avatarRepository: AvatarRepository,
        private val router: TabRouter,
        private val linkHandler: ILinkHandler,
        private val errorHandler: IErrorHandler
) : BasePresenter<ReputationView>() {

    var currentData = RepData()

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        loadReputation()
    }

    fun loadReputation() {
        reputationRepository
                .loadReputation(currentData.id, currentData.mode, currentData.sort, currentData.pagination.st)
                .doOnSubscribe { viewState.setRefreshing(true) }
                .doAfterTerminate { viewState.setRefreshing(false) }
                .subscribe({
                    currentData = it
                    viewState.showReputation(it)
                    tryShowAvatar(it)
                }, {
                    errorHandler.handle(it)
                })
                .untilDestroy()
    }

    fun changeReputation(type: Boolean, message: String) {
        reputationRepository
                .changeReputation(0, currentData.id, type, message)
                .doOnSubscribe { viewState.setRefreshing(true) }
                .doAfterTerminate { viewState.setRefreshing(false) }
                .subscribe({
                    viewState.onChangeReputation(it)
                    loadReputation()
                }, {
                    errorHandler.handle(it)
                })
                .untilDestroy()
    }

    private fun tryShowAvatar(data: RepData) {
        avatarRepository
                .getAvatar(data.nick.orEmpty())
                .subscribe({
                    viewState.showAvatar(it)
                }, {
                    errorHandler.handle(it)
                })
                .untilDestroy()
    }

    fun selectPage(page: Int) {
        currentData.pagination.st = page
        loadReputation()
    }

    fun setSort(sort: String) {
        currentData.sort = sort
        loadReputation()
    }

    fun changeReputationMode() {
        currentData.mode = if (currentData.mode == ReputationApi.MODE_FROM) ReputationApi.MODE_TO else ReputationApi.MODE_FROM
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
