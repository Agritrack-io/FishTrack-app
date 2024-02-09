package io.agritrack.api.sync;

import androidx.lifecycle.MutableLiveData;

import java.util.List;

import io.agritrack.kefalonia.R;
import io.agritrack.data.db.MobileDB;
import io.agritrack.data.dto.wh.AssetDTO;
import retrofit2.Call;
import retrofit2.Response;

import static io.agritrack.FishTrackApplication.getAppContext;

public class SyncAssetsCallBack extends BaseSyncCallBack<List<AssetDTO>> {

    public SyncAssetsCallBack(MutableLiveData<String> syncResult) {
        super(syncResult);
    }

    @Override
    public void onResponse(Call<List<AssetDTO>> call, Response<List<AssetDTO>> response) {
        List<AssetDTO> rs = response.body();

        if (rs != null) {
            // get an instance of local DB
            db = MobileDB.getInstance(getAppContext());

            for (AssetDTO assetDTO : rs) {
                db.assetDAO().insert(AssetDTO.convert(assetDTO));
            }
            // Assets sync succeeded.
            syncResult.setValue(getAppContext().getString(R.string.assets_sync_completed));
        } else {
            // no Assets found
            syncResult.setValue(getAppContext().getString(R.string.no_sites_found_alert));
        }
    }
}