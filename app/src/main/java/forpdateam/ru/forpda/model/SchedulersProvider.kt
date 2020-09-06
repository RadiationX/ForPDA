package forpdateam.ru.forpda.model

import io.reactivex.Scheduler

/**
 * Created by radiationx on 01.01.18.
 */

interface SchedulersProvider {
    fun ui(): Scheduler
    fun computation(): Scheduler
    fun trampoline(): Scheduler
    fun newThread(): Scheduler
    fun io(): Scheduler
}
