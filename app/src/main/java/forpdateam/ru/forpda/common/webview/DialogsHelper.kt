package forpdateam.ru.forpda.common.webview

import android.content.Context
import android.util.Log
import android.webkit.WebView
import androidx.core.util.Pair
import forpdateam.ru.forpda.R
import forpdateam.ru.forpda.common.Utils.copyToClipBoard
import forpdateam.ru.forpda.presentation.ILinkHandler
import forpdateam.ru.forpda.presentation.ISystemLinkHandler
import forpdateam.ru.forpda.presentation.TabRouter
import forpdateam.ru.forpda.ui.activities.imageviewer.ImageViewerActivity.Companion.startActivity
import forpdateam.ru.forpda.ui.views.DynamicDialogMenu

/**
 * Created by radiationx on 01.11.16.
 */
class DialogsHelper(
    context: Context,
    linkHandler: ILinkHandler,
    systemLinkHandler: ISystemLinkHandler,
    router: TabRouter?
) {
    private val dynamicDialogMenu = DynamicDialogMenu<Context, Pair<String, String>>()

    init {
        val openNewTab = context.getString(R.string.wv_open_new_tab)
        val openBrowser = context.getString(R.string.wv_open_in_browser)
        val copyUrl = context.getString(R.string.wv_copy_link)
        val openImage = context.getString(R.string.wv_open_image)
        val saveImage = context.getString(R.string.wv_save_image)
        val copyImageUrl = context.getString(R.string.wv_copy_image_link)

        dynamicDialogMenu.addItem(
            openNewTab
        ) { context1: Context?, data: Pair<String, String> ->
            linkHandler.handle(
                data.second,
                router
            )
        }
        dynamicDialogMenu.addItem(
            openBrowser
        ) { context1: Context?, data: Pair<String, String> ->
            systemLinkHandler.handle(
                data.second
            )
        }
        dynamicDialogMenu.addItem(
            copyUrl
        ) { context1: Context?, data: Pair<String, String> ->
            copyToClipBoard(
                data.second
            )
        }
        dynamicDialogMenu.addItem(
            openImage
        ) { context1: Context?, data: Pair<String, String> ->
            startActivity(
                context1!!, data.first
            )
        }
        dynamicDialogMenu.addItem(
            saveImage
        ) { context1: Context?, data: Pair<String, String> ->
            systemLinkHandler.handleDownload(
                data.second,
                null
            )
        }
        dynamicDialogMenu.addItem(
            copyImageUrl
        ) { context1: Context?, data: Pair<String, String> ->
            copyToClipBoard(
                data.first
            )
        }
    }

    fun handleContextMenu(context: Context, type: Int, extra: String, nodeHref: String?) {
        var type = type
        Log.d("DialogsHelper", "handleContextMenu $type : $extra : $nodeHref")
        if (type == WebView.HitTestResult.UNKNOWN_TYPE || type == WebView.HitTestResult.EDIT_TEXT_TYPE) return
        type =
            if (type == WebView.HitTestResult.ANCHOR_TYPE) WebView.HitTestResult.SRC_ANCHOR_TYPE else type
        type =
            if (type == WebView.HitTestResult.IMAGE_ANCHOR_TYPE) WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE else type

        var index: Int
        var anchor = false
        var image = false
        when (type) {
            WebView.HitTestResult.SRC_ANCHOR_TYPE -> anchor = true
            WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE -> {
                anchor = true
                image = true
            }

            WebView.HitTestResult.IMAGE_TYPE -> image = true
        }

        if (image) image = !extra.contains("4pda.to/forum/style_images")
        if (!anchor && !image) return

        dynamicDialogMenu.disallowAll()
        if (anchor) {
            dynamicDialogMenu.allow(0)
            dynamicDialogMenu.allow(1)
            dynamicDialogMenu.allow(2)
        }
        if (image) {
            dynamicDialogMenu.allow(3)
            dynamicDialogMenu.allow(4)
            dynamicDialogMenu.allow(5)
        }
        val item = Pair(extra, nodeHref ?: extra)
        dynamicDialogMenu.show(context, context, item)
    }
}
