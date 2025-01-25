package forpdateam.ru.forpda.common.simple

import java.util.Observable

/**
 * Created by radiationx on 28.05.17.
 */
class SimpleObservable : Observable() {
    @Synchronized
    override fun hasChanged(): Boolean {
        return true
    }
}
