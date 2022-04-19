package io.agritrack.fish.ui.quality;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

import io.agritrack.R;
import io.agritrack.dialog.SupportDialog;
import io.agritrack.fish.state.GlobalState;
import io.agritrack.fish.ui.FishHomeActivity;
import io.agritrack.fish.ui.quality.postpackage.PostPackagingQualityActivity;
import io.agritrack.fish.ui.quality.receipt.ReceiptQualityStartActivity;
import io.agritrack.ui.adapter.InventoryMenuAdapter;
import io.agritrack.ui.adapter.MenuItem;
import io.agritrack.ui.service.LocalPreferences;

public class QualitySelectStepsActivity extends AppCompatActivity {

    private static final int First_Step_Idx = 0, Second_Step_Idx = 1, Third_Step_Idx = 1;
    private GridView gvQualityMenu;

    private ImageView ivSupport;
    private SupportDialog supportDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quality_select_steps);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderPackageQualitySelect);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get  references of the controls
        assignCtrlVars();

        ArrayList<MenuItem> menuItemsList = new ArrayList<MenuItem>();
        menuItemsList.add(new MenuItem(getString(R.string.quality_first_step_text), "PP-DOC-01", ReceiptQualityStartActivity.class));
        //menuItemsList.add(new MenuItem(getString(R.string.quality_second_step_text), "", PackageQualityStartActivity.class));
        menuItemsList.add(new MenuItem(getString(R.string.quality_third_step_text), "PP-DOC-02", PostPackagingQualityActivity.class));

        InventoryMenuAdapter adapter = new InventoryMenuAdapter(this, menuItemsList);

        gvQualityMenu.setAdapter(adapter);
        gvQualityMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                //final Context appCtx = getApplicationContext();
                Intent i = new Intent(QualitySelectStepsActivity.this, ReceiptQualityStartActivity.class);

                switch (position) {
                    case First_Step_Idx:
                        GlobalState.initQualityRecord();
                        i = new Intent(QualitySelectStepsActivity.this, ReceiptQualityStartActivity.class);
                        i.putExtra("id", position);
                        startActivity(i);

                        //
                        /*YesNoDialogFragment confirmSiteSelectionDlg = YesNoDialogFragment.instance();
                        confirmSiteSelectionDlg.setMessage(getText(R.string.quality_select_type));

                        Intent finalI = i;
                        confirmSiteSelectionDlg.onConfirm(bundle -> {
                            finalI.putExtra("processing", true);
                            startActivity(finalI);
                        });
                        confirmSiteSelectionDlg.onReject(bundle -> {
                            finalI.putExtra("processing", false);
                            startActivity(finalI);
                        });

                        FragmentManager fm = getSupportFragmentManager();
                        confirmSiteSelectionDlg.showNow(fm, getString(R.string.confirm_selection));*/
                        break;
                    /*case Second_Step_Idx:
                        GlobalState.initQualityRecord();
                        i = new Intent(QualitySelectStepsActivity.this, PackageQualityStartActivity.class);
                        i.putExtra("id", position);
                        startActivity(i);
                        break;*/
                    case Third_Step_Idx:
                        GlobalState.initQualityRecord();
                        i = new Intent(QualitySelectStepsActivity.this, PostPackagingQualityActivity.class);
                        i.putExtra("id", position);
                        startActivity(i);
                        break;
                }
            }
        });

        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(QualitySelectStepsActivity.this);
            supportDialog.showDialog();
        });

        configFooter();
    }

    protected void configFooter() {
        ImageView ivBack = (ImageView) findViewById(R.id.ivBackToMenu);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), FishHomeActivity.class);
            startActivity(i);
        });
    }

    private void assignCtrlVars() {
        gvQualityMenu = findViewById(R.id.gvQualityMenu);
        ivSupport = findViewById(R.id.ivSupport);
    }
}