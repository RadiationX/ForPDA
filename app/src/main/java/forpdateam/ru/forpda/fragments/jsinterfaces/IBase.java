package forpdateam.ru.forpda.fragments.jsinterfaces;

import android.webkit.JavascriptInterface;

/**
 * Created by radiationx on 28.05.17.
 */

public interface IBase {
    String JS_BASE_INTERFACE = "IBase";

    @JavascriptInterface
    void playClickEffect();
}
