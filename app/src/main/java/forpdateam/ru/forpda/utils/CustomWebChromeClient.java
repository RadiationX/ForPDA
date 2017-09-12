package forpdateam.ru.forpda.utils;

import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;

/**
 * Created by radiationx on 12.09.17.
 */

public class CustomWebChromeClient extends WebChromeClient {
    private final static String CONSOLE_TAG = "WebConsole";
    @Override
    public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
        String message = "";
        message += "\"" + consoleMessage.message() + "\"";
        String source = consoleMessage.sourceId();
        if (source != null) {
            int cut = source.lastIndexOf('/');
            if (cut != -1) {
                source = source.substring(cut + 1);
            }
            message += ", [" + source + "]";
        }

        message += ", (" + consoleMessage.lineNumber() + ")";


        ConsoleMessage.MessageLevel level = consoleMessage.messageLevel();
        if (level == ConsoleMessage.MessageLevel.DEBUG) {
            Log.d(CONSOLE_TAG, message);
        } else if (level == ConsoleMessage.MessageLevel.ERROR) {
            Log.d(CONSOLE_TAG, message);
        } else if (level == ConsoleMessage.MessageLevel.WARNING) {
            Log.w(CONSOLE_TAG, message);
        } else if (level == ConsoleMessage.MessageLevel.LOG || level == ConsoleMessage.MessageLevel.TIP) {
            Log.i(CONSOLE_TAG, message);
        }
        return true;
    }
}
