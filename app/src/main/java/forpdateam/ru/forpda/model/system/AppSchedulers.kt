package forpdateam.ru.forpda.model.system

import forpdateam.ru.forpda.model.SchedulersProvider
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * Created by radiationx on 01.01.18.
 */

class AppSchedulers : SchedulersProvider {
    override fun ui() = AndroidSchedulers.mainThread()
    override fun computation() = Schedulers.computation()
    override fun trampoline() = Schedulers.trampoline()
    override fun newThread() = Schedulers.newThread()
    override fun io() = Schedulers.io()
}
