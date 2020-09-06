package forpdateam.ru.forpda.presentation.checker

import android.util.Log
import com.arellomobile.mvp.InjectViewState
import forpdateam.ru.forpda.common.mvp.BasePresenter
import forpdateam.ru.forpda.model.repository.checker.CheckerRepository
import forpdateam.ru.forpda.presentation.IErrorHandler

/**
 * Created by radiationx on 28.01.18.
 */
@InjectViewState
class CheckerPresenter(
        private val checkerRepository: CheckerRepository,
        private val errorHandler: IErrorHandler
) : BasePresenter<CheckerView>() {

    var forceLoad = false

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        checkUpdate()
    }

    private fun checkUpdate() {
        Log.e("CHECKER", "checkUpdate presenter")
        checkerRepository
                .checkUpdate(forceLoad)
                .doOnSubscribe { viewState.setRefreshing(true) }
                .doFinally { viewState.setRefreshing(false) }
                .subscribe({
                    Log.e("CHECKER", "SUBSC DATA " + it)
                    viewState.showUpdateData(it)
                }, {
                    errorHandler.handle(it)
                })
                .untilDestroy()
    }
}