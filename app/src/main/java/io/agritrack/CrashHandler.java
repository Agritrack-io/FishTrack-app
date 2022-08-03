package io.agritrack;

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

public class CrashHandler implements Thread.UncaughtExceptionHandler {
    private static final String TAG = CrashHandler.class.getSimpleName();

    //System default UncaughtException processing class
    private Thread.UncaughtExceptionHandler mDefaultHandler;
    private static final CrashHandler INSTANCE = new CrashHandler();

    //Context object for program
    private Context mContext;

    // format the date as part of the log file name
    private final DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS");

    //Private construction method guarantees only one instance of CrashHandler
    private CrashHandler() {
    }

    // Get CrashHandler instance, singleton mode
    public static CrashHandler getInstance() {
        return INSTANCE;
    }

    /**
     * A.Initialization
     * @param context
     */
    public void init(Context context) {
        mContext = context;

        //Get the system default UncaughtException processor
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();

        //Set this CrashHandler as the program's default processor
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    /**
     * B.When UncaughtException occurs, it goes into the function to handle it
     */
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        System.out.println("thread =" + ex.getMessage());
        System.out.println("ex =" + ex);

        // in case log file was not created, redirect to system default handler
        if (saveCrashInfo2File(ex) == null && mDefaultHandler != null) {
            //Let the default exception handler of the system handle if the user does not handle it
            mDefaultHandler.uncaughtException(thread, ex);
        }
        mDefaultHandler.uncaughtException(thread, ex);
    }

    /**
     * Save error information to file
     * @param ex
     * @return Return file name for easy file transfer to server
     */
    private String saveCrashInfo2File(Throwable ex) {

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
            File logPath = new File(this.mContext.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "agriLogs");
            if (!logPath.exists()) {
                logPath.mkdirs();
            }

            FileOutputStream fos = new FileOutputStream(logPath + "/" + fileName);
            fos.write(sb.toString().getBytes());
            fos.close();

            return fileName;
        } catch (Exception e) {
            Log.e(TAG, "an error occurred while writing file...", e);
        }
        return null;
    }

    /**
     * Upload log to server
     */
    public void uploadLogToServer() {
        //Traverse the crash folder in the sd card to get each file
        File file = new File(this.mContext.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "agriLogs");
        File[] files = file.listFiles();

        for (File f : files) {
            //Upload file using okhttp post

            //Delete the uploaded file crash folder
        }
    }
}
