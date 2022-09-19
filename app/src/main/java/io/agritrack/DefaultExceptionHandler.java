package io.agritrack;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import io.agritrack.fish.ui.FishHomeActivity;

public class DefaultExceptionHandler implements Thread.UncaughtExceptionHandler{
    private Thread.UncaughtExceptionHandler defaultUEH;
    Activity activity;

    public DefaultExceptionHandler(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {

        Intent intent = new Intent(activity, FishHomeActivity.class);

        intent.putExtra("crash",true);

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_CLEAR_TASK
                | Intent.FLAG_ACTIVITY_NEW_TASK);

        @SuppressLint("WrongConstant") PendingIntent pendingIntent = PendingIntent.getActivity(
                activity.getBaseContext(), 0, intent, intent.getFlags());

        //Following code will restart your application after 0.5 seconds
        AlarmManager mgr = (AlarmManager) activity.getBaseContext()
                .getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1000,
                pendingIntent);

        //This will finish your activity manually
        activity.finish();

        //This will stop your application and take out from it.
        System.exit(2);
    }
}
