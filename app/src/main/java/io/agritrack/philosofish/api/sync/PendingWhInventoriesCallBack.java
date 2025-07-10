package io.agritrack.philosofish.api.sync;

import static io.agritrack.philosofish.FishTrackApplication.getAppContext;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import io.agritrack.philosofish.R;
import io.agritrack.philosofish.data.db.MobileDB;
import io.agritrack.philosofish.data.dto.wh.RFIDInventoryDTO;
import retrofit2.Call;
import retrofit2.Response;

public class PendingWhInventoriesCallBack extends BaseSyncCallBack<RFIDInventoryDTO> {

    public PendingWhInventoriesCallBack(MutableLiveData<String> syncResult) {
        super(syncResult);
    }

    @Override
    public void onResponse(Call<RFIDInventoryDTO> call, Response<RFIDInventoryDTO> response) {
        RFIDInventoryDTO dto = response.body();

        if (dto != null) {
            // get an instance of local DB
            db = MobileDB.getInstance(getAppContext());

            //Delete the inventory by uid
            int inventoriesAffected = db.rFIDInventoryDAO().deleteById(dto.uid);
            int inventoryItemsAffected = db.rFIDInventoryItemDAO().deleteByInventoryId(dto.uid);

            Log.i("Pending inventories.", String.format("deleted %s rows from inventories and %s rows from inventories items...", inventoriesAffected, inventoryItemsAffected));

            // Sites sync succeeded.
            syncResult.setValue(getAppContext().getString(R.string.pending_measurement_tx_upload_completed));
        } else {
            //  no Sites found
            syncResult.setValue(getAppContext().getString(R.string.pending_measurement_tx_upload_failure_alert));
        }
    }
}

