package io.agritrack.fishtrack.ui.wh.inventory;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.gms.common.util.Strings;

import io.agritrack.fishtrack.R;
import io.agritrack.fishtrack.barcode.ScanUtility;
import io.agritrack.fishtrack.barcode.SoundUtil;
import io.agritrack.fishtrack.common.Constants;
import io.agritrack.fishtrack.common.Filters;
import io.agritrack.fishtrack.state.GlobalState;
import io.agritrack.fishtrack.state.InventoryWHRecord;
import io.agritrack.fishtrack.ui.WhMenuActivity;
import io.agritrack.fishtrack.ui.adapter.TreelikeAdapter;
import io.agritrack.fishtrack.ui.custom.ToggleGroup;
import io.agritrack.fishtrack.ui.service.LocalPreferences;

import static io.agritrack.fishtrack.common.LargeString.render;

public class InventoryConsumableActivity extends AppCompatActivity implements ToggleGroup.OnCheckedChangeListener {
    private String TAG = "InventoryConsumableActivity";
    private  ToggleGroup tgChooseConsumableType;

    private ExpandableListView xvInventoryItems;
    private TextView tvInventoryItemsCount;
    private InventoryWHRecord whInventoryRecord;

    private boolean scanning = false;
    private ScanUtility scanUtil;

    private TreelikeAdapter adapterInventoryItems;
    private String selectedConsumableType;
    private String activeFilter = null;
    private int selectedToggleButton = -1;
    private ImageButton ivAddItem, ivDeleteItem;
    private Button btnScanConsumable;
    private String selectedBarcode;
    private ConstraintLayout selectedItem;

    private String itemBarcode;


    // Instantiate a clickListener to be passed to adapterIncomingItems.
    // It will be used to set the selectedBarcode var to the selected item barcode.
    private final View.OnClickListener itemsClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ConstraintLayout view = (ConstraintLayout) v;
            TextView tvRecyclerItem = view.findViewById(R.id.tvRecyclerItem);
            selectedBarcode = tvRecyclerItem.getText().toString();

            if(selectedItem!=null) {
                selectedItem.setBackground(getResources().getDrawable(R.drawable.list_item_bottom, null));
            }

            v.setSelected(true);
            view.setBackgroundColor(Color.GRAY);
            selectedItem = view;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory_consumable);

        // instantiate an inventory Record
        whInventoryRecord = GlobalState.recWHInventory;

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderInventory);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get  references of the controls
        assignCtrlVars();

        // initiate raw sound
        SoundUtil.initSoundPool(this);

        //Register receiver to receive the result of scan
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.rfid.SCAN");
        registerReceiver(receiver, filter);

        // initiate RFID scanner behaviour
//        scanResult.observe(this, response -> {
//            if (response == null) {
//                return;
//            }
//            Map<String, List<String>> values = response.stream().collect(Collectors.groupingBy(g -> g.substring(0, 4), Collectors.toCollection(ArrayList::new)));;//(SiteInfo::getLevel2, Collectors.toCollection(ArrayList::new)));
//
//            adapterInventoryItems = new TreelikeAdapter(this, values);
//            xvInventoryItems.setAdapter(adapterInventoryItems);
//
//            tvInventoryItemsCount.setText(String.valueOf(response.size()));
//            adapterInventoryItems.notifyDataSetChanged();
//        });

        // initialize scanning threads

  /*      ivDeleteItem.setOnClickListener(view -> {
            clearSelectedItem();

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
            } else {
                // <delete> Button was pressed without selecting a Bin first.
                Toast.makeText(getApplicationContext(), render("Plz select a Item to delete!!"), Toast.LENGTH_LONG).show();
            }
        });*/

       /* ivAddItem.setOnClickListener(view -> {
            showAddDialog();
        });*/

        btnScanConsumable.setOnClickListener(view ->{
            scanning = !scanning;

            if(scanning) {
                startScanning();
            } else {
                stopScanning();
            }
        });

        configFooter();
    }

    private void startScanning() {
        if (scanUtil != null) {
            scanUtil.scan();
        }
    }

    private void stopScanning() {
        if (scanUtil != null) {
            scanUtil.stopScan();
        }
    }

    private void clearSelectedItem(){
        if(selectedItem!=null) {
            selectedItem.setBackground(getResources().getDrawable(R.drawable.list_item_bottom, null));
        }
    }

    private void assignCtrlVars() {
        tgChooseConsumableType = findViewById(R.id.tgChooseConsumableType);
        xvInventoryItems = findViewById(R.id.xvInventoryItems);
        tvInventoryItemsCount = findViewById(R.id.tvInventoryItemsCount);
        ivDeleteItem = findViewById(R.id.ivDeleteItem);
        ivAddItem = findViewById(R.id.ivAddItem);
        btnScanConsumable = findViewById(R.id.btnScanConsumable);

        tgChooseConsumableType.setOnCheckedChangeListener(this);
    }

    protected void configFooter() {
        ImageView ivNext = (ImageView) findViewById(R.id.ivToCongs);
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

        ImageView ivBack = (ImageView) findViewById(R.id.ivBackToWhMenu);
        ivBack.setOnClickListener(view -> {

            //Set scanning to false to stop running scan thread
            scanning = false;
            stopScanning();

            Intent i = new Intent(getApplicationContext(), InventoryStartActivity.class);
            startActivity(i);
        });
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

    @Override
    public void onCheckedChanged(ToggleGroup group, int checkedId) {

        if( selectedToggleButton == checkedId){
            group.clearCheck();
            return;
        }
        selectedToggleButton = checkedId;
        switch(checkedId){
            case R.id.tbFood:
                selectedConsumableType = Constants.ftFood;
                activeFilter = Filters.BARCODE_FOOD;
                break;
            case R.id.tbVaccine:
                selectedConsumableType = Constants.ftVaccine;
                activeFilter = Filters.BARCODE_VACCINE;
                break;
            case R.id.tbAntibiotic:
                selectedConsumableType = Constants.ftAntibiotic;
                activeFilter = Filters.BARCODE_ANTIBIOTIC;
                break;
            default:
                selectedConsumableType = null;
                activeFilter = null;
                selectedToggleButton = -1;
                break;
        }
    }

    private String validate(){
        StringBuilder sb = new StringBuilder();

     /*If(GlobalState.recWHInventory.items==null || GlobalState.recWHIncoming.items.isEmpty()){
            sb.append(String.format("\n%s is missing", "'Incoming items'"));
        }*/

        return sb.toString();
    }


    // BroadcastReceiver to receiver scan data
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            byte[] data = intent.getByteArrayExtra("data");
            if (data != null) {
                String barcode = new String(data);
//                adapterInventoryItems.addItem( barcode);

                //first add
//                if (setBarcode.isEmpty()) {
//                    setBarcode.add(barcode);
//                    listBarcode = new ArrayList<>();
//                    BarcodeTag b = new BarcodeTag();
//                    b.sn = 1;
//                    b.barcode = barcode;
//                    b.count = 1;
//                    listBarcode.add(b);
//                    //list index
//                    mapBarcode.put(barcode, 0);
////                    adapter = new MAdapter();
////                    lv.setAdapter(adapter);
//                } else {
//                    if (setBarcode.contains(barcode)) {
//                        BarcodeTag b = listBarcode.get(mapBarcode.get(barcode));
//                        b.count += 1;
//                        listBarcode.set(mapBarcode.get(barcode), b);
//
//                    } else {
//                        BarcodeTag b = new BarcodeTag();
//                        b.sn = listBarcode.size();
//                        b.barcode = barcode;
//                        b.count = 1;
//                        listBarcode.add(b);
//                        setBarcode.add(barcode);
//                        //list index
//                        mapBarcode.put(barcode, listBarcode.size() - 1);
//                    }
//                }

//                adapterInventoryItems.notifyDataSetChanged();
//                scanning = false;
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        if (scanUtil == null) {
            scanUtil = new ScanUtility(this);
            //we must set mode to 0 : BroadcastReceiver mode
            scanUtil.setScanMode(0);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (scanUtil != null) {
            scanUtil.setScanMode(1);
            scanUtil.close();
            scanUtil = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }
}