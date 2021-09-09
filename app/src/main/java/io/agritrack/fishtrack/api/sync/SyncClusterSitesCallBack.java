package io.agritrack.fishtrack.api.sync;

import androidx.lifecycle.MutableLiveData;

import java.util.List;

import io.agritrack.fishtrack.R;
import io.agritrack.fishtrack.data.db.MobileDB;
import io.agritrack.fishtrack.data.dto.SiteDTO;
import retrofit2.Call;
import retrofit2.Response;

import static io.agritrack.fishtrack.FishTrackApplication.getAppContext;

public class SyncClusterSitesCallBack extends BaseSyncCallBack<List<SiteDTO>> {

    public SyncClusterSitesCallBack(MutableLiveData<String> syncResult) {
        super(syncResult);
    }

    @Override
    public void onResponse(Call<List<SiteDTO>> call, Response<List<SiteDTO>> response) {
        List<SiteDTO> siteDTOs = response.body();

        if (siteDTOs != null) {
            // get an instance of local DB
            db = MobileDB.getInstance(getAppContext());

            for (SiteDTO siteDTO : siteDTOs) {
                db.siteDAO().insert(SiteDTO.convert(siteDTO));
            }
            // Sites sync succeeded.
            syncResult.setValue(getAppContext().getString(R.string.sites_sync_completed));
        } else {
            //  no Sites found
            syncResult.setValue(getAppContext().getString(R.string.no_sites_found_alert));
        }
    }
}
