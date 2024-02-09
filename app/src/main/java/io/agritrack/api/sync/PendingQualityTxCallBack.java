package io.agritrack.api.sync;

import static io.agritrack.FishTrackApplication.getAppContext;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import io.agritrack.kefalonia.R;
import io.agritrack.data.db.MobileDB;
import io.agritrack.data.dto.tx.ProcessingTxDTO;
import io.agritrack.data.dto.tx.QualityTxDTO;
import retrofit2.Call;
import retrofit2.Response;

public class PendingQualityTxCallBack extends BaseSyncCallBack<QualityTxDTO> {

    public PendingQualityTxCallBack(MutableLiveData<String> syncResult) {
        super(syncResult);
    }

    @Override
    public void onResponse(Call<QualityTxDTO> call, Response<QualityTxDTO> response) {
        if (response.isSuccessful()) {
            // get an instance of local DB
            db = MobileDB.getInstance(getAppContext());

            int rowsAffected = db.qualityTransactionDAO().deleteAll();
            Log.i("Pending qualities.", String.format("deleted %s rows from QualityTransactions...", rowsAffected));

            // Sites sync succeeded.
            syncResult.setValue(getAppContext().getString(R.string.pending_quality_tx_upload_completed));
        } else {
            //  no Sites found
            syncResult.setValue(getAppContext().getString(R.string.pending_quality_tx_upload_failure_alert));
        }
    }
}
