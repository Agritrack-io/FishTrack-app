package io.agritrack.fishtrack.ui.wh.inventory;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.util.Strings;

import java.util.ArrayList;

import io.agritrack.fishtrack.R;
import io.agritrack.fishtrack.barcode.BarcodeScanService;
import io.agritrack.fishtrack.barcode.SoundUtil;
import io.agritrack.fishtrack.dialog.YesNoDialogFragment;
import io.agritrack.fishtrack.state.InventoryWHRecord;
import io.agritrack.fishtrack.ui.WhMenuActivity;
import io.agritrack.fishtrack.ui.adapter.BarcodeRecyclerAdapter;
import io.agritrack.fishtrack.ui.service.LocalPreferences;

import static io.agritrack.fishtrack.common.LargeString.render;

public class InventoryConsumableActivity extends AppCompatActivity {

    private final String TAG = "InventoryConsumableActivity";

    private RecyclerView rvInventoryItems;
    private TextView tvInventoryItemsCount;
    private InventoryWHRecord whInventoryRecord;
    private boolean scanning = false;
    private BarcodeScanService scanService;
    private BarcodeRecyclerAdapter adapterInventoryItems;
    private String selectedBarcode;
    private ConstraintLayout selectedItem;

    // BroadcastReceiver to receiver scan data
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            byte[] data = intent.getByteArrayExtra("data");
            if (data != null) {
                String barcode = new String(data);
                adapterInventoryItems.addItem(barcode);
                adapterInventoryItems.notifyDataSetChanged();
                tvInventoryItemsCount.setText("# "+String.valueOf(adapterInventoryItems.getItemCount()));
                scanning = false;
            }
        }
    };

    // Instantiate a clickListener to be passed to adapterIncomingItems.
    // It will be used to set the selectedBarcode var to the selected item barcode.
    private final View.OnClickListener itemsOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ConstraintLayout view = (ConstraintLayout) v;
            TextView tvRecyclerItem = view.findViewById(R.id.tvItemDescription);
            selectedBarcode = tvRecyclerItem.getText().toString();

            if(selectedItem!=null) {
                selectedItem.setBackground(getResources().getDrawable(R.drawable.list_item_bottom, null));
            }

            v.setSelected(true);
            view.setBackgroundColor(Color.GRAY);
            selectedItem = view;
        }
    };

    private ImageButton ivAddItem, ivDeleteItem;
    private Button btnScanConsumable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory_consumable);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderInventory);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get  references of the controls
        assignCtrlVars();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvInventoryItems.setLayoutManager(layoutManager);
        rvInventoryItems.setItemAnimator(new DefaultItemAnimator());
        adapterInventoryItems = new BarcodeRecyclerAdapter(this, new ArrayList<>(), itemsOnClickListener);
        rvInventoryItems.setAdapter(adapterInventoryItems);
        rvInventoryItems.setNestedScrollingEnabled(false);

        // initiate raw sound
        SoundUtil.initSoundPool(this);

        //Register receiver to receive the result of scan
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.rfid.SCAN");
        registerReceiver(receiver, filter);

        ivDeleteItem.setOnClickListener(view -> {

            if (!Strings.isEmptyOrWhitespace(selectedBarcode)) {
                // instantiate Site selection confirm dialog
                YesNoDialogFragment confirmSiteSelectionDlg = YesNoDialogFragment.instance();
                confirmSiteSelectionDlg.args().putString("selectedBarcode", selectedBarcode);
                confirmSiteSelectionDlg.setMessage(getText(R.string.delete_selected_item) + selectedBarcode);

                confirmSiteSelectionDlg.onConfirm(bundle -> {
                    String barcode = bundle.getString("selectedBarcode");
                    if (barcode != null) {
                        adapterInventoryItems.removeItem(barcode);
                        adapterInventoryItems.notifyDataSetChanged();
                        tvInventoryItemsCount.setText(String.valueOf(adapterInventoryItems.getItemCount()));
                        selectedBarcode = null;
                    }
                });

                FragmentManager fm = getSupportFragmentManager();
                confirmSiteSelectionDlg.showNow(fm, getString(R.string.confirm_selection));
                clearSelectedItem();
            } else {
                // <delete> Button was pressed without selecting a Bin first.
                Toast.makeText(getApplicationContext(), render("Plz select a Item to delete!!"), Toast.LENGTH_LONG).show();
            }
        });

       /* ivAddItem.setOnClickListener(view -> {
            showAddDialog();
        });*/

        btnScanConsumable.setOnClickListener(view -> {
            clearSelectedItem();
            if (!scanning) {
                startScanning();
            } else {
                stopScanning();
            }
        });

        configFooter();
    }

    private void clearSelectedItem(){
        if(selectedItem!=null) {
            selectedItem.setBackground(getResources().getDrawable(R.drawable.list_item_bottom, null));
        }
    }

    private void startScanning() {
        if (scanService != null) {
            scanning = true;
            scanService.scan();
        }
    }

    private void stopScanning() {
        if (scanService != null) {
            scanService.stopScan();
            scanning = false;
        }
    }

    private void assignCtrlVars() {
        rvInventoryItems = findViewById(R.id.rvInventoryItems);
        tvInventoryItemsCount = findViewById(R.id.tvInventoryItemsCount);
        ivDeleteItem = findViewById(R.id.ivDeleteItem);
        ivAddItem = findViewById(R.id.ivAddItem);
        btnScanConsumable = findViewById(R.id.btnScanConsumable);
    }

 /*   private void showAddDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Type item BARCODE");

        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                itemBarcode = input.getText().toString();
                adapterInventoryItems.addItem(itemBarcode);
                adapterInventoryItems.notifyDataSetChanged();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();

    }*/

    protected void configFooter() {
        ImageView ivNext = findViewById(R.id.ivToCongs);
        ivNext.setOnClickListener(view -> {

            //Set scanning to false to stop running scan thread
            scanning = false;
            stopScanning();

            String v = validate();
            if (!Strings.isEmptyOrWhitespace(v)) {
                Toast.makeText(getApplicationContext(), render("Invalid inputs : " + v), Toast.LENGTH_LONG).show();
            } else {
                Intent i = new Intent(getApplicationContext(), WhMenuActivity.class);
                startActivity(i);
            }
        });

        ImageView ivBack = findViewById(R.id.ivBackToWhMenu);
        ivBack.setOnClickListener(view -> {
            //Set scanning to false to stop running scan thread
            scanning = false;
            stopScanning();

            Intent i = new Intent(getApplicationContext(), InventoryStartActivity.class);
            startActivity(i);
        });
    }

    private String validate() {
        StringBuilder sb = new StringBuilder();

     /*If(GlobalState.recWHInventory.items==null || GlobalState.recWHIncoming.items.isEmpty()){
            sb.append(String.format("\n%s is missing", "'Incoming items'"));
        }*/

        return sb.toString();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (scanService == null) {
            scanService = new BarcodeScanService(this);
            //we must set mode to 0 : BroadcastReceiver mode
            scanService.setScanMode(0);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (scanService != null) {
            scanService.setScanMode(1);
            scanService.close();
            scanService = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }
}