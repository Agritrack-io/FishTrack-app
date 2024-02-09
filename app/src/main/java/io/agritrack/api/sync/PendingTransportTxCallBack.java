package io.agritrack.api.sync;

import static io.agritrack.FishTrackApplication.getAppContext;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import io.agritrack.kefalonia.R;
import io.agritrack.data.db.MobileDB;
import io.agritrack.data.dto.tx.FishingTxDTO;
import io.agritrack.data.dto.tx.TransportTxDTO;
import retrofit2.Call;
import retrofit2.Response;

public class PendingTransportTxCallBack extends BaseSyncCallBack<TransportTxDTO> {

    public PendingTransportTxCallBack(MutableLiveData<String> syncResult) {
        super(syncResult);
    }

    @Override
    public void onResponse(Call<TransportTxDTO> call, Response<TransportTxDTO> response) {

        if (response.isSuccessful()) {
            // get an instance of local DB
            db = MobileDB.getInstance(getAppContext());

            int rowsAffected = db.transportTransactionDAO().deleteAll();
            Log.i("Pending Transportations.", String.format("deleted %s rows from TransportTransactions...", rowsAffected));

            // Sites sync succeeded.
            syncResult.setValue(getAppContext().getString(R.string.pending_transport_tx_upload_completed));
        } else {
            //  no Sites found
            syncResult.setValue(getAppContext().getString(R.string.pending_transport_tx_upload_failure_alert));
        }
    }
}
