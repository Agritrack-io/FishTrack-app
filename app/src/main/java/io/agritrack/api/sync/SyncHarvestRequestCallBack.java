package io.agritrack.api.sync;

import androidx.lifecycle.MutableLiveData;

import java.util.List;

import io.agritrack.R;
import io.agritrack.data.db.MobileDB;
import io.agritrack.data.dto.HarvestRequestDTO;
import retrofit2.Call;
import retrofit2.Response;

import static io.agritrack.FishTrackApplication.getAppContext;

public class SyncHarvestRequestCallBack extends BaseSyncCallBack<List<HarvestRequestDTO>> {

    public SyncHarvestRequestCallBack(MutableLiveData<String> syncResult) {
        super(syncResult);
    }

    @Override
    public void onResponse(Call<List<HarvestRequestDTO>> call, Response<List<HarvestRequestDTO>> response) {
        List<HarvestRequestDTO> harvestReqDTOs = response.body();

        if (harvestReqDTOs != null) {
            // get an instance of local DB
            db = MobileDB.getInstance(getAppContext());

            for (HarvestRequestDTO harvestRequestDTO : harvestReqDTOs) {
                db.harvestRequestsDAO().insert(HarvestRequestDTO.convert(harvestRequestDTO));
            }
            // Harvest Requests sync succeeded.
            syncResult.setValue(getAppContext().getString(R.string.harvest_requests_sync_completed));
        } else {
            // no Harvest Requests found
            syncResult.setValue(getAppContext().getString(R.string.no_harvest_requests_found_alert));
        }
    }
}