package io.agritrack.xchange;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.View;

public class FnKeyReceiver extends BroadcastReceiver {
    //private Button clickBtn;
    private View.OnClickListener onClick;
    private long startTime = 0;
    private boolean keyUpFlag = true;


    public FnKeyReceiver() { }

    public FnKeyReceiver(View.OnClickListener l) {
        this.onClick = l;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        int keyCode = intent.getIntExtra("keyCode", 0);
        boolean keyDown = intent.getBooleanExtra("keydown", false);

        if ((keyCode == KeyEvent.KEYCODE_F3 || keyCode == KeyEvent.KEYCODE_F4  || keyCode == KeyEvent.KEYCODE_F5)) {

            if (keyUpFlag && keyDown && System.currentTimeMillis() - startTime > 500) {
                keyUpFlag = false;
                startTime = System.currentTimeMillis();

                this.onClick.onClick(null);
            } else if (keyDown) {
                startTime = System.currentTimeMillis();
            } else {
                keyUpFlag = true;
            }
        }
    }
}
