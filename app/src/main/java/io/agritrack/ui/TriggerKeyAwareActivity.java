package io.agritrack.ui;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import io.agritrack.rfid.X9KeyReceiver;

public abstract class TriggerKeyAwareActivity extends AppCompatActivity {
    // listens to trigger button clicks.
    protected BroadcastReceiver keyReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // =================================
        // trigger + Fn keys will have the same effect as if clicking on Scan button
        keyReceiver = new X9KeyReceiver(this::onClick);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Listen for Fn key press/release;
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.rfid.FUN_KEY");
        this.registerReceiver(keyReceiver, filter);
    }

    @Override
    protected void onDestroy() {
        this.unregisterReceiver(keyReceiver);
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        this.unregisterReceiver(keyReceiver);
        super.onPause();
    }

    abstract protected void onClick(View view);
}