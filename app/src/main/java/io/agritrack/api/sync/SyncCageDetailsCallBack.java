package io.agritrack.api.sync;

import androidx.lifecycle.MutableLiveData;

import java.util.List;

import io.agritrack.R;
import io.agritrack.data.db.MobileDB;
import io.agritrack.data.dto.CageDetailsDTO;
import retrofit2.Call;
import retrofit2.Response;

import static io.agritrack.FishTrackApplication.getAppContext;

public class SyncCageDetailsCallBack extends BaseSyncCallBack<List<CageDetailsDTO>> {

    public SyncCageDetailsCallBack(MutableLiveData<String> syncResult) {
        super(syncResult);
    }

    @Override
    public void onResponse(Call<List<CageDetailsDTO>> call, Response<List<CageDetailsDTO>> response) {
        List<CageDetailsDTO> rs = response.body();

        if (rs != null) {
            // get an instance of local DB
            db = MobileDB.getInstance(getAppContext());

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