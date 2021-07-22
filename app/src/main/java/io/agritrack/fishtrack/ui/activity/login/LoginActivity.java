package io.agritrack.fishtrack.ui.activity.login;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.widget.Button;
import android.widget.Toast;

import io.agritrack.fishtrack.R;
import io.agritrack.fishtrack.ui.activity.fishing.FishingStartActivity;
import io.agritrack.fishtrack.ui.activity.transport.TransportStartActivity;

import static io.agritrack.fishtrack.FishTrackApplication.getContext;

public class LoginActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final Button btLogin = findViewById(R.id.btnLogin);
        btLogin.setOnClickListener(view -> {
            Toast.makeText(getContext(), "Logged IN!!", Toast.LENGTH_LONG).show();
            Intent i = new Intent(getApplicationContext(), TransportStartActivity.class);
            startActivity(i);
        });

    }
}