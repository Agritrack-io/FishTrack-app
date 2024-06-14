package io.agritrack.philosofish.api.sync;

import static io.agritrack.philosofish.FishTrackApplication.getAppContext;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import java.util.List;

import io.agritrack.philosofish.R;
import io.agritrack.philosofish.data.db.MobileDB;
import io.agritrack.philosofish.data.dto.BinInfoDTO;
import retrofit2.Call;
import retrofit2.Response;

public class PendingBinInfoTxCallBack extends BaseSyncCallBack<List<BinInfoDTO>> {

    public PendingBinInfoTxCallBack(MutableLiveData<String> syncResult) {
        super(syncResult);
    }

    @Override
    public void onResponse(Call<List<BinInfoDTO>> call, Response<List<BinInfoDTO>> response) {

        if (response.isSuccessful()) {
            // get an instance of local DB
            db = MobileDB.getInstance(getAppContext());

            int rowsAffected = db.binInfoDAO().deleteAll();
            Log.i("Pending Bin Info Txs.", String.format("deleted %s rows from BinInfos...", rowsAffected));

            // Sites sync succeeded.
            syncResult.setValue(getAppContext().getString(R.string.pending_fishing_tx_upload_completed));
        } else {
            //  no Sites found
            syncResult.setValue(getAppContext().getString(R.string.pending_fishing_tx_upload_failure_alert));
        }
    }
}
