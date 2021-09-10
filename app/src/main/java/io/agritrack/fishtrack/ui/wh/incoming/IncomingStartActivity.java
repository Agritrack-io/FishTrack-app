package io.agritrack.fishtrack.ui.wh.incoming;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.util.Strings;

import io.agritrack.fishtrack.R;
import io.agritrack.fishtrack.common.Constants;
import io.agritrack.fishtrack.state.GlobalState;
import io.agritrack.fishtrack.state.WHTxRecord;
import io.agritrack.fishtrack.ui.WhMenuActivity;
import io.agritrack.fishtrack.ui.custom.ToggleGroup;
import io.agritrack.fishtrack.ui.service.LocalPreferences;

import static io.agritrack.fishtrack.common.LargeString.render;

public class IncomingStartActivity extends AppCompatActivity implements ToggleGroup.OnCheckedChangeListener {

    private TextView tvIncomingFrom, tvIncomingTo;
    private ToggleGroup tgIncomingSource, tgIncomingDestination, tgIncomingItemType;
    private String selectedIncomingItemType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming_start);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderIncomingStart);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get  references of the controls
        assignCtrlVars();

        // set (any?) previously selected values to activity Controls.
        initControlsFromState();

        configFooter();
    }

    private void assignCtrlVars() {
        tvIncomingFrom = findViewById(R.id.tvIncomingFrom);
        tvIncomingTo = findViewById(R.id.tvIncomingTo);

        tgIncomingSource = findViewById(R.id.tgIncomingSource);
        tgIncomingDestination = findViewById(R.id.tgIncomingDestination);
        tgIncomingItemType = findViewById(R.id.tgIncomingItemType);

        tgIncomingItemType.setOnCheckedChangeListener(this);

        tgIncomingSource.setOnCheckedChangeListener(this);
        tgIncomingDestination.setOnCheckedChangeListener(this);
    }

    protected void configFooter() {
        ImageView ivNext = (ImageView) findViewById(R.id.ivToIncomingProcess);
        ivNext.setOnClickListener(view -> {
            updateState();
            String v = validate();
            if (!Strings.isEmptyOrWhitespace(v)) {
                Toast.makeText(getApplicationContext(), render("Invalid inputs : " + v), Toast.LENGTH_LONG).show();
            } else if (selectedIncomingItemType == Constants.ftAsset){
                Intent i = new Intent(getApplicationContext(), IncomingAssetActivity.class);
                startActivity(i);
            } else {
                Intent i = new Intent(getApplicationContext(), IncomingConsumableActivity.class);
                startActivity(i);
            }
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
            GlobalState.recWHIncoming.from = Constants.ftAvramar;
            tvIncomingFrom.setText(Constants.ftAvramar);
        } else if (checkedId == R.id.tbSupplier) {
            GlobalState.recWHIncoming.from = Constants.ftSupplier;
            tvIncomingFrom.setText(Constants.ftSupplier);
        } else if (checkedId == R.id.tbAssetFrom) {
            GlobalState.recWHIncoming.from = Constants.ftAsset;
            tvIncomingFrom.setText(Constants.ftAsset);
        } else if (checkedId == R.id.tbSite) {
            GlobalState.recWHIncoming.to = Constants.ftSite;
            tvIncomingTo.setText(Constants.ftSite);
        } else if (checkedId == R.id.tbAssetTo) {
            GlobalState.recWHIncoming.to = Constants.ftAsset;
            tvIncomingTo.setText(Constants.ftAsset);
        }
        if (checkedId == R.id.tbAsset) {
            selectedIncomingItemType = Constants.ftAsset;
        } else if (checkedId == R.id.tbConsumable) {
            selectedIncomingItemType = Constants.ftConsumable;
        }
    }

    private WHTxRecord updateState() {
        WHTxRecord whIncomingRecord = GlobalState.recWHIncoming;

        if (!Strings.isEmptyOrWhitespace(selectedIncomingItemType)) {
            whIncomingRecord.incomingItemType = selectedIncomingItemType;
        }

        return whIncomingRecord;
    }

    private String validate(){
        StringBuilder sb = new StringBuilder();

        if (Strings.isEmptyOrWhitespace(GlobalState.recWHIncoming.incomingItemType)) {
            sb.append(String.format("\n%s is missing", "'Item type'"));
        }

        if(Strings.isEmptyOrWhitespace(GlobalState.recWHIncoming.from)){
            sb.append(String.format("\n%s is missing", "'Source site'"));
        }

        if(Strings.isEmptyOrWhitespace(GlobalState.recWHIncoming.to)){
            sb.append(String.format("\n%s is missing", "'Target site'"));
        }

        return sb.toString();
    }

    private void initControlsFromState()    {

        if (Constants.ftAvramar.equalsIgnoreCase(GlobalState.recWHIncoming.from)) {
            tgIncomingSource.check(R.id.tbAvramar);
        } else if (Constants.ftSupplier.equalsIgnoreCase(GlobalState.recWHIncoming.from)) {
            tgIncomingSource.check(R.id.tbSupplier);
        } else if (Constants.ftAsset.equalsIgnoreCase(GlobalState.recWHIncoming.from)) {
            tgIncomingSource.check(R.id.tbAssetFrom);
        }

        if (Constants.ftSite.equalsIgnoreCase(GlobalState.recWHIncoming.to)) {
            tgIncomingDestination.check(R.id.tbSite);
        } else if (Constants.ftAsset.equalsIgnoreCase(GlobalState.recWHIncoming.to)) {
            tgIncomingDestination.check(R.id.tbAssetTo);
        }

        if (Constants.ftAsset.equalsIgnoreCase(GlobalState.recWHIncoming.incomingItemType)) {
            tgIncomingItemType.check(R.id.tbAsset);
        } else if (Constants.ftConsumable.equalsIgnoreCase(GlobalState.recWHIncoming.incomingItemType)) {
            tgIncomingItemType.check(R.id.tbConsumable);
        }
    }
}