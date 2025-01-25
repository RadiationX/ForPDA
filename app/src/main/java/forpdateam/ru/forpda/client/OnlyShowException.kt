package forpdateam.ru.forpda.client

import android.os.Build
import androidx.annotation.RequiresApi

/**
 * Created by radiationx on 02.12.16.
 */
class OnlyShowException : Exception {
    constructor() : super()

    constructor(message: String?) : super(message)

    constructor(message: String?, cause: Throwable?) : super(message, cause)

    constructor(cause: Throwable?) : super(cause)

    @RequiresApi(api = Build.VERSION_CODES.N)
    protected constructor(
        message: String?,
        cause: Throwable?,
        enableSuppression: Boolean,
        writableStackTrace: Boolean
    ) : super(message, cause, enableSuppression, writableStackTrace)
}
