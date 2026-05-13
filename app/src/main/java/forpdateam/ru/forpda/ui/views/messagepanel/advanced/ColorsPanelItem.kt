package forpdateam.ru.forpda.ui.views.messagepanel.advanced

import android.annotation.SuppressLint
import android.content.Context
import forpdateam.ru.forpda.ui.views.messagepanel.MessagePanel
import forpdateam.ru.forpda.ui.views.messagepanel.advanced.adapters.PanelItemAdapter
import java.util.Locale

/**
 * Created by radiationx on 08.01.17.
 */
@SuppressLint("ViewConstructor")
class ColorsPanelItem(
    context: Context,
    panel: MessagePanel,
    colors: List<Int>,
    listener: (PanelListItem.Color) -> Unit,
    title: String
) : BasePanelItem(context, panel, title) {

    init {
        val colorItems = colors.map { color ->
            var hexColor = Integer.toHexString(color).uppercase(Locale.getDefault())
            if (hexColor.length > 6) {
                hexColor = hexColor.substring(2)
            }

            PanelListItem.Color(
                "#$hexColor",
                color
            )
        }
        val adapter = PanelItemAdapter(colorItems.toMutableList()) {
            if (it is PanelListItem.Color) {
                listener.invoke(it)
            }
        }
        recyclerView.adapter = adapter
    }
}
