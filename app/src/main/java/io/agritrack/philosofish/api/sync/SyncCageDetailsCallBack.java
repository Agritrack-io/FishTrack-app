package io.agritrack.philosofish.api.sync;

import static io.agritrack.philosofish.FishTrackApplication.getAppContext;

import androidx.lifecycle.MutableLiveData;

import java.util.List;

import io.agritrack.philosofish.R;
import io.agritrack.philosofish.data.db.MobileDB;
import io.agritrack.philosofish.data.dto.CageDetailsDTO;
import retrofit2.Call;
import retrofit2.Response;

public class SyncCageDetailsCallBack extends BaseSyncCallBack<List<CageDetailsDTO>> {

    public SyncCageDetailsCallBack(MutableLiveData<String> syncResult) {
        super(syncResult);
    }

    @Override
    public void onResponse(Call<List<CageDetailsDTO>> call, Response<List<CageDetailsDTO>> response) {
        List<CageDetailsDTO> rs = response.body();

        if (rs != null) {
            db = MobileDB.getInstance(getAppContext());

            for (CageDetailsDTO detailDTO : rs) {
                db.cageDetailsDAO().insert(CageDetailsDTO.convert(detailDTO));
            }

            set(getAppContext().getString(R.string.cage_details_sync_completed));

        } else {

            set(getAppContext().getString(R.string.no_cage_details_found_alert));
        }
    }
}
