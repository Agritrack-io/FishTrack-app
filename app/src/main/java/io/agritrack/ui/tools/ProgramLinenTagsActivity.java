package io.agritrack.ui.tools;

import static io.agritrack.FishTrackApplication.getAppContext;
import static io.agritrack.common.LargeString.render;
import static io.agritrack.ui.custom.CustomToast.CToast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.util.Strings;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import io.agritrack.R;
import io.agritrack.data.db.MobileDB;
import io.agritrack.data.service.EncodingSchemeService;
import io.agritrack.dialog.SimpleListDialog;
import io.agritrack.dialog.SupportDialog;
import io.agritrack.hotel.ui.HotelHomeActivity;
import io.agritrack.hotel.ui.incoming.HotelIncomingLinenActivity;
import io.agritrack.hotel.ui.incoming.HotelIncomingStartActivity;
import io.agritrack.ui.login.LoginActivity;
import io.agritrack.ui.service.LocalPreferences;

public class ProgramLinenTagsActivity extends AppCompatActivity {

    private static final EncodingSchemeService schemeSvc = EncodingSchemeService.getInstance();
    private SimpleListDialog linenTypeDialog;
    private MobileDB db;
    private Map<String, String> typeEPCSMap;
    private TextView tvProductCode, tvItemsCnt;
    private Button btnLinenType, btnWriteEPC;
    private EditText etNextEPC;
    private RecyclerView rvEPCsPerType;
    private ImageView ivProgOutcome;
    private String currentType;
    private final MutableLiveData<String> currentTypeSelection = new MutableLiveData<>();

    private ImageView ivSupport;
    private SupportDialog supportDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_program_linen_tags);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderProgramLinen);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get an instance of local DB
        db = MobileDB.getInstance(getAppContext());

        this.typeEPCSMap = new HashMap<>();

        // get  references of the controls
        assignCtrlVars();

        currentTypeSelection.observe(this, response -> {
            if (response != null) {
                currentType = response;
                btnLinenType.setText(currentType);
                tvProductCode.setText(schemeSvc.codeOf(currentType));
                linenTypeDialog.dismiss();
                if (!Strings.isEmptyOrWhitespace(currentType)){
                    String currentEPC = this.typeEPCSMap.get(currentType);
                    if (!Strings.isEmptyOrWhitespace(currentEPC)){
                        etNextEPC.setText(currentEPC);
                    }
                }
            }
        });

        btnLinenType.setOnClickListener(view -> {
            linenTypeDialog = new SimpleListDialog(ProgramLinenTagsActivity.this, Arrays.asList(schemeSvc.distinctNamesOnly()), currentTypeSelection, R.string.type_linen);
            linenTypeDialog.showDialog();
        });

        btnWriteEPC.setOnClickListener(view -> {
            Editable currSerialNumber = etNextEPC.getText();
        });

        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(ProgramLinenTagsActivity.this);
            supportDialog.showDialog();
        });

        configFooter();
    }

    private void assignCtrlVars() {
        tvProductCode = findViewById(R.id.tvProductCode);
        etNextEPC = findViewById(R.id.etNextEPC);
        btnLinenType = findViewById(R.id.btnLinenType);
        ivProgOutcome = findViewById(R.id.ivProgOutcome);
        tvItemsCnt = findViewById(R.id.tvItemsCnt);
        btnWriteEPC = findViewById(R.id.btnWriteEPC);
        rvEPCsPerType = findViewById(R.id.rvEPCsPerType);
        ivSupport = findViewById(R.id.ivSupport);
    }

    protected void configFooter() {
        ImageView ivNext = findViewById(R.id.ivToCongs);
        ivNext.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(i);
        });
    }
}