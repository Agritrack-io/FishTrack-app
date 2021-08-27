package io.agritrack.fishtrack.ui.wh.incoming;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Arrays;

import io.agritrack.fishtrack.R;
import io.agritrack.fishtrack.common.Constants;
import io.agritrack.fishtrack.enums.AssetType;
import io.agritrack.fishtrack.state.GlobalState;
import io.agritrack.fishtrack.state.IncomingWHRecord;
import io.agritrack.fishtrack.ui.WhMenuActivity;
import io.agritrack.fishtrack.ui.custom.ToggleGroup;
import io.agritrack.fishtrack.ui.service.LocalPreferences;

public class IncomingStartActivity extends AppCompatActivity implements ToggleGroup.OnCheckedChangeListener {

    private Spinner spAssetType;
    private TextView tvIncomingFrom, tvIncomingTo;
    private ToggleGroup tgIncomingSource, tgIncomingDestination;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming_start);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderIncomingStart);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get  references of the controls
        assignCtrlVars();

        // load all Asset Types and fill in the spAssetType Spinner.
        AssetType[] assetTypes = AssetType.values();
        if (assetTypes != null) {
            String[] assetTypeArray = Arrays.stream(assetTypes).map(x -> x.name()).toArray(String[]::new);
            ArrayAdapter<String> atAdapter = new ArrayAdapter<>(this, R.layout.simple_spinner_item, assetTypeArray);
            atAdapter.setDropDownViewResource(R.layout.simple_spinner_item);
            spAssetType.setAdapter(atAdapter);
        }

        // set (any?) previously selected values to activity Controls.
        initControlsFromState();

        configFooter();
    }

    private void assignCtrlVars() {
        spAssetType = findViewById(R.id.spAssetType);
        tvIncomingFrom = findViewById(R.id.tvIncomingFrom);
        tvIncomingTo = findViewById(R.id.tvIncomingTo);

        tgIncomingSource = findViewById(R.id.tgIncomingSource);
        tgIncomingDestination = findViewById(R.id.tgIncomingDestination);

        tgIncomingSource.setOnCheckedChangeListener(this);
        tgIncomingDestination.setOnCheckedChangeListener(this);
    }

    protected void configFooter() {
        ImageView ivNext = (ImageView) findViewById(R.id.ivToIncomingProcess);
        ivNext.setOnClickListener(view -> {
            updateState();
            Intent i = new Intent(getApplicationContext(), IncomingProcessActivity.class);
            startActivity(i);
        });

        ImageView ivBack = (ImageView) findViewById(R.id.ivBackToWhMenu);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), WhMenuActivity.class);
            startActivity(i);
        });
    }

    @Override
    public void onCheckedChanged(ToggleGroup group, int checkedId) {
        if (checkedId == R.id.tbAvramar) {
            GlobalState.recWHIncoming.incomingFrom = Constants.ftAvramar;
            tvIncomingFrom.setText(Constants.ftAvramar);
        } else if (checkedId == R.id.tbSupplier) {
            GlobalState.recWHIncoming.incomingFrom = Constants.ftSupplier;
            tvIncomingFrom.setText(Constants.ftSupplier);
        } else if (checkedId == R.id.tbAssetFrom) {
            GlobalState.recWHIncoming.incomingFrom = Constants.ftAsset;
            tvIncomingFrom.setText(Constants.ftAsset);
        } else if (checkedId == R.id.tbSite) {
            GlobalState.recWHIncoming.incomingTo = Constants.ftSite;
            tvIncomingTo.setText(Constants.ftSite);
        } else if (checkedId == R.id.tbAssetTo) {
            GlobalState.recWHIncoming.incomingTo = Constants.ftAsset;
            tvIncomingTo.setText(Constants.ftAsset);
        }
    }

    private IncomingWHRecord updateState() {
        IncomingWHRecord whIncomingRecord = GlobalState.recWHIncoming;

        if (spAssetType.getSelectedItem() != null) {
            whIncomingRecord.assetType = AssetType.valueOf(spAssetType.getSelectedItem().toString());
        }
        whIncomingRecord.assetTypePos = spAssetType.getSelectedItemPosition();

        return whIncomingRecord;
    }

    private void initControlsFromState()    {
        if (GlobalState.recWHIncoming.assetTypePos > -1) {
            spAssetType.setSelection(GlobalState.recWHIncoming.assetTypePos);
        }

        if (Constants.ftAvramar.equalsIgnoreCase(GlobalState.recWHIncoming.incomingFrom)) {
            tgIncomingSource.check(R.id.tbAvramar);
        } else if (Constants.ftSupplier.equalsIgnoreCase(GlobalState.recWHIncoming.incomingFrom)) {
            tgIncomingSource.check(R.id.tbSupplier);
        } else if (Constants.ftAsset.equalsIgnoreCase(GlobalState.recWHIncoming.incomingFrom)) {
            tgIncomingSource.check(R.id.tbAssetFrom);
        }

        if (Constants.ftSite.equalsIgnoreCase(GlobalState.recWHIncoming.incomingTo)) {
            tgIncomingDestination.check(R.id.tbSite);
        } else if (Constants.ftAsset.equalsIgnoreCase(GlobalState.recWHIncoming.incomingTo)) {
            tgIncomingDestination.check(R.id.tbAssetTo);
        }
    }
}