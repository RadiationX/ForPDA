package forpdateam.ru.forpda.presentation.checker

import forpdateam.ru.forpda.common.mvp.IBaseView
import forpdateam.ru.forpda.entity.remote.checker.UpdateData

/**
 * Created by radiationx on 28.01.18.
 */
interface CheckerView : IBaseView {

    fun showUpdateData(update: UpdateData)
}