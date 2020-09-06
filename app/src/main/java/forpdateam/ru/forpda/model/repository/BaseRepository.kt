package forpdateam.ru.forpda.model.repository

import forpdateam.ru.forpda.model.SchedulersProvider
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

open class BaseRepository(
        private val schedulers: SchedulersProvider
) {

    fun <T> Single<T>.runInIoToUi(): Single<T> = this
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun <T> Observable<T>.runInIoToUi(): Observable<T> = this
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())

    fun Completable.runInIoToUi(): Completable = this
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui())
}
