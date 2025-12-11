package io.agritrack.philosofish.api.sync;

import static io.agritrack.philosofish.FishTrackApplication.getAppContext;

import androidx.lifecycle.MutableLiveData;

import io.agritrack.philosofish.R;
import io.agritrack.philosofish.data.db.MobileDB;
import io.agritrack.philosofish.data.dto.tx.ReceiptQualityTxDTO;
import io.agritrack.philosofish.data.model.tx.ReceiptQualityTransaction;
import retrofit2.Call;
import retrofit2.Response;

public class PendingRecQualityTxCallBack extends BaseSyncCallBack<ReceiptQualityTxDTO> {

    public PendingRecQualityTxCallBack(MutableLiveData<String> syncResult) {
        super(syncResult);
    }

    @Override
    public void onResponse(Call<ReceiptQualityTxDTO> call, Response<ReceiptQualityTxDTO> response) {
        if (response.isSuccessful()) {
            // get an instance of local DB
            db = MobileDB.getInstance(getAppContext());
            ReceiptQualityTransaction recTx = db.receiptQualityTransactionDAO().getByLot(response.body().lot);
            if (recTx != null) {
                recTx.isSynced = true;
                db.receiptQualityTransactionDAO().update(recTx);
            }

            // Sites sync succeeded.
            syncResult.setValue(getAppContext().getString(R.string.pending_quality_tx_upload_completed));
        } else {
            //  no Sites found
            syncResult.setValue(getAppContext().getString(R.string.pending_quality_tx_upload_failure_alert));
        }
    }
}
