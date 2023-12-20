package io.agritrack.fish.ui.wh.zebra.correlation;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Arrays;

import io.agritrack.R;
import io.agritrack.dialog.SupportDialog;
import io.agritrack.fish.ui.WhMenuActivity;
import io.agritrack.ui.adapter.IOnItemClickListener;
import io.agritrack.ui.adapter.OptionGridAdapter;
import io.agritrack.ui.service.LocalPreferences;
import lombok.Data;

public class ZebraCorrelationMenuActivity extends AppCompatActivity {

    private static final int Cage_Idx = 0, Net_Idx = 1, Bin_Idx = 3, Platform_Idx = 4;
    GridView gvCorrelationMenu;
    private ImageView ivSupport;
    private SupportDialog supportDialog;
    private ProgressDialog progressDialog;

    private IOnItemClickListener<Option> onItemClickListener;
    private GridView grid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zebra_correlation_menu);
        grid = findViewById(R.id.gridView);
//        gvCorrelationMenu = findViewById(R.id.gvCorrelationMenu);

        onItemClickListener = new IOnItemClickListener<Option>() {
            @Override
            public void onItemClick(int position, Option element) {
                final Context appCtx = getApplicationContext();
                Intent i = new Intent(appCtx, ZebraCorrelationSubMenuActivity.class);

                i.putExtra("id", position);
                startActivity(i);
            }
        };

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderCorrelationMenu);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        OptionGridAdapter adapter = new OptionGridAdapter(this, Arrays.asList(
                new Option(getString(R.string.menu_title_cage), R.drawable.cage),
                new Option(getString(R.string.menu_title_net), R.drawable.net),
                new Option(getString(R.string.menu_title_bin), R.drawable.bin),
                new Option(getString(R.string.platform), R.drawable.platform)
        ), onItemClickListener);
        grid.setAdapter(adapter);

        // instantiate ProgressDialog and set style.
        progressDialog = new ProgressDialog(ZebraCorrelationMenuActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        ivSupport = findViewById(R.id.ivSupport);
        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(ZebraCorrelationMenuActivity.this);
            supportDialog.showDialog();
        });

        configFooter();
    }

    protected void configFooter() {
        ImageView ivBack = (ImageView) findViewById(R.id.ivBackToWhMenu);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), WhMenuActivity.class);
            startActivity(i);
        });
    }

    @Data
    public class Option implements OptionGridAdapter.IDrawableWithText {
        String name;
        int imageId;

        public Option(String name, int imageId) {
            this.name = name;
            this.imageId = imageId;
        }

        @Override
        public String getText() {
            return name;
        }

        @Override
        public int getResourceId() {
            return imageId;
        }
    }
}