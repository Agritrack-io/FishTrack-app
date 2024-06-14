package io.agritrack.philosofish.common;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import io.agritrack.philosofish.FishTrackApplication;

public class FileUtils {

    // format the date as part of the log file name
    private static final DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS");

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

    public static String saveCrashInfo2File(Throwable ex) {

        StringBuffer sb = new StringBuffer();
        sb.append(ex.getMessage());

        //Store error log in SD card, commonly used in development, problems uploading saved files to server
        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);

        Throwable cause = ex.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        printWriter.close();

        String result = writer.toString();
        sb.append(result);

        try {
            long timestamp = System.currentTimeMillis();
            String time = formatter.format(System.currentTimeMillis());
            String fileName = "crash-" + time + "-" + timestamp + ".log";
            File logPath = new File(FishTrackApplication.getAppContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "agriLogs");
            if (!logPath.exists()) {
                logPath.mkdirs();
            }

            FileOutputStream fos = new FileOutputStream(logPath + "/" + fileName);
            fos.write(sb.toString().getBytes());
            fos.close();

            return fileName;
        } catch (Exception e) {
            Log.e("FileUtils", "an error occurred while writing file...", e);
        }
        return null;
    }
}
