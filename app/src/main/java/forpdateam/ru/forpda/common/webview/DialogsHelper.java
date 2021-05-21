package forpdateam.ru.forpda.common.webview;

import android.content.Context;

import androidx.core.util.Pair;
import android.util.Log;
import android.webkit.WebView;

import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.common.Utils;
import forpdateam.ru.forpda.presentation.ILinkHandler;
import forpdateam.ru.forpda.presentation.ISystemLinkHandler;
import forpdateam.ru.forpda.presentation.TabRouter;
import forpdateam.ru.forpda.ui.activities.imageviewer.ImageViewerActivity;
import forpdateam.ru.forpda.ui.views.DynamicDialogMenu;

/**
 * Created by radiationx on 01.11.16.
 */

public class DialogsHelper {
    private DynamicDialogMenu<Context, Pair<String, String>> dynamicDialogMenu = new DynamicDialogMenu<>();

    public DialogsHelper(
            Context context,
            ILinkHandler linkHandler,
            ISystemLinkHandler systemLinkHandler,
            TabRouter router
    ) {
        String openNewTab = context.getString(R.string.wv_open_new_tab);
        String openBrowser = context.getString(R.string.wv_open_in_browser);
        String copyUrl = context.getString(R.string.wv_copy_link);
        String openImage = context.getString(R.string.wv_open_image);
        String saveImage = context.getString(R.string.wv_save_image);
        String copyImageUrl = context.getString(R.string.wv_copy_image_link);

        dynamicDialogMenu.addItem(openNewTab, (context1, data) -> linkHandler.handle(data.second, router));
        dynamicDialogMenu.addItem(openBrowser, (context1, data) -> systemLinkHandler.handle(data.second));
        dynamicDialogMenu.addItem(copyUrl, (context1, data) -> Utils.copyToClipBoard(data.second));
        dynamicDialogMenu.addItem(openImage, (context1, data) -> ImageViewerActivity.startActivity(context1, data.first));
        dynamicDialogMenu.addItem(saveImage, (context1, data) -> systemLinkHandler.handleDownload(data.second, null));
        dynamicDialogMenu.addItem(copyImageUrl, (context1, data) -> Utils.copyToClipBoard(data.first));
    }

    public void handleContextMenu(Context context, int type, String extra, String nodeHref) {
        Log.d("DialogsHelper", "handleContextMenu " + type + " : " + extra + " : " + nodeHref);
        if (type == WebView.HitTestResult.UNKNOWN_TYPE || type == WebView.HitTestResult.EDIT_TEXT_TYPE)
            return;
        type = type == WebView.HitTestResult.ANCHOR_TYPE ? WebView.HitTestResult.SRC_ANCHOR_TYPE : type;
        type = type == WebView.HitTestResult.IMAGE_ANCHOR_TYPE ? WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE : type;

        int index;
        boolean anchor = false, image = false;
        switch (type) {
            case WebView.HitTestResult.SRC_ANCHOR_TYPE:
                anchor = true;
                break;
            case WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE:
                anchor = true;
                image = true;
                break;
            case WebView.HitTestResult.IMAGE_TYPE:
                image = true;
                break;
        }

        if (image)
            image = !extra.contains("4pda.to/forum/style_images");
        if (!anchor && !image)
            return;

        dynamicDialogMenu.disallowAll();
        if (anchor) {
            dynamicDialogMenu.allow(0);
            dynamicDialogMenu.allow(1);
            dynamicDialogMenu.allow(2);
        }
        if (image) {
            dynamicDialogMenu.allow(3);
            dynamicDialogMenu.allow(4);
            dynamicDialogMenu.allow(5);
        }
        Pair<String, String> item = new Pair<>(extra, nodeHref == null ? extra : nodeHref);
        dynamicDialogMenu.show(context, context, item);
    }
}
