package forpdateam.ru.forpda.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.util.Pair;
import android.util.Log;
import android.webkit.WebView;

import forpdateam.ru.forpda.App;
import forpdateam.ru.forpda.R;
import forpdateam.ru.forpda.imageviewer.ImageViewerActivity;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

/**
 * Created by radiationx on 01.11.16.
 */

public class DialogsHelper {
    private static DynamicDialogMenu<Context, Pair<String, String>> dynamicDialogMenu;
    private final static String openNewTab = App.get().getString(R.string.wv_open_new_tab);
    private final static String openBrowser = App.get().getString(R.string.wv_open_in_browser);
    private final static String copyUrl = App.get().getString(R.string.wv_copy_link);
    private final static String openImage = App.get().getString(R.string.wv_open_image);
    private final static String saveImage = App.get().getString(R.string.wv_save_image);
    private final static String copyImageUrl = App.get().getString(R.string.wv_copy_image_link);

    public static void handleContextMenu(Context context, int type, String extra, String nodeHref) {
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
            image = !extra.contains("4pda.ru/forum/style_images");
        if (!anchor && !image)
            return;

        if (dynamicDialogMenu == null) {
            dynamicDialogMenu = new DynamicDialogMenu<>();

            dynamicDialogMenu.addItem(openNewTab, (context1, data) -> IntentHandler.handle(data.second));
            dynamicDialogMenu.addItem(openBrowser, (context1, data) -> App.get().startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse(data.second)).addFlags(FLAG_ACTIVITY_NEW_TASK)));
            dynamicDialogMenu.addItem(copyUrl, (context1, data) -> Utils.copyToClipBoard(data.second));
            dynamicDialogMenu.addItem(openImage, (context1, data) -> ImageViewerActivity.startActivity(context1, data.first));
            dynamicDialogMenu.addItem(saveImage, (context1, data) -> IntentHandler.handleDownload(data.second));
            dynamicDialogMenu.addItem(copyImageUrl, (context1, data) -> Utils.copyToClipBoard(data.first));
        }
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
