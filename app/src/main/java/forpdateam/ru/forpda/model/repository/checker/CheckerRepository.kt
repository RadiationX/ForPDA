package forpdateam.ru.forpda.model.repository.checker

import android.util.Log
import forpdateam.ru.forpda.entity.remote.checker.UpdateData
import forpdateam.ru.forpda.model.data.remote.api.checker.CheckerApi
import forpdateam.ru.forpda.model.data.storage.IPatternProvider
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Created by radiationx on 28.01.18.
 */
class CheckerRepository(
    private val checkerApi: CheckerApi,
    private val patternProvider: IPatternProvider
) {

    private val currentDataState = MutableStateFlow<UpdateData?>(null)

    suspend fun checkUpdate(force: Boolean = false): UpdateData {
        val updateData = if (!force && currentDataState.value != null)
            currentDataState.value!!
        else
            checkerApi.checkUpdate()
        Log.e(
            "kokos",
            "check version on updater ${updateData.patternsVersion} > ${patternProvider.getCurrentVersion()}"
        )
        if (updateData.patternsVersion > patternProvider.getCurrentVersion()) {
            val patterns = checkerApi.loadPatterns()
            patternProvider.update(patterns)
        }
        currentDataState.value = updateData
        return updateData
    }

}
