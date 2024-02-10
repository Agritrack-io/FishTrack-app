package io.agritrack.kefalonia.api.sync;

import static io.agritrack.kefalonia.FishTrackApplication.getAppContext;

import androidx.lifecycle.MutableLiveData;

import io.agritrack.kefalonia.R;
import io.agritrack.kefalonia.data.dto.common.ReaderDTO;
import retrofit2.Call;
import retrofit2.Response;

public class SyncCurrentEpcsCallBack extends BaseSyncCallBack<ReaderDTO> {
    public SyncCurrentEpcsCallBack(MutableLiveData<String> syncResult) {
        super(syncResult);
    }

    @Override
    public void onResponse(Call<ReaderDTO> call, Response<ReaderDTO> response) {
        ReaderDTO rs = response.body();

        if (rs != null) {
            // Assets sync succeeded.
            syncResult.setValue(getAppContext().getString(R.string.assets_sync_completed));
        } else {
            // no Assets found
            syncResult.setValue(getAppContext().getString(R.string.no_sites_found_alert));
        }
    }
}
