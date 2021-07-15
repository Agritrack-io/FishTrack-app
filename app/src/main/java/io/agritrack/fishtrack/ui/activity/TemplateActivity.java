package io.agritrack.fishtrack.ui.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import io.agritrack.fishtrack.R;

public class TemplateActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_template);

        getSupportActionBar().hide();
    }
}