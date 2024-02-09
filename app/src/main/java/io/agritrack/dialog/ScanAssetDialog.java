package io.agritrack.dialog;

import static io.agritrack.FishTrackApplication.IsDemo;
import static io.agritrack.FishTrackApplication.getAppContext;
import static io.agritrack.ui.custom.CustomToast.CToast;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.StringRes;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.util.Strings;

import java.lang.ref.WeakReference;
import java.util.List;

import io.agritrack.kefalonia.R;
import io.agritrack.common.Filters;
import io.agritrack.data.db.MobileDB;
import io.agritrack.data.model.wh.Asset;
import io.agritrack.fish.state.GlobalState;
import io.agritrack.fish.ui.wh.correlation.CorrelationCageActivity;
import io.agritrack.rfid.SingleShotScanner;
import io.agritrack.ui.adapter.TemplateRecyclerAdapter;
import io.agritrack.ui.login.api.SiteInfoRS;

public class ScanAssetDialog {
    private TextView tvTitle, tvCageBarcode;
    private Button btnScanAssetTag, btnOk;
    private final ScanHandler mScanHandler = new ScanHandler(this);
    private final MutableLiveData<String> selectedCage;
    private final MobileDB db;

    private final Activity activity;
    private Dialog dialog;

    public ScanAssetDialog(Activity activity, MutableLiveData<String> liveData, @StringRes int title) {
        this.activity = activity;
        this.selectedCage = liveData;

        setDialog();
        findViews();
        SetCaptions(title);

        // get an instance of local DB
        db = MobileDB.getInstance(getAppContext());

        // RFID scanning functionality
        btnScanAssetTag.setOnClickListener(this::onClick);

        btnOk.setOnClickListener(view -> {
            dismiss();
            //CToast(activity.getApplicationContext(), render("Your photo was saved locally"), Toast.LENGTH_LONG);
        });
    }

    protected void onClick(View view) {
        SingleShotScanner scanner_runnable = new SingleShotScanner(mScanHandler);
        scanner_runnable.setFilter(Filters.RFID_CAGE);
        scanner_runnable.startReading();
        mScanHandler.postDelayed(scanner_runnable, 0);
    }

    // ###################################################
    private class ScanHandler extends Handler {
        private final WeakReference<ScanAssetDialog> mActivity;

        public ScanHandler(ScanAssetDialog activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    String epcStr = msg.getData().getString("epc");
                    if (epcStr!=null) {
                        String label = epcStr.length() > 15 ? epcStr.substring(14) : epcStr;
                        try {
                            if (!Strings.isEmptyOrWhitespace(epcStr)) {
                                tvCageBarcode.setText(label);
                                Asset cage = db.assetDAO().getAssetByEpc(epcStr);
                                if (cage == null){
                                    return;
                                }
                                selectedCage.setValue(cage.code);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case 1980:
                    if (!IsDemo) {
                        //CToast(getApplicationContext(), render(String.format("No item of type %s was found!", selectedAssetType)), Toast.LENGTH_LONG);
                    }
                    break;
            }
        }
    }

    public void showDialog() {
        dialog.show();
    }

    public void hide() {
        dialog.hide();
    }

    public void dismiss() {
        dialog.dismiss();
    }

    private void setDialog() {
        dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.scan_asset_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    private void findViews() {
        tvTitle = dialog.findViewById(R.id.tv_title);
        tvCageBarcode = dialog.findViewById(R.id.tvCageBarcode);
        btnScanAssetTag = dialog.findViewById(R.id.btnScanAssetTag);
        btnOk = (Button) dialog.findViewById(R.id.btnOk);
    }

    private void SetCaptions(int title) {
        tvTitle.setText(title);
    }
}
