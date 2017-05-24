package forpdateam.ru.forpda.utils;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;
import android.webkit.MimeTypeMap;

import org.acra.ACRA;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import forpdateam.ru.forpda.api.RequestFile;

/**
 * Created by radiationx on 13.01.17.
 */

public class FilePickHelper {

    public static Intent pickImage(boolean onlyImages) {
        /*Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image*//*");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        }
        startActivityForResult(intent, PICK_IMAGE);*/


        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        if (onlyImages) {
            intent.setType("image/*");
        } else {
            intent.setType("*/*");
        }
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        return Intent.createChooser(intent, "Select file");
    }

    public static List<RequestFile> onActivityResult(Context context, Intent data) {
        List<RequestFile> files = new ArrayList<>();
        RequestFile tempFile;
        Log.e("FORPDA_LOG", "ON ACTIVITY RESULT INTENT " + data);
        if (data.getData() == null) {
            if (data.getClipData() != null) {
                for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                    tempFile = createFile(context, data.getClipData().getItemAt(i).getUri());
                    if (tempFile != null) files.add(tempFile);
                }
            }
        } else {
            tempFile = createFile(context, data.getData());
            if (tempFile != null) files.add(tempFile);
        }
        return files;
    }

    private final static Pattern extensionPattern = Pattern.compile("[\\s\\S]*\\.([\\s\\S]*)");

    private static String getExtension(String name) {
        String extension = null;
        if (name != null) {
            Matcher matcher = extensionPattern.matcher(name);
            if (matcher.find()) {
                extension = matcher.group(1);
            }
        }
        return extension;
    }

    private static RequestFile createFile(Context context, Uri uri) {
        RequestFile requestFile = null;
        Log.e("FORPDA_LOG", "CREATE FILE " + uri);
        try {
            InputStream inputStream = null;
            String name = getFileName(context, uri);
            String extension = getExtension(name);
            String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
            if (mimeType == null) {
                mimeType = context.getContentResolver().getType(uri);
            }
            if (mimeType == null) {
                mimeType = MimeTypeUtil.getType(extension);
            }
            if (uri.getScheme().equals("content")) {
                inputStream = context.getContentResolver().openInputStream(uri);
            } else if (uri.getScheme().equals("file")) {
                inputStream = new FileInputStream(new File(uri.getPath()));
            }
            requestFile = new RequestFile(name, mimeType, inputStream);
        } catch (Exception e) {
            ACRA.getErrorReporter().handleException(e);
        }
        return requestFile;
    }

    private static String getFileName(Context context, Uri uri) {
        Log.d("FORPDA_LOG", uri.getScheme() + " : " + context.getContentResolver().getType(uri));
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index >= 0) {
                        result = cursor.getString(index);
                    }
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        if (result == null) {
            Log.d("FORPDA_LOG", "res " + uri.getPath());
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }
}
