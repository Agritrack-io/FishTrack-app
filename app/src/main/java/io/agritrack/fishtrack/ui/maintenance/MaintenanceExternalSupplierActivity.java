package io.agritrack.fishtrack.ui.maintenance;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.util.Strings;

import java.util.List;

import io.agritrack.fishtrack.R;
import io.agritrack.fishtrack.data.db.MobileDB;
import io.agritrack.fishtrack.data.model.common.Supplier;
import io.agritrack.fishtrack.state.GlobalState;
import io.agritrack.fishtrack.ui.bo.GenericListModel;
import io.agritrack.fishtrack.ui.service.LocalPreferences;

import static io.agritrack.fishtrack.FishTrackApplication.getContext;

public class MaintenanceExternalSupplierActivity extends AppCompatActivity implements OnItemClickListener {

    private ListView lvSupplier;
    private EditText etMaintenanceManager, etMaintenanceCost;
    private MultiAutoCompleteTextView mtvExtRemarks;

    private MobileDB db;
    private GenericListModel[] availableSuppliers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maintenance_external_supplier);

        // get  references of the controls
        assignCtrlVars();

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderMaintenanceExternalSupplier);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get an instance of local DB
        db = MobileDB.getInstance(getContext());

        // define if single or multiple choice mode will be used to display the checkboxes.
        this.lvSupplier.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        // load suppliers and fill in the lvSupplier ListView.
        List<Supplier> suppliersList = db.supplierDAO().getAll();
        if (suppliersList != null && !suppliersList.isEmpty()) {
            this.availableSuppliers = suppliersList.stream().map(x -> new GenericListModel(x.id, x.name)).toArray(GenericListModel[]::new);
            ArrayAdapter<GenericListModel> candidatesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_checked, availableSuppliers);

            this.lvSupplier.setAdapter(candidatesAdapter);
            this.lvSupplier.setOnItemClickListener(this);
        }

        // set (any?) previously selected values to activity Controls.
        initControlsFromState();

        configFooter();
    }

    protected void configFooter() {
        ImageView ivBack = (ImageView) findViewById(R.id.ivBackToMaintenanceExternalStart);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), MaintenanceExternalStartActivity.class);
            startActivity(i);
        });

        ImageView ivNext = (ImageView) findViewById(R.id.ivToConfirmExternal);
        ivNext.setOnClickListener(view -> {
            updateState();
            Intent i = new Intent(getApplicationContext(), MaintenanceExternalConfirmActivity.class);
            startActivity(i);
        });
    }

    private void assignCtrlVars() {
        lvSupplier = findViewById(R.id.lvSupplier);
        etMaintenanceManager = findViewById(R.id.etMaintenanceManager);
        etMaintenanceCost = findViewById(R.id.etMaintenanceCost);
        mtvExtRemarks = findViewById(R.id.mtvExtRemarks);
    }

    private void initControlsFromState() {
        if (GlobalState.recExternalRepair.repairTeam != null) {
            int sz = GlobalState.recExternalRepair.repairTeam.size();
            // Since coming from <back> button, retain the previously checked items.
            for (int i = 0; i < sz; i++) {
                this.lvSupplier.setItemChecked(GlobalState.recExternalRepair.repairTeam.get(i).intValue(), Boolean.TRUE);
            }
        }

        if (!Strings.isEmptyOrWhitespace(GlobalState.recExternalRepair.manager)) {
            etMaintenanceManager.setText(GlobalState.recExternalRepair.manager);
        }
        if (!Strings.isEmptyOrWhitespace(GlobalState.recExternalRepair.cost)) {
            etMaintenanceCost.setText(GlobalState.recExternalRepair.cost);
        }

        if (!Strings.isEmptyOrWhitespace(GlobalState.recExternalRepair.remarks)) {
            mtvExtRemarks.setText(GlobalState.recExternalRepair.remarks);
        }
    }

    private void updateState() {
        int supLoc = this.lvSupplier.getCheckedItemPosition();
        GlobalState.recExternalRepair.supplierPos = supLoc;
        GlobalState.recExternalRepair.manager = etMaintenanceManager.getText().toString();
        GlobalState.recExternalRepair.cost = etMaintenanceCost.getText().toString();
        GlobalState.recExternalRepair.remarks = mtvExtRemarks.getText().toString();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        CheckedTextView v = (CheckedTextView) view;
        boolean currentCheck = v.isChecked();
        GenericListModel member = (GenericListModel) this.lvSupplier.getItemAtPosition(position);
        member.setChecked(!currentCheck);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}