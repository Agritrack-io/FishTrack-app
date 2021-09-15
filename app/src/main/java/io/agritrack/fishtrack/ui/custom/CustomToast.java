package io.agritrack.fishtrack.ui.custom;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import io.agritrack.fishtrack.R;

public class CustomToast {

    public static void CToast(android.content.Context context, CharSequence msg, int duration){
        LayoutInflater inflater = LayoutInflater.from(context);
        View layout = inflater.inflate(R.layout.custom_toast_container, null);

        LinearLayout customContainer = (LinearLayout) layout.findViewById(R.id.custom_toast_container);
        customContainer.setBackgroundResource(R.drawable.toast_background);
        TextView text = (TextView) layout.findViewById(R.id.message);
        text.setText(msg);

        Toast toast = new Toast(context);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER,0,0);
        toast.setView(layout);
        toast.show();
    }


}
