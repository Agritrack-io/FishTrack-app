package io.agritrack.philosofish.api.sync;

import static io.agritrack.philosofish.FishTrackApplication.getAppContext;

import androidx.lifecycle.MutableLiveData;

import io.agritrack.philosofish.R;
import io.agritrack.philosofish.data.db.MobileDB;
import io.agritrack.philosofish.data.dto.tx.PackageQualityTxDTO;
import io.agritrack.philosofish.data.model.tx.PackageQualityTransaction;
import retrofit2.Call;
import retrofit2.Response;

public class PendingPackQualityTxCallBack extends BaseSyncCallBack<PackageQualityTxDTO> {

    public PendingPackQualityTxCallBack(MutableLiveData<String> syncResult) {
        super(syncResult);
    }

    @Override
    public void onResponse(Call<PackageQualityTxDTO> call, Response<PackageQualityTxDTO> response) {
        if (response.isSuccessful()) {
            // get an instance of local DB
            db = MobileDB.getInstance(getAppContext());
            PackageQualityTransaction packTx = db.packageQualityTransactionDAO().getByLot(response.body().lot);
            if (packTx != null) {
                if (response.body().labelCheck != null) {
                    packTx.isLabelSynced = true;
                }
                if (response.body().freshCheck != null) {
                    packTx.isFreshSynced = true;
                }
                if (response.body().sampling != null) {
                    packTx.isSampleSynced = true;
                }
                db.packageQualityTransactionDAO().update(packTx);
            }

            // Sites sync succeeded.
            syncResult.setValue(getAppContext().getString(R.string.pending_quality_tx_upload_completed));
        } else {
            //  no Sites found
            syncResult.setValue(getAppContext().getString(R.string.pending_quality_tx_upload_failure_alert));
        }
    }
}
