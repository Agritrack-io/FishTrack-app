package io.agritrack.api.sync;

import androidx.lifecycle.MutableLiveData;

import java.util.List;

import io.agritrack.R;
import io.agritrack.data.db.MobileDB;
import io.agritrack.data.dto.common.SpeciesDTO;
import retrofit2.Call;
import retrofit2.Response;

import static io.agritrack.FishTrackApplication.getAppContext;

public class SyncSpeciesCallBack extends BaseSyncCallBack<List<SpeciesDTO>> {

    public SyncSpeciesCallBack(MutableLiveData<String> syncResult) {
        super(syncResult);
    }

    @Override
    public void onResponse(Call<List<SpeciesDTO>> call, Response<List<SpeciesDTO>> response) {
        List<SpeciesDTO> rs = response.body();

        if (rs != null) {
            // get an instance of local DB
            db = MobileDB.getInstance(getAppContext());

            for (SpeciesDTO speciesDTO : rs) {
                db.speciesDAO().insert(SpeciesDTO.convert(speciesDTO));
            }
            // Fish Species sync succeeded.
            syncResult.setValue(getAppContext().getString(R.string.species_sync_completed));
        } else {
            // no Fish Species found
            syncResult.setValue(getAppContext().getString(R.string.no_species_found_alert));
        }
    }
}
