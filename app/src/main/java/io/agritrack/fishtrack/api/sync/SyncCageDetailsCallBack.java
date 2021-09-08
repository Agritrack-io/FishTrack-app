package io.agritrack.fishtrack.api.sync;

import androidx.lifecycle.MutableLiveData;

import java.util.List;

import io.agritrack.fishtrack.R;
import io.agritrack.fishtrack.data.db.MobileDB;
import io.agritrack.fishtrack.data.dto.CageDetailsDTO;
import retrofit2.Call;
import retrofit2.Response;

import static io.agritrack.fishtrack.FishTrackApplication.getAppContext;
import static io.agritrack.fishtrack.FishTrackApplication.getContext;

public class SyncCageDetailsCallBack extends BaseSyncCallBack<List<CageDetailsDTO>> {

    public SyncCageDetailsCallBack(MutableLiveData<String> syncResult) {
        super(syncResult);
    }

    @Override
    public void onResponse(Call<List<CageDetailsDTO>> call, Response<List<CageDetailsDTO>> response) {
        List<CageDetailsDTO> rs = response.body();

        if (rs != null) {
            // get an instance of local DB
            db = MobileDB.getInstance(getContext());

            for (CageDetailsDTO detailDTO : rs) {
                db.cageDetailsDAO().insert(CageDetailsDTO.convert(detailDTO));
            }
            // Cage Details sync succeeded.
            syncResult.setValue(getAppContext().getString(R.string.cage_details_sync_completed));
        } else {
            // no Cage Details found
            syncResult.setValue(getAppContext().getString(R.string.no_cage_details_found_alert));
        }
    }
}