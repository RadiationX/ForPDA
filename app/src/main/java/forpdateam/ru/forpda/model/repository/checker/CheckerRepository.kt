package forpdateam.ru.forpda.model.repository.checker

import android.util.Log
import com.jakewharton.rxrelay2.BehaviorRelay
import forpdateam.ru.forpda.entity.remote.checker.UpdateData
import forpdateam.ru.forpda.model.SchedulersProvider
import forpdateam.ru.forpda.model.data.remote.api.checker.CheckerApi
import forpdateam.ru.forpda.model.data.storage.IPatternProvider
import io.reactivex.Single

/**
 * Created by radiationx on 28.01.18.
 */
class CheckerRepository(
        private val schedulers: SchedulersProvider,
        private val checkerApi: CheckerApi,
        private val patternProvider: IPatternProvider
) {

    private val currentDataRelay = BehaviorRelay.create<UpdateData>()

    fun checkUpdate(force: Boolean = false): Single<UpdateData> = Single
            .fromCallable {
                return@fromCallable if (!force && currentDataRelay.hasValue())
                    currentDataRelay.value
                else
                    checkerApi.checkUpdate()
            }
            .map { updateData ->
                Log.e("kokos", "check version on updater ${updateData.patternsVersion} > ${patternProvider.getCurrentVersion()}")
                if (updateData.patternsVersion > patternProvider.getCurrentVersion()) {
                    val patterns = checkerApi.loadPatterns()
                    patternProvider.update(patterns)
                }
                updateData
            }
            .doOnSuccess {
                currentDataRelay.accept(it)
            }
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

}
