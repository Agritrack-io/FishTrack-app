package io.agritrack.philosofish.api.sync;

import static io.agritrack.philosofish.FishTrackApplication.getAppContext;

import androidx.lifecycle.MutableLiveData;

import java.util.List;

import io.agritrack.philosofish.R;
import io.agritrack.philosofish.data.db.MobileDB;
import io.agritrack.philosofish.data.dto.wh.AssetDTO;
import io.agritrack.philosofish.data.model.wh.Asset;
import retrofit2.Call;
import retrofit2.Response;

public class SyncAssetsCallBack extends BaseSyncCallBack<List<AssetDTO>> {

    public SyncAssetsCallBack(MutableLiveData<String> syncResult) {
        super(syncResult);
    }

    @Override
    public void onResponse(Call<List<AssetDTO>> call, Response<List<AssetDTO>> response) {
        List<AssetDTO> rs = response.body();

        db = MobileDB.getInstance(getAppContext());

        if (rs != null) {

            // Remove ALL local assets to avoid duplication and null old records
            db.assetDAO().deleteAll();

            for (AssetDTO dto : rs) {
                db.assetDAO().insert(AssetDTO.convert(dto));
            }

            syncResult.setValue(getAppContext().getString(R.string.assets_sync_completed));

        } else {
            syncResult.setValue(getAppContext().getString(R.string.no));
        }
    }



}