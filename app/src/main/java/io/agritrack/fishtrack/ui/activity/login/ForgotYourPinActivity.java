package io.agritrack.fishtrack.ui.activity.login;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import gr.beffect.agritrack.AgriTrackApplication;
import gr.beffect.agritrack.R;
import gr.beffect.agritrack.service.RestfulCommunicationSingleton;
import gr.beffect.agritrack.state.ForgotYourPinState;

public class ForgotYourPinActivity extends AppCompatActivity {
    EditText phoneNumberText;
    EditText usernameText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_your_pin);

        phoneNumberText = findViewById(R.id.phone_number_text);
        usernameText = findViewById(R.id.username_text);
        Button cancelButton = findViewById(R.id.cancel_button);
        Button sendButton = findViewById(R.id.send_button);

        cancelButton.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(i);
        });

        sendButton.setOnClickListener(view -> sendPasswordReminder());
    }

    public void sendPasswordReminder() {
        String mUrlString;
        try {
            InputStream is = AgriTrackApplication.getContext().getAssets().open("connection.properties");
            Properties props = new Properties();
            props.load(is);
            mUrlString = props.getProperty("url", null) + "/users/password.remind";
            is.close();
            Uri.Builder builder = Uri.parse(mUrlString).buildUpon();
            builder.appendQueryParameter("phoneNumber", phoneNumberText.getText().toString());
            builder.appendQueryParameter("username", usernameText.getText().toString());
            mUrlString = builder.build().toString();
            StringRequest ar = new StringRequest(Request.Method.POST, mUrlString, object -> {
                ForgotYourPinState state = ForgotYourPinState.getNewInstance();
                state.setSuccess(true);
                Intent i = new Intent(getApplicationContext(), ForgotYourPinResultActivity.class);
                startActivity(i);
            }, error -> {
                ForgotYourPinState state = ForgotYourPinState.getNewInstance();
                state.setSuccess(false);
                Intent i = new Intent(getApplicationContext(), ForgotYourPinResultActivity.class);
                startActivity(i);
            }) {
                @Override
                public Map<String, String> getHeaders() {
                    HashMap<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    return headers;
                }
            };
            ar.setRetryPolicy(new DefaultRetryPolicy(
                    0,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            ar.setShouldCache(false);
            RestfulCommunicationSingleton.getInstance(AgriTrackApplication.getContext()).addToRequestQueue(ar);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}