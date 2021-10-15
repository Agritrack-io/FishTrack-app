package io.agritrack.fish.ui.maintenance;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.common.util.Strings;

import java.util.List;

import io.agritrack.R;
import io.agritrack.data.db.MobileDB;
import io.agritrack.data.model.common.Supplier;
import io.agritrack.dialog.PhotoDialog;
import io.agritrack.dialog.SupportDialog;
import io.agritrack.fish.state.GlobalState;
import io.agritrack.fish.ui.HomeActivity;
import io.agritrack.fish.ui.process.ProcessInfoActivity;
import io.agritrack.ui.bo.GenericListModel;
import io.agritrack.ui.service.LocalPreferences;

import static io.agritrack.FishTrackApplication.getAppContext;
import static io.agritrack.common.LargeString.render;
import static io.agritrack.ui.custom.CustomToast.CToast;

public class MaintenanceExternalSupplierActivity extends AppCompatActivity implements OnItemClickListener {

    private static final int pic_id = 123;
    private final MutableLiveData<Bitmap> photoResult = new MutableLiveData<>();
    private PhotoDialog photoDialog;
    private ImageView ivTakenPhoto;

    private ListView lvSupplier;
    private EditText etMaintenanceManager, etMaintenanceCost, etMaintenanceTime, mtvExtRemarks;

    private MobileDB db;
    private GenericListModel[] availableSuppliers;

    private ImageView ivSupport;
    private SupportDialog supportDialog;

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
        db = MobileDB.getInstance(getAppContext());

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

        // Camera_open button is for open the camera
        // and add the setOnClickListener in this button
        ImageButton ivCamera = findViewById(R.id.ivCamera);
        ivCamera.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Create the camera_intent ACTION_IMAGE_CAPTURE
                // it will open the camera for capture the image
                Intent camera_intent
                        = new Intent(MediaStore
                        .ACTION_IMAGE_CAPTURE);

                // Start the activity with camera_intent,
                // and request pic id
                startActivityForResult(camera_intent, pic_id);
            }
        });

        photoResult.observe(this, response -> {
            if (response != null) {
                GlobalState.recExternalRepair.photoPath = System.currentTimeMillis() + "";
                ivTakenPhoto.setVisibility(View.VISIBLE);
            } else {
                ivTakenPhoto.setVisibility(View.GONE);
                GlobalState.recExternalRepair.photoPath = null;
            }
        });

        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(MaintenanceExternalSupplierActivity.this);
            supportDialog.showDialog();
        });

        configFooter();
    }

    // This method will help to retrieve the image
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // Match the request 'pic id with requestCode
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == pic_id) {

            // BitMap is data structure of image file
            // which stor the image in memory
            Bitmap photo = (Bitmap) data.getExtras().get("data");

            // Set the image in imageview for display
            photoResult.setValue(photo);

            photoDialog = new PhotoDialog(MaintenanceExternalSupplierActivity.this, photoResult, R.string.photo_taken);
            photoDialog.showDialog();
        }
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
            String v = validate();
            if (!Strings.isEmptyOrWhitespace(v)) {
                CToast(getApplicationContext(), render("Invalid inputs : " + v), Toast.LENGTH_LONG);
            } else {
                Intent i = new Intent(getApplicationContext(), MaintenanceExternalConfirmActivity.class);
                startActivity(i);
            }
        });
    }

    private void assignCtrlVars() {
        lvSupplier = findViewById(R.id.lvSupplier);
        etMaintenanceManager = findViewById(R.id.etMaintenanceManager);
        etMaintenanceCost = findViewById(R.id.etMaintenanceCost);
        etMaintenanceTime = findViewById(R.id.etMaintenanceTime);
        mtvExtRemarks = findViewById(R.id.mtvExtRemarks);
        mtvExtRemarks.setImeOptions(EditorInfo.IME_ACTION_DONE);
        mtvExtRemarks.setRawInputType(InputType.TYPE_CLASS_TEXT);
        ivTakenPhoto = findViewById(R.id.ivTakenPhoto);
        ivSupport = findViewById(R.id.ivSupport);
    }

    private void initControlsFromState() {
        /*if (GlobalState.recExternalRepair.repairTeam != null) {
            int sz = GlobalState.recExternalRepair.repairTeam.size();
            // Since coming from <back> button, retain the previously checked items.
            for (int i = 0; i < sz; i++) {
                this.lvSupplier.setItemChecked(GlobalState.recExternalRepair.repairTeam.get(i).intValue(), Boolean.TRUE);
            }
        }*/

        if (!Strings.isEmptyOrWhitespace(GlobalState.recExternalRepair.manager)) {
            etMaintenanceManager.setText(GlobalState.recExternalRepair.manager);
        }
        if (!Strings.isEmptyOrWhitespace(GlobalState.recExternalRepair.cost)) {
            etMaintenanceCost.setText(GlobalState.recExternalRepair.cost);
        }
        if (!Strings.isEmptyOrWhitespace(GlobalState.recExternalRepair.repairTime)) {
            etMaintenanceTime.setText(GlobalState.recExternalRepair.repairTime);
        }
        if (!Strings.isEmptyOrWhitespace(GlobalState.recExternalRepair.remarks)) {
            mtvExtRemarks.setText(GlobalState.recExternalRepair.remarks);
        }

        if (!Strings.isEmptyOrWhitespace(GlobalState.recExternalRepair.photoPath)) {
            ivTakenPhoto.setVisibility(View.VISIBLE);
        }
    }

    private void updateState() {
        int supLoc = this.lvSupplier.getCheckedItemPosition();
        GlobalState.recExternalRepair.supplierPos = supLoc;
        GlobalState.recExternalRepair.manager = etMaintenanceManager.getText().toString();
        GlobalState.recExternalRepair.cost = etMaintenanceCost.getText().toString();
        GlobalState.recExternalRepair.repairTime = etMaintenanceTime.getText().toString();
        GlobalState.recExternalRepair.remarks = mtvExtRemarks.getText().toString();
    }

    private String validate(){
        StringBuilder sb = new StringBuilder();

        /*if(Strings.isEmptyOrWhitespace(GlobalState.recExternalRepair.supplier)){
            sb.append(String.format("\n%s is missing", "'Supplier'"));
        }*/

        if(Strings.isEmptyOrWhitespace(GlobalState.recExternalRepair.manager)){
            sb.append(String.format("\n%s is missing", "'Maintenance manager'"));
        }

        if(Strings.isEmptyOrWhitespace(GlobalState.recExternalRepair.cost)){
            sb.append(String.format("\n%s is missing", "'Maintenance cost'"));
        }

        if(Strings.isEmptyOrWhitespace(GlobalState.recExternalRepair.repairTime)){
            sb.append(String.format("\n%s is missing", "'Maintenance time'"));
        }

        return sb.toString();
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