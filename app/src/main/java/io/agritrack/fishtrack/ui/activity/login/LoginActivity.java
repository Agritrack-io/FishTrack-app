package io.agritrack.fishtrack.ui.activity.login;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;

import io.agritrack.fishtrack.R;
import io.agritrack.fishtrack.data.MobileDB;
import io.agritrack.fishtrack.ui.activity.fishing.FishingStartActivity;
import io.agritrack.fishtrack.ui.activity.process.ProcessStartActivity;
import io.agritrack.fishtrack.ui.activity.transport.TransportStartActivity;
import io.agritrack.fishtrack.ui.activity.wh.IncomingStartActivity;
import io.agritrack.fishtrack.ui.activity.wh.OutgoingStartActivity;

import static io.agritrack.fishtrack.FishTrackApplication.getContext;

public class LoginActivity extends AppCompatActivity {
    private MobileDB db;
    private ProgressBar loadingProgressBar;
    private TextView loadingText;
    private MutableLiveData<LoginResult> loginResult = new MutableLiveData<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final Button btLogin = findViewById(R.id.btnLogin);
        btLogin.setOnClickListener(view -> {
            Toast.makeText(getContext(), "Logged IN!!", Toast.LENGTH_LONG).show();
            Intent i = new Intent(getApplicationContext(), OutgoingStartActivity.class);
            startActivity(i);
        });

    }
}