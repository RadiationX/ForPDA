package forpdateam.ru.forpda.presentation

import ru.terrakok.cicerone.Router

class TabRouter : Router() {
    companion object {
        private const val errorMessage = "Use methods with class Screen instead screenKey"
    }

    fun newScreenChain(screen: Screen) {
        super.newScreenChain(screen.getKey(), screen)
    }

    fun navigateTo(screen: Screen) {
        super.navigateTo(screen.getKey(), screen)
    }

    fun backTo(screen: Screen) {
        super.backTo(screen.getKey())
    }

    fun replaceScreen(screen: Screen) {
        super.replaceScreen(screen.getKey(), screen)
    }

    fun newRootScreen(screen: Screen) {
        super.newRootScreen(screen.getKey(), screen)
    }

    @Deprecated(errorMessage, level = DeprecationLevel.ERROR)
    override fun newScreenChain(screenKey: String) {
        throw Exception(errorMessage)
    }

    @Deprecated(errorMessage, level = DeprecationLevel.ERROR)
    override fun newScreenChain(screenKey: String, data: Any?) {
        throw Exception(errorMessage)
    }

    @Deprecated(errorMessage, level = DeprecationLevel.ERROR)
    override fun navigateTo(screenKey: String) {
        throw Exception(errorMessage)
    }

    @Deprecated(errorMessage, level = DeprecationLevel.ERROR)
    override fun navigateTo(screenKey: String, data: Any?) {
        throw Exception(errorMessage)
    }

    @Deprecated(errorMessage, level = DeprecationLevel.ERROR)
    override fun backTo(screenKey: String) {
        throw Exception(errorMessage)
    }

    @Deprecated(errorMessage, level = DeprecationLevel.ERROR)
    override fun replaceScreen(screenKey: String) {
        throw Exception(errorMessage)
    }

    @Deprecated(errorMessage, level = DeprecationLevel.ERROR)
    override fun replaceScreen(screenKey: String, data: Any?) {
        throw Exception(errorMessage)
    }

    @Deprecated(errorMessage, level = DeprecationLevel.ERROR)
    override fun newRootScreen(screenKey: String) {
        throw Exception(errorMessage)
    }

    @Deprecated(errorMessage, level = DeprecationLevel.ERROR)
    override fun newRootScreen(screenKey: String, data: Any?) {
        throw Exception(errorMessage)
    }
}