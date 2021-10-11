package io.agritrack.api.sync;

import androidx.lifecycle.MutableLiveData;

import java.util.List;

import io.agritrack.R;
import io.agritrack.data.db.MobileDB;
import io.agritrack.data.dto.common.FishSpeciesDTO;
import retrofit2.Call;
import retrofit2.Response;

import static io.agritrack.FishTrackApplication.getAppContext;

public class SyncSpeciesCallBack extends BaseSyncCallBack<List<FishSpeciesDTO>> {

    public SyncSpeciesCallBack(MutableLiveData<String> syncResult) {
        super(syncResult);
    }

    @Override
    public void onResponse(Call<List<FishSpeciesDTO>> call, Response<List<FishSpeciesDTO>> response) {
        List<FishSpeciesDTO> rs = response.body();

        if (rs != null) {
            // get an instance of local DB
            db = MobileDB.getInstance(getAppContext());

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
