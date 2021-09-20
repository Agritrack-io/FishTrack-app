package io.agritrack.fishtrack.ui.wh.outgoing;

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
import io.agritrack.fishtrack.ui.custom.CustomToast;
import io.agritrack.fishtrack.ui.custom.ToggleGroup;
import io.agritrack.fishtrack.ui.service.LocalPreferences;
import io.agritrack.fishtrack.ui.wh.incoming.IncomingAssetActivity;
import io.agritrack.fishtrack.ui.wh.incoming.IncomingConsumableActivity;

import static io.agritrack.fishtrack.common.LargeString.render;
import static io.agritrack.fishtrack.ui.custom.CustomToast.CToast;

public class OutgoingStartActivity extends AppCompatActivity implements ToggleGroup.OnCheckedChangeListener {
    private TextView tvOutgoingFrom, tvOutgoingTo;
    private ToggleGroup tgOutgoingSource, tgOutgoingDestination, tgOutgoingItemType;
    private String selectedOutgoingItemType;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outgoing_start);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderOutgoingStart);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get  references of the controls
        assignCtrlVars();

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
                CToast(getApplicationContext(), render("Invalid inputs : " + v), Toast.LENGTH_LONG);
            } else if (selectedOutgoingItemType == Constants.ftAsset){
                Intent i = new Intent(getApplicationContext(), OutgoingAssetActivity.class);
                startActivity(i);
            } else {
                Intent i = new Intent(getApplicationContext(), OutgoingConsumableActivity.class);
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
        tvOutgoingFrom = findViewById(R.id.tvOutgoingFrom);
        tvOutgoingTo = findViewById(R.id.tvOutgoingTo);

        tgOutgoingSource = findViewById(R.id.tgOutgoingSource);
        tgOutgoingDestination = findViewById(R.id.tgOutgoingDestination);

        tgOutgoingItemType = findViewById(R.id.tgOutgoingItemType);

        tgOutgoingItemType.setOnCheckedChangeListener(this);

        tgOutgoingSource.setOnCheckedChangeListener(this);
        tgOutgoingDestination.setOnCheckedChangeListener(this);
    }

    @Override
    public void onCheckedChanged(ToggleGroup group, int checkedId) {
        if (checkedId == R.id.tbSite) {
            GlobalState.recWHOutgoing.from = Constants.ftSite;
            tvOutgoingFrom.setText(Constants.ftSite);
        } else if (checkedId == R.id.tbAssetFrom) {
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
        if (checkedId == R.id.tbAsset) {
            selectedOutgoingItemType = Constants.ftAsset;
        } else if (checkedId == R.id.tbConsumable) {
            selectedOutgoingItemType = Constants.ftConsumable;
        }
    }

    private WHTxRecord updateState() {
        WHTxRecord whOutgoingRecord = GlobalState.recWHOutgoing;

        if (!Strings.isEmptyOrWhitespace(selectedOutgoingItemType)) {
            whOutgoingRecord.outgoingItemType = selectedOutgoingItemType;
        }
        return whOutgoingRecord;
    }

    private String validate(){
        StringBuilder sb = new StringBuilder();

        if (Strings.isEmptyOrWhitespace(GlobalState.recWHOutgoing.outgoingItemType)) {
            sb.append(String.format("\n%s is missing", "'Item type'"));
        }

        if(Strings.isEmptyOrWhitespace(GlobalState.recWHOutgoing.to)){
            sb.append(String.format("\n%s is missing", "'Target site'"));
        }

        if(Strings.isEmptyOrWhitespace(GlobalState.recWHOutgoing.from)){
            sb.append(String.format("\n%s is missing", "'Source site'"));
        }

        return sb.toString();
    }

    private void initControlsFromState()    {

        if (Constants.ftSite.equalsIgnoreCase(GlobalState.recWHOutgoing.from)) {
            tgOutgoingSource.check(R.id.tbSite);
        } else if (Constants.ftAsset.equalsIgnoreCase(GlobalState.recWHOutgoing.from)) {
            tgOutgoingSource.check(R.id.tbAssetFrom);
        }

        if (Constants.ftAvramar.equalsIgnoreCase(GlobalState.recWHOutgoing.to)) {
            tgOutgoingDestination.check(R.id.tbAvramar);
        } else if (Constants.ftCustomer.equalsIgnoreCase(GlobalState.recWHOutgoing.to)) {
            tgOutgoingDestination.check(R.id.tbCustomer);
        } else if (Constants.ftAsset.equalsIgnoreCase(GlobalState.recWHOutgoing.to)) {
            tgOutgoingDestination.check(R.id.tbOutAssetTo);
        }

        if (Constants.ftAsset.equalsIgnoreCase(GlobalState.recWHOutgoing.outgoingItemType)) {
            tgOutgoingItemType.check(R.id.tbAsset);
        } else if (Constants.ftConsumable.equalsIgnoreCase(GlobalState.recWHOutgoing.outgoingItemType)) {
            tgOutgoingItemType.check(R.id.tbConsumable);
        }
    }
}