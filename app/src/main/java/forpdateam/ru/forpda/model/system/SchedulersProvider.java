package forpdateam.ru.forpda.model.system;

import io.reactivex.Scheduler;

/**
 * Created by radiationx on 01.01.18.
 */

public interface SchedulersProvider {
    Scheduler ui();

    Scheduler computation();

    Scheduler trampoline();

    Scheduler newThread();

    Scheduler io();
}
