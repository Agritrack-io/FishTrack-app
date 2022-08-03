package io.agritrack;

import static io.agritrack.common.LargeString.render;
import static io.agritrack.ui.custom.CustomToast.CToast;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.SocketTimeoutException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import io.agritrack.api.APIServiceGenerator;
import io.agritrack.api.upload.UploadingApi;
import io.agritrack.common.FileUtils;
import io.agritrack.hotel.ui.inventory.HotelInventoryLinenActivity;
import io.agritrack.ui.service.LocalPreferences;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
        saveCrashInfo2File(ex);
        // in case log file was not created, redirect to system default handler
        if (mDefaultHandler != null) {
            //Let the default exception handler of the system handle if the user does not handle it
            mDefaultHandler.uncaughtException(thread, ex);
        }
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
}
