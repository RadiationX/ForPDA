package forpdateam.ru.forpda.ui.navigation

import androidx.annotation.StringRes
import com.github.terrakok.cicerone.Command

sealed interface SystemMessage : Command {
    data class Text(val message: String) : SystemMessage
    data class Res(@param:StringRes val res: Int) : SystemMessage
}