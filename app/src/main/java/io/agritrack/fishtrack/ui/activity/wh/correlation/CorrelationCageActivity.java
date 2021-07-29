package io.agritrack.fishtrack.ui.activity.wh.correlation;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import io.agritrack.fishtrack.R;
import io.agritrack.fishtrack.ui.activity.WhMenuActivity;

import static io.agritrack.fishtrack.FishTrackApplication.getContext;

public class CorrelationCageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_correlation_cage);

        configFooter();
    }

    protected void configFooter() {
        ImageView ivBack = (ImageView) findViewById(R.id.ivBackToCorrelationMenu);
        ivBack.setOnClickListener(view -> {
            Toast.makeText(getContext(), "Correlation menu!!", Toast.LENGTH_LONG).show();
            Intent i = new Intent(getApplicationContext(), CorrelationMenuActivity.class);
            startActivity(i);
        });

        ImageView ivNext = (ImageView) findViewById(R.id.ivToCongs);
        ivNext.setOnClickListener(view -> {
            Toast.makeText(getContext(), "Congratulations!!", Toast.LENGTH_LONG).show();
            Intent i = new Intent(getApplicationContext(), CorrelationMenuActivity.class);
            startActivity(i);
        });
    }
}