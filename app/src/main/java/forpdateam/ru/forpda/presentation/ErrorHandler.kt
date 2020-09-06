package forpdateam.ru.forpda.presentation

import android.content.Context

/**
 * Created by radiationx on 23.02.18.
 */
class ErrorHandler(
        private val router: TabRouter
) : IErrorHandler {

    override fun handle(throwable: Throwable,  messageListener: ((Throwable, String?) -> Unit)?) {
        throwable.printStackTrace()
        val message = getMessage(throwable)
        if (messageListener != null) {
            messageListener.invoke(throwable, message)
        } else {
            router.showSystemMessage(message)
        }
    }

    private fun getMessage(throwable: Throwable): String {
        return throwable.message.orEmpty()
    }
}