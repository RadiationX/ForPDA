package forpdateam.ru.forpda.model.repository.checker

import forpdateam.ru.forpda.entity.app.checker.UpdateData
import forpdateam.ru.forpda.model.data.remote.api.checker.CheckerApi
import forpdateam.ru.forpda.model.data.storage.PatternProvider
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

/**
 * Created by radiationx on 28.01.18.
 */
class CheckerRepository @Inject constructor(
    private val checkerApi: CheckerApi,
    private val patternProvider: PatternProvider
) {

    private val currentDataState = MutableStateFlow<UpdateData?>(null)

    suspend fun checkUpdate(force: Boolean = false): UpdateData {
        val updateData = if (!force && currentDataState.value != null) {
            currentDataState.value!!
        } else {
            checkerApi.checkUpdate()
        }
        currentDataState.value = updateData
        if (updateData.patternsVersion > (patternProvider.getVersion() ?: 0)) {
            patternProvider.setNeedsUpdate()
        }
        return updateData
    }

}
