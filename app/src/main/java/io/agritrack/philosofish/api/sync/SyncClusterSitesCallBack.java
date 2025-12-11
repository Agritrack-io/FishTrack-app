package io.agritrack.philosofish.api.sync;

import static io.agritrack.philosofish.FishTrackApplication.getAppContext;
import static io.agritrack.philosofish.ui.service.LocalPreferences.SelectedCluster_Key;
import static io.agritrack.philosofish.ui.service.LocalPreferences.SelectedSiteId_Key;
import static io.agritrack.philosofish.ui.service.LocalPreferences.SelectedSiteLevel_Key;
import static io.agritrack.philosofish.ui.service.LocalPreferences.SelectedSiteName_Key;

import androidx.lifecycle.MutableLiveData;

import java.util.List;

import io.agritrack.philosofish.R;
import io.agritrack.philosofish.data.db.MobileDB;
import io.agritrack.philosofish.data.dto.SiteDTO;
import io.agritrack.philosofish.data.model.Site;
import io.agritrack.philosofish.settings.EncryptedSharedPreferences;
import io.agritrack.philosofish.ui.service.LocalPreferences;
import retrofit2.Call;
import retrofit2.Response;

public class SyncClusterSitesCallBack extends BaseSyncCallBack<List<SiteDTO>> {

    private EncryptedSharedPreferences pref;

    public SyncClusterSitesCallBack(MutableLiveData<String> syncResult) {
        super(syncResult);
    }

    @Override
    public void onResponse(Call<List<SiteDTO>> call, Response<List<SiteDTO>> response) {
        List<SiteDTO> siteDTOs = response.body();
        pref = new EncryptedSharedPreferences(getAppContext());

        if (siteDTOs != null) {
            db = MobileDB.getInstance(getAppContext());

            for (SiteDTO siteDTO : siteDTOs) {
                db.siteDAO().insert(SiteDTO.convert(siteDTO));
            }

            String siteName = pref.loadPreference("centralSite");
            Site site = db.siteDAO().getBySiteName(siteName);

            LocalPreferences.writeValue(SelectedSiteName_Key, siteName);

            if (site != null) {
                LocalPreferences.writeValue(SelectedSiteId_Key, site.id);
                LocalPreferences.writeValue(SelectedCluster_Key, site.lvl2);
                LocalPreferences.writeValue(SelectedSiteLevel_Key, site.lvl3);
            }

            set(getAppContext().getString(R.string.sites_sync_completed));
        } else {
            set(getAppContext().getString(R.string.no_sites_found_alert));
        }
    }
}
