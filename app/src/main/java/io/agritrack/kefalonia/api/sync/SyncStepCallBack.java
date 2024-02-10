package io.agritrack.kefalonia.api.sync;

import static io.agritrack.kefalonia.FishTrackApplication.getAppContext;

import androidx.lifecycle.MutableLiveData;

import io.agritrack.kefalonia.R;
import io.agritrack.kefalonia.data.type.ConfigDevice;
import io.agritrack.kefalonia.ui.service.LocalPreferences;
import retrofit2.Call;
import retrofit2.Response;

public class SyncStepCallBack extends BaseSyncCallBack<ConfigDevice> {
    public SyncStepCallBack(MutableLiveData<String> syncResult) {
        super(syncResult);
    }

    @Override
    public void onResponse(Call<ConfigDevice> call, Response<ConfigDevice> response) {
        ConfigDevice rs = response.body();

        if (rs != null) {
            // get an instance of local DB


            LocalPreferences.writeValue("Prefix", rs.getPrefix());
//            LocalPreferences.writeValue("Step", rs.getEpcs());
            if (LocalPreferences.getCurrentEpcList() != null) {

            } else {
                LocalPreferences.putCurrentEpcList(rs.getEpcs());
            }
            // Assets sync succeeded.
            syncResult.setValue(getAppContext().getString(R.string.assets_sync_completed));
        } else {
            // no Assets found
            syncResult.setValue(getAppContext().getString(R.string.no_sites_found_alert));
        }
    }
}
