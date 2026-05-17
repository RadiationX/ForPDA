package forpdateam.ru.forpda.ui

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.Log
import biz.source_code.miniTemplator.MiniTemplator
import forpdateam.ru.forpda.R.string
import forpdateam.ru.forpda.common.apptheme.AppTheme
import forpdateam.ru.forpda.common.apptheme.AppThemeController
import forpdateam.ru.forpda.common.simple.SimpleActivityLifecycleCallbacks
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.radiationx.quill.get
import java.io.ByteArrayInputStream
import java.nio.charset.Charset
import javax.inject.Inject

class TemplateManager @Inject constructor(
    private val application: Application,
    private val appThemeController: AppThemeController
) {

    companion object {
        const val TEMPLATE_THEME = "theme"
        const val TEMPLATE_SEARCH = "search"
        const val TEMPLATE_QMS_CHAT = "qms_chat"
        const val TEMPLATE_QMS_CHAT_MESS = "qms_chat_mess"
        const val TEMPLATE_NEWS = "news"
        const val TEMPLATE_FORUM_RULES = "forum_rules"
        const val TEMPLATE_ANNOUNCE = "announce"
    }

    private val staticStrings = mutableMapOf<String, String>()
    private val templates = mutableMapOf<String, MiniTemplator>()

    init {
        updateStaticRes(application)
        application.registerActivityLifecycleCallbacks(object : SimpleActivityLifecycleCallbacks() {
            override fun onActivityPreCreated(activity: Activity, savedInstanceState: Bundle?) {
                updateStaticRes(activity)
            }
        })
    }

    fun observeThemeType(): Flow<String> = appThemeController
        .observeTheme()
        .map {
            when (it) {
                AppTheme.LIGHT -> "light"
                AppTheme.DARK -> "dark"
            }
        }

    fun getThemeType(): String {
        return when (appThemeController.getTheme()) {
            AppTheme.LIGHT -> "light"
            AppTheme.DARK -> "dark"
        }
    }

    fun fillStaticStrings(template: MiniTemplator): MiniTemplator = template.apply {
        variables.forEach { entry ->
            staticStrings[entry.key]?.let {
                setVariable(entry.key, it)
            }
        }
    }

    fun getTemplate(name: String): MiniTemplator = templates[name]
        ?: findTemplate(name).apply { templates[name] = this }

    private fun findTemplate(name: String): MiniTemplator = try {
        val stream = application.assets.open("template_$name.html")
        MiniTemplator.Builder().build(stream, Charset.forName("utf-8"))
    } catch (ex: Exception) {
        ex.printStackTrace()
        MiniTemplator.Builder().build(
            ByteArrayInputStream("Template error!".toByteArray(Charset.forName("utf-8"))),
            Charset.forName("utf-8")
        )
    }

    private fun updateStaticRes(context: Context) {
        Log.e("kekosina", "updateStaticRes")
        val templateStringCache = HashMap<String, String>()
        for (f in string::class.java.fields) {
            try {
                if (f.name.startsWith("res_s_")) {
                    templateStringCache[f.name] = context.getString(f.getInt(f))
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
        staticStrings.clear()
        staticStrings.putAll(templateStringCache)
    }

}