package io.agritrack.philosofish.api.sync;

import static io.agritrack.philosofish.FishTrackApplication.getAppContext;

import androidx.lifecycle.MutableLiveData;

import java.util.List;

import io.agritrack.philosofish.R;
import io.agritrack.philosofish.data.db.MobileDB;
import io.agritrack.philosofish.data.dto.common.SpeciesDTO;
import retrofit2.Call;
import retrofit2.Response;

public class SyncSpeciesCallBack extends BaseSyncCallBack<List<SpeciesDTO>> {

    public SyncSpeciesCallBack(MutableLiveData<String> syncResult) {
        super(syncResult);
    }

    @Override
    public void onResponse(Call<List<SpeciesDTO>> call, Response<List<SpeciesDTO>> response) {
        List<SpeciesDTO> rs = response.body();

        if (rs != null) {
            db = MobileDB.getInstance(getAppContext());

            for (SpeciesDTO speciesDTO : rs) {
                db.speciesDAO().insert(SpeciesDTO.convert(speciesDTO));
            }

            // Safe setter (avoids crash when syncResult == null)
            set(getAppContext().getString(R.string.species_sync_completed));
        } else {
            set(getAppContext().getString(R.string.no_species_found_alert));
        }
    }
}
