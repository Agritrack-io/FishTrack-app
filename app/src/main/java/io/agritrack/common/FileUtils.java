package io.agritrack.common;

import android.content.Context;
import android.os.Environment;

import java.io.File;

import io.agritrack.fish.ui.FishHomeActivity;

public class FileUtils {
    public static boolean deleteInventoryFile(Context ctx, String fileName) {
        File fileToBeDeleted = new File(ctx.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName);
        if (fileToBeDeleted.exists()) {
            return fileToBeDeleted.delete();
        }
        return false;
    }

    public static boolean deletePhotoFile(Context ctx, String fileName) {
        File fileToBeDeleted = new File(ctx.getExternalFilesDir(Environment.DIRECTORY_PICTURES), fileName);
        if (fileToBeDeleted.exists()) {
            return fileToBeDeleted.delete();
        }
        return false;
    }

    public static boolean deleteCrashFile(Context ctx, String fileName) {
        File fileToBeDeleted = new File(ctx.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) + "/agriLogs", fileName);
        if (fileToBeDeleted.exists()) {
            return fileToBeDeleted.delete();
        }
        return false;
    }
}
