package forpdateam.ru.forpda.extensions

import moxy.MvpAppCompatActivity
import moxy.MvpAppCompatFragment
import moxy.MvpDelegate
import moxy.MvpPresenter
import moxy.ktx.MoxyKtxDelegate
import ru.radiationx.quill.QuillExtra
import ru.radiationx.quill.QuillModule
import ru.radiationx.quill.QuillScope
import ru.radiationx.quill.getScope
import kotlin.reflect.KClass

inline fun <reified T : MvpPresenter<*>> MvpAppCompatFragment.quillMoxyPresenter(
    noinline extraProvider: (() -> QuillExtra)? = null
): MoxyKtxDelegate<T> {
    return mvpDelegate.createMoxyDelegate(T::class, getScope(), extraProvider)
}

inline fun <reified T : MvpPresenter<*>> MvpAppCompatActivity.quillMoxyPresenter(
    noinline extraProvider: (() -> QuillExtra)? = null
): MoxyKtxDelegate<T> {
    return mvpDelegate.createMoxyDelegate(T::class, getScope(), extraProvider)
}

fun <T : MvpPresenter<*>> MvpDelegate<*>.createMoxyDelegate(
    clazz: KClass<T>,
    scope: QuillScope,
    extraProvider: (() -> QuillExtra)?
): MoxyKtxDelegate<T> {
    return MoxyKtxDelegate(this, "${clazz.java.name}.presenter", {
        scope.apply {
            if (extraProvider != null) {
                val module = QuillModule().apply {
                    instanceAsIs(extraProvider.invoke())
                }
                installModules(module)
            }
        }.get(clazz)
    })
}