package io.agritrack.philosofish.api.sync;

import static io.agritrack.philosofish.FishTrackApplication.getAppContext;

import androidx.lifecycle.MutableLiveData;

import io.agritrack.philosofish.R;
import io.agritrack.philosofish.data.db.MobileDB;
import io.agritrack.philosofish.data.dto.tx.FinalQualityTxDTO;
import io.agritrack.philosofish.data.dto.tx.PackageQualityTxDTO;
import io.agritrack.philosofish.data.model.tx.FinalQualityTransaction;
import io.agritrack.philosofish.data.model.tx.PackageQualityTransaction;
import retrofit2.Call;
import retrofit2.Response;

public class PendingFinalQualityTxCallBack extends BaseSyncCallBack<FinalQualityTxDTO> {

    public PendingFinalQualityTxCallBack(MutableLiveData<String> syncResult) {
        super(syncResult);
    }

    @Override
    public void onResponse(Call<FinalQualityTxDTO> call, Response<FinalQualityTxDTO> response) {
        if (response.isSuccessful()) {
            // get an instance of local DB
            db = MobileDB.getInstance(getAppContext());
            FinalQualityTransaction finalTx = db.finalQualityTransactionDAO().getByLot(response.body().lot);
            if (finalTx != null) {
                finalTx.isSynced = true;
                db.finalQualityTransactionDAO().update(finalTx);
            }

            // Sites sync succeeded.
            syncResult.setValue(getAppContext().getString(R.string.pending_quality_tx_upload_completed));
        } else {
            //  no Sites found
            syncResult.setValue(getAppContext().getString(R.string.pending_quality_tx_upload_failure_alert));
        }
    }
}
