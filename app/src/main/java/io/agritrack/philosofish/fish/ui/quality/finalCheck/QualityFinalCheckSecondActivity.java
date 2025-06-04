package io.agritrack.philosofish.fish.ui.quality.finalCheck;

import static io.agritrack.philosofish.FishTrackApplication.getAppContext;
import static io.agritrack.philosofish.common.LargeString.render;
import static io.agritrack.philosofish.fish.state.GlobalState.recQualityFinal;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.util.Strings;

import io.agritrack.philosofish.R;
import io.agritrack.philosofish.api.APIServiceGenerator;
import io.agritrack.philosofish.api.tx.TransactionApi;
import io.agritrack.philosofish.api.upload.UploadingApi;
import io.agritrack.philosofish.common.InputFilterMinMax;
import io.agritrack.philosofish.data.db.MobileDB;
import io.agritrack.philosofish.dialog.SupportDialog;
import io.agritrack.philosofish.dialog.YesNoDialogFragment;
import io.agritrack.philosofish.fish.state.FinalQualityRecord;
import io.agritrack.philosofish.fish.state.GlobalState;
import io.agritrack.philosofish.ui.service.LocalPreferences;

public class QualityFinalCheckSecondActivity extends AppCompatActivity  {

    private final UploadingApi upldSvc = APIServiceGenerator.createAPI(UploadingApi.class);
    private final TransactionApi updService = APIServiceGenerator.createAPI(TransactionApi.class);
    private MobileDB db;
    private YesNoDialogFragment confirmAllOkDlg;
    private EditText size1, size2, size3, type1, type2, type3, number1, number2, number3, actual1, actual2, actual3,
            under11, under12, under13,under21,under22, under23, under31, under32, under33 , over11, over12, over13, over21, over22, over23,
            over31, over32, over33 ,net1, net2, net3, ice1, ice2, ice3, temp1, temp2, temp3;
    private ProgressDialog progressDialog;
    private ImageView ivSupport, ivNext, ivBack;
    private boolean proceedWithoutLocation = false;
    private SupportDialog supportDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quality_final_check_second);

        // set Header Info
        TextView tvHeader = findViewById(R.id.tvHeaderFinalQuality);
        tvHeader.setText(LocalPreferences.HeaderMsg());

        // get an instance of local DB
        this.db = MobileDB.getInstance(getAppContext());

        // get  references of the controls
        assignCtrlVars();

        confirmAllOkDlg = YesNoDialogFragment.instance();
        confirmAllOkDlg.setMessage(getText(R.string.confirm_submission));
        confirmAllOkDlg.onConfirm(bundle -> {
        });
        confirmAllOkDlg.onReject(bundle -> {
            //proceedWithoutLocation = false;
        });

        // instantiate ProgressDialog and set style.
        progressDialog = new ProgressDialog(QualityFinalCheckSecondActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        // set (any?) previously selected values to activity Controls.
        initControlsFromState();


        ivSupport.setOnClickListener(view -> {
            supportDialog = new SupportDialog(QualityFinalCheckSecondActivity.this);
            supportDialog.showDialog();
        });

        configFooter();
    }


    protected void configFooter() {
        ivNext.setOnClickListener(v -> {
            updateState();
            Intent i = new Intent(getApplicationContext(), QualityFinalCheckConfirmActivity.class);
            startActivity(i);
        });

        ivBack.setOnClickListener(view -> {
            updateState();
            Intent i = new Intent(getApplicationContext(), QualityFinalCheckActivity.class);
            startActivity(i);
        });
    }

    private void assignCtrlVars() {
        size1 = findViewById(R.id.etSize1);
        type1 = findViewById(R.id.etType1);
        number1 = findViewById(R.id.etNumber1);
        actual1 = findViewById(R.id.etActual1);
        under11 = findViewById(R.id.etUnder11);
        under12 = findViewById(R.id.etUnder12);
        under13 = findViewById(R.id.etUnder13);
        over11 = findViewById(R.id.etOver11);
        over12 = findViewById(R.id.etOver12);
        over13 = findViewById(R.id.etOver13);
        net1 = findViewById(R.id.etNet1);
        ice1 = findViewById(R.id.etIce1);
        ice1.setFilters(new InputFilter[]{new InputFilterMinMax(0, 100)});
        temp1 = findViewById(R.id.etTemp1);
        size2 = findViewById(R.id.etSize2);
        type2 = findViewById(R.id.etType2);
        number2 = findViewById(R.id.etNumber2);
        actual2 = findViewById(R.id.etActual2);
        under21 = findViewById(R.id.etUnder21);
        under22 = findViewById(R.id.etUnder22);
        under23 = findViewById(R.id.etUnder23);
        over21 = findViewById(R.id.etOver21);
        over22 = findViewById(R.id.etOver22);
        over23 = findViewById(R.id.etOver23);
        net2 = findViewById(R.id.etNet2);
        ice2 = findViewById(R.id.etIce2);
        ice2.setFilters(new InputFilter[]{new InputFilterMinMax(0, 100)});
        temp2 = findViewById(R.id.etTemp2);
        size3 = findViewById(R.id.etSize3);
        type3 = findViewById(R.id.etType3);
        number3 = findViewById(R.id.etNumber3);
        actual3 = findViewById(R.id.etActual3);
        under31 = findViewById(R.id.etUnder31);
        under32 = findViewById(R.id.etUnder32);
        under33 = findViewById(R.id.etUnder33);
        over31 = findViewById(R.id.etOver31);
        over32 = findViewById(R.id.etOver32);
        over33 = findViewById(R.id.etOver33);
        net3 = findViewById(R.id.etNet3);
        ice3 = findViewById(R.id.etIce3);
        ice3.setFilters(new InputFilter[]{new InputFilterMinMax(0, 100)});
        temp3 = findViewById(R.id.etTemp3);
        ivSupport = findViewById(R.id.ivSupport);
        ivNext = findViewById(R.id.ivToFinalQualityConfirm);
        ivBack = findViewById(R.id.ivBackToQualityMenu);
    }

    private void initControlsFromState() {
        FinalQualityRecord qltRecord = GlobalState.recQualityFinal;

        if (!Strings.isEmptyOrWhitespace(recQualityFinal.sample1.size)) {
            size1.setText(recQualityFinal.sample1.size);
        }
        if (recQualityFinal.sample1.boxType != null) {
            type1.setText(String.valueOf(recQualityFinal.sample1.boxType));
        }
        if (recQualityFinal.sample1.labelPieces != null) {
            number1.setText(String.valueOf(recQualityFinal.sample1.labelPieces));
        }
        if (recQualityFinal.sample1.countedPieces != null) {
            actual1.setText(String.valueOf(recQualityFinal.sample1.countedPieces));
        }
        if (recQualityFinal.sample1.underWeight1 != null) {
            under11.setText(String.valueOf(recQualityFinal.sample1.underWeight1));
        }
        if (recQualityFinal.sample1.underWeight2 != null) {
            under12.setText(String.valueOf(recQualityFinal.sample1.underWeight2));
        }
        if (recQualityFinal.sample1.underWeight3 != null) {
            under13.setText(String.valueOf(recQualityFinal.sample1.underWeight3));
        }
        if (recQualityFinal.sample1.overWeight1 != null) {
            over11.setText(String.valueOf(recQualityFinal.sample1.overWeight1));
        }
        if (recQualityFinal.sample1.overWeight2 != null) {
            over12.setText(String.valueOf(recQualityFinal.sample1.overWeight2));
        }
        if (recQualityFinal.sample1.overWeight3 != null) {
            over13.setText(String.valueOf(recQualityFinal.sample1.overWeight3));
        }
        if (recQualityFinal.sample1.netWeight != null) {
            net1.setText(String.valueOf(recQualityFinal.sample1.netWeight));
        }
        if (recQualityFinal.sample1.iceQuantity != null) {
            ice1.setText(String.valueOf(recQualityFinal.sample1.iceQuantity));
        }
        if (recQualityFinal.sample1.fishTemp != null) {
            temp1.setText(String.valueOf(recQualityFinal.sample1.fishTemp));
        }


        if (!Strings.isEmptyOrWhitespace(recQualityFinal.sample2.size)) {
            size2.setText(recQualityFinal.sample2.size);
        }
        if (recQualityFinal.sample2.boxType != null) {
            type2.setText(String.valueOf(recQualityFinal.sample2.boxType));
        }
        if (recQualityFinal.sample2.labelPieces != null) {
            number2.setText(String.valueOf(recQualityFinal.sample2.labelPieces));
        }
        if (recQualityFinal.sample2.countedPieces != null) {
            actual2.setText(String.valueOf(recQualityFinal.sample2.countedPieces));
        }
        if (recQualityFinal.sample2.underWeight1 != null) {
            under21.setText(String.valueOf(recQualityFinal.sample2.underWeight1));
        }
        if (recQualityFinal.sample2.underWeight2 != null) {
            under22.setText(String.valueOf(recQualityFinal.sample2.underWeight2));
        }
        if (recQualityFinal.sample2.underWeight3 != null) {
            under23.setText(String.valueOf(recQualityFinal.sample2.underWeight3));
        }
        if (recQualityFinal.sample2.overWeight1 != null) {
            over21.setText(String.valueOf(recQualityFinal.sample2.overWeight1));
        }
        if (recQualityFinal.sample2.overWeight2 != null) {
            over22.setText(String.valueOf(recQualityFinal.sample2.overWeight2));
        }
        if (recQualityFinal.sample2.overWeight3 != null) {
            over23.setText(String.valueOf(recQualityFinal.sample2.overWeight3));
        }
        if (recQualityFinal.sample2.netWeight != null) {
            net2.setText(String.valueOf(recQualityFinal.sample2.netWeight));
        }
        if (recQualityFinal.sample2.iceQuantity != null) {
            ice2.setText(String.valueOf(recQualityFinal.sample2.iceQuantity));
        }
        if (recQualityFinal.sample2.fishTemp != null) {
            temp2.setText(String.valueOf(recQualityFinal.sample2.fishTemp));
        }


        if (!Strings.isEmptyOrWhitespace(recQualityFinal.sample3.size)) {
            size3.setText(recQualityFinal.sample3.size);
        }
        if (recQualityFinal.sample3.boxType != null) {
            type3.setText(String.valueOf(recQualityFinal.sample3.boxType));
        }
        if (recQualityFinal.sample3.labelPieces != null) {
            number3.setText(String.valueOf(recQualityFinal.sample3.labelPieces));
        }
        if (recQualityFinal.sample3.countedPieces != null) {
            actual3.setText(String.valueOf(recQualityFinal.sample3.countedPieces));
        }
        if (recQualityFinal.sample3.underWeight1 != null) {
            under31.setText(String.valueOf(recQualityFinal.sample3.underWeight1));
        }
        if (recQualityFinal.sample3.underWeight2 != null) {
            under32.setText(String.valueOf(recQualityFinal.sample3.underWeight2));
        }
        if (recQualityFinal.sample3.underWeight3 != null) {
            under33.setText(String.valueOf(recQualityFinal.sample3.underWeight3));
        }
        if (recQualityFinal.sample3.overWeight1 != null) {
            over31.setText(String.valueOf(recQualityFinal.sample3.overWeight1));
        }
        if (recQualityFinal.sample3.overWeight2 != null) {
            over32.setText(String.valueOf(recQualityFinal.sample3.overWeight2));
        }
        if (recQualityFinal.sample3.overWeight3 != null) {
            over33.setText(String.valueOf(recQualityFinal.sample3.overWeight3));
        }
        if (recQualityFinal.sample3.netWeight != null) {
            net3.setText(String.valueOf(recQualityFinal.sample3.netWeight));
        }
        if (recQualityFinal.sample3.iceQuantity != null) {
            ice3.setText(String.valueOf(recQualityFinal.sample3.iceQuantity));
        }
        if (recQualityFinal.sample3.fishTemp != null) {
            temp3.setText(String.valueOf(recQualityFinal.sample3.fishTemp));
        }

    }


    private boolean updateState() {


        recQualityFinal.sample1.size = Strings.isEmptyOrWhitespace(size1.getText().toString()) ? null : size1.getText().toString();
        recQualityFinal.sample1.boxType = Strings.isEmptyOrWhitespace(type1.getText().toString()) ? null : Integer.valueOf(type1.getText().toString());
        recQualityFinal.sample1.labelPieces = Strings.isEmptyOrWhitespace(number1.getText().toString()) ? null : Integer.valueOf(number1.getText().toString());
        recQualityFinal.sample1.countedPieces = Strings.isEmptyOrWhitespace(actual1.getText().toString()) ? null : Integer.valueOf(actual1.getText().toString());
        recQualityFinal.sample1.underWeight1 = Strings.isEmptyOrWhitespace(under11.getText().toString()) ? null : Integer.valueOf(under11.getText().toString());
        recQualityFinal.sample1.underWeight2 = Strings.isEmptyOrWhitespace(under12.getText().toString()) ? null : Integer.valueOf(under12.getText().toString());
        recQualityFinal.sample1.underWeight3 = Strings.isEmptyOrWhitespace(under13.getText().toString()) ? null : Integer.valueOf(under13.getText().toString());
        recQualityFinal.sample1.overWeight1 = Strings.isEmptyOrWhitespace(over11.getText().toString()) ? null : Integer.valueOf(over11.getText().toString());
        recQualityFinal.sample1.overWeight2 = Strings.isEmptyOrWhitespace(over12.getText().toString()) ? null : Integer.valueOf(over12.getText().toString());
        recQualityFinal.sample1.overWeight3 = Strings.isEmptyOrWhitespace(over13.getText().toString()) ? null : Integer.valueOf(over13.getText().toString());
        recQualityFinal.sample1.netWeight = Strings.isEmptyOrWhitespace(net1.getText().toString()) ? null : Integer.valueOf(net1.getText().toString());
        recQualityFinal.sample1.iceQuantity = Strings.isEmptyOrWhitespace(ice1.getText().toString()) ? null : Integer.valueOf(ice1.getText().toString());
        recQualityFinal.sample1.fishTemp = Strings.isEmptyOrWhitespace(temp1.getText().toString()) ? null : Double.valueOf(temp1.getText().toString());

        recQualityFinal.sample2.size = Strings.isEmptyOrWhitespace(size2.getText().toString()) ? null : size2.getText().toString();
        recQualityFinal.sample2.boxType = Strings.isEmptyOrWhitespace(type2.getText().toString()) ? null : Integer.valueOf(type2.getText().toString());
        recQualityFinal.sample2.labelPieces = Strings.isEmptyOrWhitespace(number2.getText().toString()) ? null : Integer.valueOf(number2.getText().toString());
        recQualityFinal.sample2.countedPieces = Strings.isEmptyOrWhitespace(actual2.getText().toString()) ? null : Integer.valueOf(actual2.getText().toString());
        recQualityFinal.sample2.underWeight1 = Strings.isEmptyOrWhitespace(under21.getText().toString()) ? null : Integer.valueOf(under21.getText().toString());
        recQualityFinal.sample2.underWeight2 = Strings.isEmptyOrWhitespace(under22.getText().toString()) ? null : Integer.valueOf(under22.getText().toString());
        recQualityFinal.sample2.underWeight3 = Strings.isEmptyOrWhitespace(under23.getText().toString()) ? null : Integer.valueOf(under23.getText().toString());
        recQualityFinal.sample2.overWeight1 = Strings.isEmptyOrWhitespace(over21.getText().toString()) ? null : Integer.valueOf(over21.getText().toString());
        recQualityFinal.sample2.overWeight2 = Strings.isEmptyOrWhitespace(over22.getText().toString()) ? null : Integer.valueOf(over22.getText().toString());
        recQualityFinal.sample2.overWeight3 = Strings.isEmptyOrWhitespace(over23.getText().toString()) ? null : Integer.valueOf(over23.getText().toString());
        recQualityFinal.sample2.netWeight = Strings.isEmptyOrWhitespace(net2.getText().toString()) ? null : Integer.valueOf(net2.getText().toString());
        recQualityFinal.sample2.iceQuantity = Strings.isEmptyOrWhitespace(ice2.getText().toString()) ? null : Integer.valueOf(ice2.getText().toString());
        recQualityFinal.sample2.fishTemp = Strings.isEmptyOrWhitespace(temp2.getText().toString()) ? null : Double.valueOf(temp2.getText().toString());

        recQualityFinal.sample3.size = Strings.isEmptyOrWhitespace(size3.getText().toString()) ? null : size3.getText().toString();
        recQualityFinal.sample3.boxType = Strings.isEmptyOrWhitespace(type3.getText().toString()) ? null : Integer.valueOf(type3.getText().toString());
        recQualityFinal.sample3.labelPieces = Strings.isEmptyOrWhitespace(number3.getText().toString()) ? null : Integer.valueOf(number3.getText().toString());
        recQualityFinal.sample3.countedPieces = Strings.isEmptyOrWhitespace(actual3.getText().toString()) ? null : Integer.valueOf(actual3.getText().toString());
        recQualityFinal.sample3.underWeight1 = Strings.isEmptyOrWhitespace(under31.getText().toString()) ? null : Integer.valueOf(under31.getText().toString());
        recQualityFinal.sample3.underWeight2 = Strings.isEmptyOrWhitespace(under32.getText().toString()) ? null : Integer.valueOf(under32.getText().toString());
        recQualityFinal.sample3.underWeight3 = Strings.isEmptyOrWhitespace(under33.getText().toString()) ? null : Integer.valueOf(under33.getText().toString());
        recQualityFinal.sample3.overWeight1 = Strings.isEmptyOrWhitespace(over31.getText().toString()) ? null : Integer.valueOf(over31.getText().toString());
        recQualityFinal.sample3.overWeight2 = Strings.isEmptyOrWhitespace(over32.getText().toString()) ? null : Integer.valueOf(over32.getText().toString());
        recQualityFinal.sample3.overWeight3 = Strings.isEmptyOrWhitespace(over33.getText().toString()) ? null : Integer.valueOf(over33.getText().toString());
        recQualityFinal.sample3.netWeight = Strings.isEmptyOrWhitespace(net3.getText().toString()) ? null : Integer.valueOf(net3.getText().toString());
        recQualityFinal.sample3.iceQuantity = Strings.isEmptyOrWhitespace(ice3.getText().toString()) ? null : Integer.valueOf(ice3.getText().toString());
        recQualityFinal.sample3.fishTemp = Strings.isEmptyOrWhitespace(temp3.getText().toString()) ? null : Double.valueOf(temp3.getText().toString());


//        recQualityFinal.signature = signatureView.getBitmap();
//        recQualityFinal.signatureBytes = signatureView.getBytes();

        return true;
    }


}
