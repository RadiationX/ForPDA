package forpdateam.ru.forpda.ui.views.messagepanel.advanced

import android.annotation.SuppressLint
import android.content.Context
import android.widget.FrameLayout
import forpdateam.ru.forpda.ui.views.messagepanel.AutoFitRecyclerView
import forpdateam.ru.forpda.ui.views.messagepanel.MessagePanel

/**
 * Created by radiationx on 08.01.17.
 */
@SuppressLint("ViewConstructor")
open class BasePanelItem(
    context: Context,
    @JvmField protected var messagePanel: MessagePanel,
    val title: String
) : FrameLayout(context) {
    @JvmField
    protected var recyclerView: AutoFitRecyclerView = AutoFitRecyclerView(context)

    init {
        addView(recyclerView)
    }
}
