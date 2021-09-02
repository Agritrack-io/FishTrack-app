package io.agritrack.fishtrack.ui.wh.outgoing;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.util.Strings;

import java.util.Arrays;

import io.agritrack.fishtrack.R;
import io.agritrack.fishtrack.common.Constants;
import io.agritrack.fishtrack.enums.AssetType;
import io.agritrack.fishtrack.state.GlobalState;
import io.agritrack.fishtrack.state.WHTxRecord;
import io.agritrack.fishtrack.ui.WhMenuActivity;
import io.agritrack.fishtrack.ui.custom.ToggleGroup;
import io.agritrack.fishtrack.ui.service.LocalPreferences;

public class OutgoingStartActivity extends AppCompatActivity implements ToggleGroup.OnCheckedChangeListener {
    private Spinner spAssetType;
    private TextView tvOutgoingFrom, tvOutgoingTo;
    private ToggleGroup tgOutgoingSource, tgOutgoingDestination;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outgoing_start);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderOutgoingStart);
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

    protected void configFooter() {
        ImageView ivNext = (ImageView) findViewById(R.id.ivToOutgoingProcess);
        ivNext.setOnClickListener(view -> {
            updateState();
            String v = validate();
            if (!Strings.isEmptyOrWhitespace(v)) {
                Toast.makeText(getApplicationContext(), "Invalid inputs : " + v, Toast.LENGTH_LONG).show();
            } else {
                Intent i = new Intent(getApplicationContext(), OutgoingProcessActivity.class);
                startActivity(i);
            }
        });

        ImageView ivBack = (ImageView) findViewById(R.id.ivBackToWhMenu);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), WhMenuActivity.class);
            startActivity(i);
        });
    }

    private void assignCtrlVars() {
        spAssetType = findViewById(R.id.spAssetType);
        tvOutgoingFrom = findViewById(R.id.tvOutgoingFrom);
        tvOutgoingTo = findViewById(R.id.tvOutgoingTo);

        tgOutgoingSource = findViewById(R.id.tgOutgoingSource);
        tgOutgoingDestination = findViewById(R.id.tgOutgoingDestination);

        tgOutgoingSource.setOnCheckedChangeListener(this);
        tgOutgoingDestination.setOnCheckedChangeListener(this);
    }

    @Override
    public void onCheckedChanged(ToggleGroup group, int checkedId) {
        if (checkedId == R.id.tbSite) {
            GlobalState.recWHOutgoing.from = Constants.ftSite;
            tvOutgoingFrom.setText(Constants.ftSite);
        } else if (checkedId == R.id.tbAsset) {
            GlobalState.recWHOutgoing.from = Constants.ftAsset;
            tvOutgoingFrom.setText(Constants.ftAsset);
        } else if (checkedId == R.id.tbAvramar) {
            GlobalState.recWHOutgoing.to = Constants.ftAvramar;
            tvOutgoingTo.setText(Constants.ftAvramar);
        } else if (checkedId == R.id.tbCustomer) {
            GlobalState.recWHOutgoing.to = Constants.ftCustomer;
            tvOutgoingTo.setText(Constants.ftCustomer);
        } else if (checkedId == R.id.tbOutAssetTo) {
            GlobalState.recWHOutgoing.to = Constants.ftAsset;
            tvOutgoingTo.setText(Constants.ftAsset);
        }
    }

    private WHTxRecord updateState() {
        WHTxRecord whOutgoingRecord = GlobalState.recWHOutgoing;

        if (spAssetType.getSelectedItem() != null) {
            whOutgoingRecord.assetType = AssetType.valueOf(spAssetType.getSelectedItem().toString());
        }
        whOutgoingRecord.assetTypePos = spAssetType.getSelectedItemPosition();

        return whOutgoingRecord;
    }

    private String validate(){
        StringBuilder sb = new StringBuilder();

        if(Strings.isEmptyOrWhitespace(GlobalState.recWHOutgoing.to)){
            sb.append(String.format("\n%s is missing", "'Target site'"));
        }

        if(Strings.isEmptyOrWhitespace(GlobalState.recWHOutgoing.from)){
            sb.append(String.format("\n%s is missing", "'Source site'"));
        }

        return sb.toString();
    }

    private void initControlsFromState()    {
        if (GlobalState.recWHOutgoing.assetTypePos > -1) {
            spAssetType.setSelection(GlobalState.recWHOutgoing.assetTypePos);
        }

        if (Constants.ftSite.equalsIgnoreCase(GlobalState.recWHOutgoing.from)) {
            tgOutgoingSource.check(R.id.tbSite);
        } else if (Constants.ftAsset.equalsIgnoreCase(GlobalState.recWHOutgoing.from)) {
            tgOutgoingSource.check(R.id.tbAsset);
        }

        if (Constants.ftAvramar.equalsIgnoreCase(GlobalState.recWHOutgoing.to)) {
            tgOutgoingDestination.check(R.id.tbAvramar);
        } else if (Constants.ftCustomer.equalsIgnoreCase(GlobalState.recWHOutgoing.to)) {
            tgOutgoingDestination.check(R.id.tbCustomer);
        } else if (Constants.ftAsset.equalsIgnoreCase(GlobalState.recWHOutgoing.to)) {
            tgOutgoingDestination.check(R.id.tbOutAssetTo);
        }
    }
}