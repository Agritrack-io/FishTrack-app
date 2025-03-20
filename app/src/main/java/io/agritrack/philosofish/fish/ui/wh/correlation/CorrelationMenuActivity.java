package io.agritrack.philosofish.fish.ui.wh.correlation;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Arrays;

import io.agritrack.philosofish.R;
import io.agritrack.philosofish.dialog.SupportDialog;
import io.agritrack.philosofish.fish.ui.WhMenuActivity;
import io.agritrack.philosofish.ui.adapter.IOnItemClickListener;
import io.agritrack.philosofish.ui.adapter.OptionGridAdapter;
import io.agritrack.philosofish.ui.service.LocalPreferences;

public class CorrelationMenuActivity extends AppCompatActivity {

    private static final int Cage_Idx = 0, Net_Idx = 1, Bin_Idx = 3, Cage_Net_Idx = 4;
    GridView gvCorrelationMenu;
    private ImageView ivSupport;
    private SupportDialog supportDialog;
    private ProgressDialog progressDialog;

    private IOnItemClickListener<Option> onItemClickListener;
    private GridView grid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_correlation_menu);
        grid = findViewById(R.id.gridView);
//        gvCorrelationMenu = findViewById(R.id.gvCorrelationMenu);

        onItemClickListener = new IOnItemClickListener<Option>() {
            @Override
            public void onItemClick(int position, Option element) {
                final Context appCtx = getApplicationContext();
//                Intent i = new Intent(appCtx, CorrelationSubMenuActivity.class);
                Intent i;
                //i.putExtra("id", position);
                //startActivity(i);

                switch (position) {
                    case 0:
                         i = new Intent(appCtx, ZebraCorrelationCageActivity.class);
                        startActivity(i);;
                        break;
                    case 1:
                         i = new Intent(appCtx, ZebraCorrelationNetActivity.class);
                        startActivity(i);;
                        break;
                    case 2:
                         i = new Intent(appCtx, CorrelationBinActivity.class);
                        startActivity(i);;
                        break;
                    case 3:
                        i = new Intent(appCtx, CorrelationCageNetActivity.class);
                        startActivity(i);;
                        break;
                    default:
                }
            }
        };

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderCorrelationMenu);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        OptionGridAdapter adapter = new OptionGridAdapter(this, Arrays.asList(
                new Option(getString(R.string.menu_title_cage), R.drawable.cage),
                new Option(getString(R.string.menu_title_net), R.drawable.net),
                new Option(getString(R.string.menu_title_bin), R.drawable.bin),
                new Option(getString(R.string.menu_title_cage_net), R.drawable.cage_net)
        ), onItemClickListener);
        grid.setAdapter(adapter);

        // instantiate ProgressDialog and set style.
        progressDialog = new ProgressDialog(CorrelationMenuActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        ivSupport = findViewById(R.id.ivSupport);
        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(CorrelationMenuActivity.this);
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