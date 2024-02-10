package io.agritrack.kefalonia.api.sync;

import static io.agritrack.kefalonia.FishTrackApplication.getAppContext;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import io.agritrack.kefalonia.R;
import io.agritrack.kefalonia.data.db.MobileDB;
import io.agritrack.kefalonia.data.dto.tx.FishingTxDTO;
import retrofit2.Call;
import retrofit2.Response;

public class PendingFishingTxCallBack extends BaseSyncCallBack<FishingTxDTO> {

    public PendingFishingTxCallBack(MutableLiveData<String> syncResult) {
        super(syncResult);
    }

    @Override
    public void onResponse(Call<FishingTxDTO> call, Response<FishingTxDTO> response) {

        if (response.isSuccessful()) {
            // get an instance of local DB
            db = MobileDB.getInstance(getAppContext());

            int rowsAffected = db.fishingTransactionDAO().deleteAllCompleted();
            Log.i("Pending Fishings.", String.format("deleted %s rows from FishingTransactions...", rowsAffected));

            // Sites sync succeeded.
            syncResult.setValue(getAppContext().getString(R.string.pending_fishing_tx_upload_completed));
        } else {
            //  no Sites found
            syncResult.setValue(getAppContext().getString(R.string.pending_fishing_tx_upload_failure_alert));
        }
    }
}
