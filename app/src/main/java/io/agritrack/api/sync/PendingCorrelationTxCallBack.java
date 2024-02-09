package io.agritrack.api.sync;

import static io.agritrack.FishTrackApplication.getAppContext;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import io.agritrack.kefalonia.R;
import io.agritrack.data.db.MobileDB;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

public class PendingCorrelationTxCallBack extends BaseSyncCallBack<ResponseBody> {

    public PendingCorrelationTxCallBack(MutableLiveData<String> syncResult) {
        super(syncResult);
    }

    @Override
    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
        if (response.isSuccessful()) {
            // get an instance of local DB
            db = MobileDB.getInstance(getAppContext());

            int rowsAffected = db.correlationTransactionDAO().deleteAll();
            Log.i("Pending Correlations.", String.format("deleted %s rows from CorrelationTransactions...", rowsAffected));

            // Sites sync succeeded.
            syncResult.setValue(getAppContext().getString(R.string.pending_correlation_tx_upload_completed));
        } else {
            //  no Sites found
            syncResult.setValue(getAppContext().getString(R.string.pending_correlation_tx_upload_failure_alert));
        }
    }
}
