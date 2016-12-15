package forpdateam.ru.forpda.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static forpdateam.ru.forpda.utils.Utils.log;

/**
 * Created by isanechek on 15.12.16.
 */

public class AssetsUtils {

    private static final String TAG = "AssetsUtils";

    public static void startCopyFromAssets(Context ctx) {
        log(TAG + " startCopyFromAssets start");
        AssetManager manager = ctx.getAssets();
        try {
            if (copyAssets(manager, "", ctx.getFilesDir()))
                log(TAG + " startCopyFromAssets ->> Finish! All good.");
            else
                log(TAG + " startCopyFromAssets ->> Ooopss. Finish with unknown error! ");
        } catch (IOException ignored) {
            log(TAG + " startCopyFromAssets IOError ->> " + ignored.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            log(TAG + " startCopyFromAssets Error ->> " + e.getMessage());
        }
    }

    private static boolean copyAssets(AssetManager assetManager, String path,
                                      File targetFolder) throws Exception {
        Log.i(TAG, "Copying " + path + " to " + targetFolder);
        String sources[] = assetManager.list(path);
        if (sources.length == 0) { // its not a folder, so its a file:
            copyAssetFileToFolder(assetManager, path, targetFolder);
        } else { // its a folder:
            if (path.startsWith("images") || path.startsWith("sounds")
                    || path.startsWith("webkit")) {
                Log.i(TAG, "  > Skipping " + path);
                return false;
            }
            File targetDir = new File(targetFolder, path);
            targetDir.mkdirs();
            for (String source : sources) {
                String fullSourcePath = path.equals("") ? source : (path
                        + File.separator + source);
                copyAssets(assetManager, fullSourcePath, targetFolder);
            }
        }
        return true;
    }

    private static void copyAssetFileToFolder(AssetManager assetManager, String fullAssetPath,
                                              File targetBasePath) throws IOException {
        InputStream in = assetManager.open(fullAssetPath);
        OutputStream out = new FileOutputStream(new File(targetBasePath,
                fullAssetPath));
        byte[] buffer = new byte[16 * 1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
        in.close();
        out.flush();
        out.close();
    }
}
