package io.agritrack.api.sync;

import static io.agritrack.FishTrackApplication.getAppContext;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import io.agritrack.R;
import io.agritrack.data.db.MobileDB;
import io.agritrack.data.dto.tx.AssetTxDTO;
import io.agritrack.data.dto.tx.FishingTxDTO;
import retrofit2.Call;
import retrofit2.Response;

public class PendingAssetTxCallBack extends BaseSyncCallBack<AssetTxDTO> {

    public PendingAssetTxCallBack(MutableLiveData<String> syncResult) {
        super(syncResult);
    }

    @Override
    public void onResponse(Call<AssetTxDTO> call, Response<AssetTxDTO> response) {
        if (response.isSuccessful()) {
            // get an instance of local DB
            db = MobileDB.getInstance(getAppContext());

            int rowsAffected = db.assetTransactionDAO().deleteAll();
            Log.i("Pending AssetTxs.", String.format("deleted %s rows from AssetTransactions...", rowsAffected));

            // Sites sync succeeded.
            syncResult.setValue(getAppContext().getString(R.string.pending_asset_tx_upload_completed));
        } else {
            //  no Sites found
            syncResult.setValue(getAppContext().getString(R.string.pending_asset_tx_upload_failure_alert));
        }
    }
}
