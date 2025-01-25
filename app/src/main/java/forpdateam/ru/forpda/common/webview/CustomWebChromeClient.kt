package forpdateam.ru.forpda.common.webview

import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.ConsoleMessage.MessageLevel
import android.webkit.WebChromeClient

/**
 * Created by radiationx on 12.09.17.
 */
open class CustomWebChromeClient : WebChromeClient() {
    override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
        var message = ""
        message += "\"" + consoleMessage.message() + "\""
        var source = consoleMessage.sourceId()
        if (source != null) {
            val cut = source.lastIndexOf('/')
            if (cut != -1) {
                source = source.substring(cut + 1)
            }
            message += ", [$source]"
        }

        message += ", (" + consoleMessage.lineNumber() + ")"


        val level = consoleMessage.messageLevel()
        if (level == MessageLevel.DEBUG) {
            Log.d(CONSOLE_TAG, message)
        } else if (level == MessageLevel.ERROR) {
            Log.e(CONSOLE_TAG, message)
        } else if (level == MessageLevel.WARNING) {
            Log.w(CONSOLE_TAG, message)
        } else if (level == MessageLevel.LOG || level == MessageLevel.TIP) {
            Log.i(CONSOLE_TAG, message)
        } else {
            Log.d(CONSOLE_TAG, message)
        }
        return true
    }

    companion object {
        private const val CONSOLE_TAG = "WebConsole"
    }
}
