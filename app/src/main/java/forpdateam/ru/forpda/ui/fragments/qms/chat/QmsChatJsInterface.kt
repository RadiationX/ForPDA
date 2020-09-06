package forpdateam.ru.forpda.ui.fragments.qms.chat

import android.webkit.JavascriptInterface
import forpdateam.ru.forpda.presentation.qms.chat.IQmsChatPresenter
import forpdateam.ru.forpda.ui.fragments.BaseJsInterface

class QmsChatJsInterface(
        private val presenter: IQmsChatPresenter
) : BaseJsInterface() {

    @JavascriptInterface
    fun loadMoreMessages() = runInUiThread(Runnable { presenter.loadMoreMessages() })

}