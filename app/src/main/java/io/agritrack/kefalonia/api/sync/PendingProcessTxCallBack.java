package io.agritrack.kefalonia.api.sync;

import static io.agritrack.kefalonia.FishTrackApplication.getAppContext;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import io.agritrack.kefalonia.R;
import io.agritrack.kefalonia.data.db.MobileDB;
import io.agritrack.kefalonia.data.dto.tx.ProcessingTxDTO;
import retrofit2.Call;
import retrofit2.Response;

public class PendingProcessTxCallBack extends BaseSyncCallBack<ProcessingTxDTO> {

    public PendingProcessTxCallBack(MutableLiveData<String> syncResult) {
        super(syncResult);
    }

    @Override
    public void onResponse(Call<ProcessingTxDTO> call, Response<ProcessingTxDTO> response) {
        ProcessingTxDTO dto = response.body();

        if (dto != null) {
            // get an instance of local DB
            db = MobileDB.getInstance(getAppContext());

            int rowsAffected = db.processingTransactionDAO().deleteAll();
            Log.i("Pending receipts.", String.format("deleted %s rows from ReceiptTransactions...", rowsAffected));

            // Sites sync succeeded.
            syncResult.setValue(getAppContext().getString(R.string.pending_process_tx_upload_completed));
        } else {
            //  no Sites found
            syncResult.setValue(getAppContext().getString(R.string.pending_process_tx_upload_failure_alert));
        }
    }
}
