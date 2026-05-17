package forpdateam.ru.forpda.presentation.checker

import android.util.Log
import forpdateam.ru.forpda.common.mvp.BasePresenter
import forpdateam.ru.forpda.extensions.coRunCatching
import forpdateam.ru.forpda.model.repository.checker.CheckerRepository
import forpdateam.ru.forpda.presentation.ErrorHandler
import kotlinx.coroutines.launch
import moxy.InjectViewState

/**
 * Created by radiationx on 28.01.18.
 */
@InjectViewState
class CheckerPresenter(
    private val checkerRepository: CheckerRepository,
    private val errorHandler: ErrorHandler
) : BasePresenter<CheckerView>() {

    var forceLoad = false

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        checkUpdate()
    }

    private fun checkUpdate() {
        Log.e("CHECKER", "checkUpdate presenter")
        viewModelScope.launch {
            viewState.setRefreshing(true)
            coRunCatching {
                checkerRepository.checkUpdate(forceLoad)
            }.onSuccess {
                Log.e("CHECKER", "SUBSC DATA $it")
                viewState.showUpdateData(it)
            }.onFailure {
                errorHandler.handle(it)
            }
            viewState.setRefreshing(false)
        }
    }
}