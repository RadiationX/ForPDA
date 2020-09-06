package forpdateam.ru.forpda.ui.fragments

import android.os.Handler
import android.os.Looper

open class BaseJsInterface {
    private val handler = Handler(Looper.getMainLooper())
    protected fun runInUiThread(runnable: Runnable) = handler.post(runnable)
}