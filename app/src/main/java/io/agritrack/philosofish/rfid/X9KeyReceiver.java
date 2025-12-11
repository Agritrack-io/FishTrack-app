package io.agritrack.philosofish.rfid;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.View;

public class X9KeyReceiver extends BroadcastReceiver {
    //private Button clickBtn;
    private View.OnClickListener onClick;
    private long startTime = 0;
    private boolean keyUpFlag = true;


    public X9KeyReceiver() {
    }

    public X9KeyReceiver(View.OnClickListener l) {
//        this.clickBtn = btn;
        this.onClick = l;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        int keyCode = intent.getIntExtra("keyCode", 0);
        boolean keyDown = intent.getBooleanExtra("keydown", false);

        if ((keyCode == KeyEvent.KEYCODE_F3 || keyCode == KeyEvent.KEYCODE_F4 || keyCode == KeyEvent.KEYCODE_F5)) {

            if (keyUpFlag && keyDown && System.currentTimeMillis() - startTime > 500) {
                keyUpFlag = false;
                startTime = System.currentTimeMillis();
                //this.clickBtn.callOnClick();

                if (this.onClick != null) {
                    this.onClick.onClick(null);
                }
            } else if (keyDown) {
                startTime = System.currentTimeMillis();
            } else {
                keyUpFlag = true;
            }
        }
    }
}
