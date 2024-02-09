package io.agritrack.ui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import io.agritrack.kefalonia.R;

public class TemplateActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_template);

        getSupportActionBar().hide();
    }
}