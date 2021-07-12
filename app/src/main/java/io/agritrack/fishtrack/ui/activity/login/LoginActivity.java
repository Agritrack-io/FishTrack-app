package io.agritrack.fishtrack.ui.activity.login;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import io.agritrack.fishtrack.R;
import io.agritrack.fishtrack.ui.activity.HomeActivity;
import io.agritrack.fishtrack.ui.activity.profile.ProfileActivity;

import static io.agritrack.fishtrack.FishTrackApplication.getContext;

public class LoginActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final Button btLogin = findViewById(R.id.btnLogin);
        btLogin.setOnClickListener(view -> {
            Toast.makeText(getContext(), "Logged IN!!", Toast.LENGTH_LONG).show();
            Intent i = new Intent(getApplicationContext(), ProfileActivity.class);
            startActivity(i);
        });

    }
}