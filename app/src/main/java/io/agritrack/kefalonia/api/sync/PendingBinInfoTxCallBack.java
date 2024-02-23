package io.agritrack.kefalonia.api.sync;

import static io.agritrack.kefalonia.FishTrackApplication.getAppContext;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import java.util.List;

import io.agritrack.kefalonia.R;
import io.agritrack.kefalonia.data.db.MobileDB;
import io.agritrack.kefalonia.data.dto.BinInfoDTO;
import io.agritrack.kefalonia.data.dto.tx.FishingTxDTO;
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
