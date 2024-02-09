package io.agritrack.api.sync;

import androidx.lifecycle.MutableLiveData;

import java.util.List;

import io.agritrack.kefalonia.R;
import io.agritrack.data.db.MobileDB;
import io.agritrack.data.dto.SiteDTO;
import io.agritrack.data.model.Site;
import io.agritrack.settings.EncryptedSharedPreferences;
import io.agritrack.ui.service.LocalPreferences;
import retrofit2.Call;
import retrofit2.Response;

import static io.agritrack.FishTrackApplication.getAppContext;
import static io.agritrack.ui.service.LocalPreferences.SelectedCluster_Key;
import static io.agritrack.ui.service.LocalPreferences.SelectedSiteId_Key;
import static io.agritrack.ui.service.LocalPreferences.SelectedSiteLevel_Key;
import static io.agritrack.ui.service.LocalPreferences.SelectedSiteName_Key;

public class SyncClusterSitesCallBack extends BaseSyncCallBack<List<SiteDTO>> {

    public SyncClusterSitesCallBack(MutableLiveData<String> syncResult) {
        super(syncResult);
    }
    private EncryptedSharedPreferences pref;

    @Override
    public void onResponse(Call<List<SiteDTO>> call, Response<List<SiteDTO>> response) {
        List<SiteDTO> siteDTOs = response.body();
        pref = new EncryptedSharedPreferences(getAppContext());

        if (siteDTOs != null) {
            // get an instance of local DB
            db = MobileDB.getInstance(getAppContext());

            for (SiteDTO siteDTO : siteDTOs) {
                db.siteDAO().insert(SiteDTO.convert(siteDTO));
            }

            String siteName =  pref.loadPreference("centralSite");
            Site site = db.siteDAO().getBySiteName(siteName);
            // persist selected Site to local Preferences.
            LocalPreferences.writeValue(SelectedSiteName_Key, siteName);
            if (site != null) {
                LocalPreferences.writeValue(SelectedSiteId_Key, site.id);
                LocalPreferences.writeValue(SelectedCluster_Key, site.lvl2);
                LocalPreferences.writeValue(SelectedSiteLevel_Key, site.lvl3);
            }

            // Sites sync succeeded.
            syncResult.setValue(getAppContext().getString(R.string.sites_sync_completed));
        } else {
            //  no Sites found
            syncResult.setValue(getAppContext().getString(R.string.no_sites_found_alert));
        }
    }
}
