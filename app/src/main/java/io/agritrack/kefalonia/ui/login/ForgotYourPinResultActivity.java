package io.agritrack.kefalonia.ui.login;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import io.agritrack.kefalonia.R;
import io.agritrack.kefalonia.ui.state.ForgotYourPinState;


public class ForgotYourPinResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_your_pin_result);

        ImageView continueButton = findViewById(R.id.ivSend);
        TextView forgotYourPinResultText = findViewById(R.id.forgot_your_pin_result_text);

        if (ForgotYourPinState.getInstance().isSuccess()) {
            continueButton.setOnClickListener(view -> {
                Intent i = new Intent(getApplicationContext(), LoginActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
            });
        } else {
            forgotYourPinResultText.setText(R.string.forgot_your_pin_failure);
            continueButton.setOnClickListener(view -> {
                Intent i = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(i);
            });
        }
    }
}