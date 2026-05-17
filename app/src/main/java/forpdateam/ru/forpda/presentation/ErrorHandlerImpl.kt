package forpdateam.ru.forpda.presentation

import javax.inject.Inject

/**
 * Created by radiationx on 23.02.18.
 */
class ErrorHandlerImpl @Inject constructor(
    private val router: TabRouter
) : ErrorHandler {

    override fun handle(throwable: Throwable, messageListener: ((Throwable, String?) -> Unit)?) {
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