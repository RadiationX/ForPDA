package forpdateam.ru.forpda.ui.fragments

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

open class BaseJsInterface {
    protected fun runInUiThread(runnable: Runnable) {
        GlobalScope.launch(Dispatchers.Main.immediate) {
            runnable.run()
        }
    }
}