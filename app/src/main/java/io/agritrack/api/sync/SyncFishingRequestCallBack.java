package io.agritrack.api.sync;

import static io.agritrack.FishTrackApplication.getAppContext;

import androidx.lifecycle.MutableLiveData;

import java.util.List;

import io.agritrack.R;
import io.agritrack.data.db.MobileDB;
import io.agritrack.data.dto.FishingRequestDTO;
import retrofit2.Call;
import retrofit2.Response;

public class SyncFishingRequestCallBack extends BaseSyncCallBack<List<FishingRequestDTO>> {

    public SyncFishingRequestCallBack(MutableLiveData<String> syncResult) {
        super(syncResult);
    }

    @Override
    public void onResponse(Call<List<FishingRequestDTO>> call, Response<List<FishingRequestDTO>> response) {
        List<FishingRequestDTO> fishingReqDTOs = response.body();

        if (fishingReqDTOs != null) {
            // get an instance of local DB
            db = MobileDB.getInstance(getAppContext());

            for (FishingRequestDTO fishingRequestDTO : fishingReqDTOs) {
                db.fishingRequestsDAO().insert(FishingRequestDTO.convert(fishingRequestDTO));
            }
            // Harvest Requests sync succeeded.
            syncResult.setValue(getAppContext().getString(R.string.harvest_requests_sync_completed));
        } else {
            // no Harvest Requests found
            syncResult.setValue(getAppContext().getString(R.string.no_harvest_requests_found_alert));
        }
    }
}