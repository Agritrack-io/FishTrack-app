package io.agritrack.philosofish.api.sync;

import static io.agritrack.philosofish.FishTrackApplication.getAppContext;

import androidx.lifecycle.MutableLiveData;

import java.util.List;

import io.agritrack.philosofish.R;
import io.agritrack.philosofish.data.db.MobileDB;
import io.agritrack.philosofish.data.dto.wh.AssetDTO;
import retrofit2.Call;
import retrofit2.Response;

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