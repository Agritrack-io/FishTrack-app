package io.agritrack;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.Process;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import io.agritrack.fish.ui.WhMenuActivity;
import io.agritrack.ui.service.LocalPreferences;

public class CrashHandler implements Thread.UncaughtExceptionHandler {
    private static final String TAG = CrashHandler.class.getSimpleName();
    private static final CrashHandler INSTANCE = new CrashHandler();
    // format the date as part of the log file name
    private final DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS");
    //System default UncaughtException processing class
    private Thread.UncaughtExceptionHandler mDefaultHandler;
    //Context object for program
    private Context mContext;

    //Private construction method guarantees only one instance of CrashHandler
    private CrashHandler() {
    }

    // Get CrashHandler instance, singleton mode
    public static CrashHandler getInstance() {
        return INSTANCE;
    }

    /**
     * A.Initialization
     *
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
        saveCrashInfo2File(ex);
        // log the error in local terminal
        System.out.println("thread =" + ex.getMessage());
        System.out.println("ex =" + ex);

        // in case log file was not created, redirect to system default handler
        if (mDefaultHandler != null) {
            //Let the default exception handler of the system handle if the user does not handle it
            //mDefaultHandler.uncaughtException(thread, ex);
            restartApplication();
        }
    }

    private void restartApplication() {
        String product = LocalPreferences.getActivePRODUCT();
        Intent intent = new Intent();
        //[FISH, TOMATO, HOTEL, MILK]
        if (product.equalsIgnoreCase("FISH")) {
            intent = new Intent(this.mContext, WhMenuActivity.class);
        }
        intent.putExtra("crash", true);

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_CLEAR_TASK
                | Intent.FLAG_ACTIVITY_NEW_TASK);

        @SuppressLint("WrongConstant") PendingIntent pendingIntent = PendingIntent.getActivity(
                this.mContext, 0, intent, intent.getFlags());

        //Following code will restart your application after 0.5 seconds
        AlarmManager mgr = (AlarmManager) this.mContext.getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 500, pendingIntent);

        /*//This will finish your activity manually
        activity.finish();*/

        Process.killProcess(Process.myPid());

        //This will stop your application and take out from it.
        System.exit(2);
    }

    /**
     * Save error information to file
     *
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
}
