package forpdateam.ru.forpda.client;

import android.os.Build;
import androidx.annotation.RequiresApi;

/**
 * Created by radiationx on 02.12.16.
 */

public class OnlyShowException extends Exception {
    public OnlyShowException() {
        super();
    }

    public OnlyShowException(String message) {
        super(message);
    }

    public OnlyShowException(String message, Throwable cause) {
        super(message, cause);
    }

    public OnlyShowException(Throwable cause) {
        super(cause);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    protected OnlyShowException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
