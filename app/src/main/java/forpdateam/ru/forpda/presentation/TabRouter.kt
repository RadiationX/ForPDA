package forpdateam.ru.forpda.presentation

import com.github.terrakok.cicerone.Router
import forpdateam.ru.forpda.ui.navigation.SystemMessage

class TabRouter : Router() {

    fun showSystemMessage(message: String) {
        executeCommands(SystemMessage(message))
    }
}