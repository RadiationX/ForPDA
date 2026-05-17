package forpdateam.ru.forpda.presentation

import androidx.annotation.StringRes
import com.github.terrakok.cicerone.Router
import forpdateam.ru.forpda.ui.navigation.SystemMessage

class TabRouter : Router() {

    fun showSystemMessage(message: String) {
        executeCommands(SystemMessage.Text(message))
    }

    fun showSystemMessage(@StringRes res: Int) {
        executeCommands(SystemMessage.Res(res))
    }
}