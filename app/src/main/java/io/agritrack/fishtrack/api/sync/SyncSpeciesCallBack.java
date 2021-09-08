package io.agritrack.fishtrack.api.sync;

import androidx.lifecycle.MutableLiveData;

import java.util.List;

import io.agritrack.fishtrack.R;
import io.agritrack.fishtrack.data.db.MobileDB;
import io.agritrack.fishtrack.data.dto.common.FishSpeciesDTO;
import retrofit2.Call;
import retrofit2.Response;

import static io.agritrack.fishtrack.FishTrackApplication.getAppContext;
import static io.agritrack.fishtrack.FishTrackApplication.getContext;

public class SyncSpeciesCallBack extends BaseSyncCallBack<List<FishSpeciesDTO>> {

    public SyncSpeciesCallBack(MutableLiveData<String> syncResult) {
        super(syncResult);
    }

    @Override
    public void onResponse(Call<List<FishSpeciesDTO>> call, Response<List<FishSpeciesDTO>> response) {
        List<FishSpeciesDTO> rs = response.body();

        if (rs != null) {
            // get an instance of local DB
            db = MobileDB.getInstance(getContext());

            for (FishSpeciesDTO speciesDTO : rs) {
                db.speciesDAO().insert(FishSpeciesDTO.convert(speciesDTO));
            }
            // Fish Species sync succeeded.
            syncResult.setValue(getAppContext().getString(R.string.species_sync_completed));
        } else {
            // no Fish Species found
            syncResult.setValue(getAppContext().getString(R.string.no_species_found_alert));
        }
    }
}
