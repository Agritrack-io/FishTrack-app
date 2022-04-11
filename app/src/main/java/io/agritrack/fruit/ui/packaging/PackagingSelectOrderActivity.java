package io.agritrack.fruit.ui.packaging;

import static io.agritrack.FishTrackApplication.IsDemo;
import static io.agritrack.FishTrackApplication.getAppContext;
import static io.agritrack.common.LargeString.render;
import static io.agritrack.ui.custom.CustomToast.CToast;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.util.Strings;

import io.agritrack.R;
import io.agritrack.data.db.MobileDB;
import io.agritrack.data.model.HarvestRequest;
import io.agritrack.dialog.SupportDialog;
import io.agritrack.fish.state.GlobalState;
import io.agritrack.fruit.ui.FruitHomeActivity;
import io.agritrack.ui.service.LocalPreferences;

public class PackagingSelectOrderActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private MobileDB db;
    private ListView lvOpenOrders;
    private io.agritrack.ui.bo.GenericListModel[] orders;

    private ImageView ivSupport;
    private SupportDialog supportDialog;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_packaging_select_order);

        // get main controls references
        this.lvOpenOrders = findViewById(R.id.lvOpenOrders);

        // define if single or multiple choice mode will be used to display the checkboxes.
        this.lvOpenOrders.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        // get an instance of local DB
        db = MobileDB.getInstance(getAppContext());

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderPackagingSelectOrder);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // load Harvest Request fetched via Synch op.
        //List<Order> orders = db.orderDAO().getAll();
        /*if (orders != null && !orders.isEmpty()) {
            this.orders = orders.stream().map(x -> new io.agritrack.ui.bo.GenericListModel(x.id, String.format("%s, %s kg, %s", x.cageCode, x.reqQty, x.fishName))).toArray(io.agritrack.ui.bo.GenericListModel[]::new);
            ArrayAdapter<io.agritrack.ui.bo.GenericListModel> ordersAdapter = new ArrayAdapter<io.agritrack.ui.bo.GenericListModel>(this, android.R.layout.simple_list_item_checked, orders) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);
                    TextView text = view.findViewById(android.R.id.text1);
                    text.setTextSize(22);
                    return view;
                }
            };

            this.lvOpenOrders.setAdapter(ordersAdapter);
            this.lvOpenOrders.setOnItemClickListener(this);
        }*/

        ivSupport = findViewById(R.id.ivSupport);
        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(PackagingSelectOrderActivity.this);
            supportDialog.showDialog();
        });

        // create Footer
        configFooter();
    }

    // Define 'back' / 'next' Buttons functionality
    protected void configFooter() {
        ImageView ivNext = findViewById(R.id.ivToStartPackaging);
        ivNext.setOnClickListener(view -> {
            String v = validate();
            if (!Strings.isEmptyOrWhitespace(v)) {
                CToast(getApplicationContext(), render("Invalid inputs : " + v), Toast.LENGTH_LONG);
            } else {
                // NO open FishingTx exists, instantiate a new.
               /* FishingTransaction openTx = new FishingTransaction();
                openTx.txStatus = TxStatus.PENDING;
                GlobalState.recFishing.txKey = db.fishingTransactionDAO().insert(openTx);*/

                // transfer existing Record Data to Entity and persist to db.
               /* GlobalState.commitFishing(this.db, false);*/

                Intent i = new Intent(getApplicationContext(), PackagingStartActivity.class);
                startActivity(i);
            }
        });

        ImageView ivBack = findViewById(R.id.ivBackToFruitHome);
        ivBack.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), FruitHomeActivity.class);
            startActivity(i);
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        CheckedTextView v = (CheckedTextView) view;
        boolean currentCheck = v.isChecked();
        io.agritrack.ui.bo.GenericListModel member = (io.agritrack.ui.bo.GenericListModel) this.lvOpenOrders.getItemAtPosition(position);
        member.setChecked(!currentCheck);

        HarvestRequest harvestRq = db.harvestRequestsDAO().getById(member.getId().toString());
        if (harvestRq != null) {
            GlobalState.recFishing.harvestRqPkId = harvestRq.id;
            GlobalState.recFishing.harvestRq = harvestRq.requestId;
            GlobalState.recFishing.speciesName = harvestRq.species;
            GlobalState.recFishing.cageCode = harvestRq.cageCode;
            GlobalState.recFishing.cageRFID = harvestRq.cageRFID;
            GlobalState.recFishing.requesterName = harvestRq.requester;
            GlobalState.recFishing.fishSize = harvestRq.fishSize;
            GlobalState.recFishing.reqWeight = harvestRq.reqQty;
            GlobalState.recFishing.notes = harvestRq.notes;
            GlobalState.recFishing.packagingPlant = harvestRq.packagingPlant;
        }
    }

    private String validate() {
        StringBuilder sb = new StringBuilder();
        if(!IsDemo) {
            if (Strings.isEmptyOrWhitespace(GlobalState.recFishing.speciesName)) {
                sb.append(String.format("Please Select a Harvest Request to proceed", ""));
            }
        }

        return sb.toString();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}