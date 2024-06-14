package io.agritrack.philosofish.api.sync;

import static io.agritrack.philosofish.FishTrackApplication.getAppContext;

import androidx.lifecycle.MutableLiveData;

import java.util.List;

import io.agritrack.philosofish.R;
import io.agritrack.philosofish.data.db.MobileDB;
import io.agritrack.philosofish.data.dto.FishingRequestDTO;
import io.agritrack.philosofish.data.model.FishingRequest;
import io.agritrack.philosofish.data.repo.FishingRequestRepository;
import io.agritrack.philosofish.data.repo.IFishTrackRepository;
import retrofit2.Call;
import retrofit2.Response;

public class SyncFishingRequestCallBack extends BaseSyncCallBack<List<FishingRequestDTO>> {

    private IFishTrackRepository fishingRqRepo;

    public SyncFishingRequestCallBack(MutableLiveData<String> syncResult) {
        super(syncResult);
        this.fishingRqRepo = new FishingRequestRepository();
    }

    @Override
    public void onResponse(Call<List<FishingRequestDTO>> call, Response<List<FishingRequestDTO>> response) {
        List<FishingRequestDTO> fishingReqDTOs = response.body();

        // get an instance of local DB
        this.db = MobileDB.getInstance(getAppContext());

        //Clean fishing request table before update
        //this.db.fishingRequestsDAO().deleteAll();
        //this.fishingRqRepo.removeAll();
        List<FishingRequest> currentFishingRqs = this.db.fishingRequestsDAO().getAll();
        for (FishingRequest frq : currentFishingRqs) {
            this.db.fishingRequestsDAO().delete(frq);
        }

        if (fishingReqDTOs != null) {
            for (FishingRequestDTO fishingRequestDTO : fishingReqDTOs) {
                this.db.fishingRequestsDAO().insert(FishingRequestDTO.convert(fishingRequestDTO));
            }
            // Harvest Requests sync succeeded.
            syncResult.setValue(getAppContext().getString(R.string.harvest_requests_sync_completed));
        } else {
            // no Harvest Requests found
            syncResult.setValue(getAppContext().getString(R.string.no_harvest_requests_found_alert));
        }
    }
}