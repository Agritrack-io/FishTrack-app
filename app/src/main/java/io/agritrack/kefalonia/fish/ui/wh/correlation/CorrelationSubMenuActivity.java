package io.agritrack.kefalonia.fish.ui.wh.correlation;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

import io.agritrack.kefalonia.R;
import io.agritrack.kefalonia.dialog.SupportDialog;
import io.agritrack.kefalonia.fish.state.GlobalState;
import io.agritrack.kefalonia.ui.adapter.HomeMenuAdapter;
import io.agritrack.kefalonia.ui.adapter.MenuItem;
import io.agritrack.kefalonia.ui.login.LoginActivity;
import io.agritrack.kefalonia.ui.service.LocalPreferences;

public class CorrelationSubMenuActivity extends AppCompatActivity {

    private static final int Correlate_Idx = 0, Existing_Tag_Idx = 1, New_Tag_Idx = 2;
    GridView gvCorrelationMenu;
    private ImageView ivSupport;
    private SupportDialog supportDialog;
    private ProgressDialog progressDialog;
    private int finalPosition;
    private Class clazz = null;

    @SuppressLint({"StringFormatMatches", "StringFormatInvalid"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_correlation_sub_menu);

        if (getIntent() != null) {
            Bundle bundle = getIntent().getExtras();
            finalPosition = bundle != null ? bundle.getInt("id") : 0;
        }

        gvCorrelationMenu = findViewById(R.id.gvCorrelationMenu);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderCorrelationMenu);
        TextView tvTitleToolbar = findViewById(R.id.tvTitleToolbar);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // instantiate ProgressDialog and set style.
        progressDialog = new ProgressDialog(CorrelationSubMenuActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        ArrayList<MenuItem> menuItemsList = new ArrayList<MenuItem>();

        switch (finalPosition) {
            case 0:
                tvTitleToolbar.setText(getString(R.string.title_program_menu, getString(R.string.of_cage)));
                menuItemsList.add(new MenuItem(getString(R.string.menu_title_correlation), CorrelationCageActivity.class, R.drawable.cage));
                clazz = CorrelationCageActivity.class;
                break;
            case 1:
                tvTitleToolbar.setText(getString(R.string.title_program_menu, getString(R.string.of_net)));
                menuItemsList.add(new MenuItem(getString(R.string.menu_title_correlation), CorrelationNetActivity.class, R.drawable.net));
                clazz = CorrelationNetActivity.class;
                break;
            case 2:
                tvTitleToolbar.setText(getString(R.string.title_program_menu, getString(R.string.of_bin)));
                menuItemsList.add(new MenuItem(getString(R.string.menu_title_correlation), CorrelationBinActivity.class, R.drawable.bin));
                clazz = CorrelationBinActivity.class;
                break;
            case 3:
                tvTitleToolbar.setText(getString(R.string.title_program_menu, getString(R.string.of_cage_net)));
                menuItemsList.add(new MenuItem(getString(R.string.menu_title_correlation), CorrelationCageNetActivity.class, R.drawable.cage_net));
                clazz = CorrelationCageNetActivity.class;
                break;
            default:
        }

//        menuItemsList.add(new MenuItem(getString(R.string.menu_title_replace_tags), ExistingTagActivity.class, R.drawable.program));
//        menuItemsList.add(new MenuItem(getString(R.string.menu_title_new_tags), NewTagActivity.class, R.drawable.program));

        HomeMenuAdapter adapter = new HomeMenuAdapter(this, menuItemsList);
        gvCorrelationMenu.setAdapter(adapter);

        gvCorrelationMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                final Context appCtx = getApplicationContext();
                Intent i = new Intent(appCtx, LoginActivity.class);

                switch (position) {
                    case Correlate_Idx:
                        GlobalState.initWHCorrelationRecord();
                        i = new Intent(appCtx, clazz);
                        break;
//                    case Existing_Tag_Idx:
//                        i = new Intent(appCtx, ExistingTagActivity.class);
//                        break;
//                    case New_Tag_Idx:
//                        i = new Intent(appCtx, NewTagActivity.class);
//                        break;
                    default:
                }

                // Pass image index
                i.putExtra("id", position);
                i.putExtra("assetType", finalPosition);
                startActivity(i);
            }
        });

        ivSupport = findViewById(R.id.ivSupport);
        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(CorrelationSubMenuActivity.this);
            supportDialog.showDialog();
        });

        configFooter();
    }

    protected void configFooter() {
        ImageView ivBack = (ImageView) findViewById(R.id.ivBackToWhMenu);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), CorrelationMenuActivity.class);
            startActivity(i);
        });
    }
}